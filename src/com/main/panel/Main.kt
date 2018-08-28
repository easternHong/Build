package com.main.panel

import com.main.entry.BuildConfigFile
import com.main.entry.IConfigFile
import com.main.script.Script
import com.main.utils.*
import com.main.utils.log.LogTextAreaOutputStream
import com.main.utils.log.TextAreaOutputStream
import main.utils.task.TaskManager
import okhttp3.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.ComponentEvent
import java.io.File
import java.io.IOException
import java.io.PrintStream
import javax.swing.*


object Main {

    private const val WINDOW_WIDTH = 700
    private const val WINDOW_HEIGHT = 400
    private const val BTN_WIDTH = 100
    private var logArea = JTextArea()
    private lateinit var project: com.intellij.openapi.project.Project
    private val okHttpClient = OkHttpClient()
//    @JvmStatic
//    fun main(args: Array<String>) {
//        System.setOut(PrintStream(TextAreaOutputStream(logArea, "")))
//        System.setErr(PrintStream(LogTextAreaOutputStream(logArea, "")))
//        accountFile = when {
//            OsUtils.isWindows() -> WindowsAccountFile(("/Users/eastern/project/RemoteBuild/src/config"))
//            OsUtils.isMac() -> MacAccountFile(("/Users/eastern/project/RemoteBuild/src/config"))
//            else -> ReadConfigFile(File("/Users/eastern/project/RemoteBuild/src/config"))
//        }
//        EventQueue.invokeLater({ init() })
//    }

    fun startApplication(file: File, project: com.intellij.openapi.project.Project) {
        System.setOut(PrintStream(TextAreaOutputStream(logArea, "")))
        System.setErr(PrintStream(LogTextAreaOutputStream(logArea, "")))
        accountFile = when {
            OsUtils.isWindows() -> {
                BuildConfigFile(System.getProperty("user.home") + "/.build_config")
            }
            else -> BuildConfigFile(System.getProperty("user.home") + "/.build_config")
        }

        configFile = when {
            OsUtils.isWindows() -> BuildConfigFile(file.absolutePath)
            else -> BuildConfigFile(file.absolutePath)
        }
        this.project = project
        init()
    }


    private lateinit var clearLogBtn: JButton
    private lateinit var jSubmitBtn: JButton
    private lateinit var jFrame: JFrame
    private lateinit var jPanel: JPanel
    private lateinit var scrollPane: JScrollPane

    private lateinit var accountFile: IConfigFile
    private lateinit var configFile: IConfigFile

    private fun init() {
        jFrame = JFrame(project.name + "插件本地服务器构建辅助工具")
        jFrame.addComponentListener(object : SimpleComponentListener() {
            override fun componentResized(e: ComponentEvent?) {
                //need to resize jPanel
                val frameWidth = jFrame.width
                jPanel.setBounds(0, 0, jFrame.width, jFrame.height)
                val y = jSubmitBtn.bounds.y + jSubmitBtn.bounds.height + 10
                scrollPane.setBounds(0, y, frameWidth, jFrame.height - y)
            }
        })
        jFrame.minimumSize = Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)
        jPanel = JPanel()
        jPanel.layout = null

        val c = jFrame.contentPane
        c.add(jPanel, BorderLayout.CENTER)
        jFrame.layout = null
        c.add(jPanel)

        addWidgets()

        jFrame.setBounds(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT)
        jPanel.setBounds(0, 0, jFrame.width, jFrame.height)

        jFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        jFrame.setLocation(100, 70)
        EventQueue.invokeLater({
            jSubmitBtn.isEnabled = !work()
        })
        jFrame.isVisible = true
        Log.i("Welcome!!!")
    }

    private fun addWidgets() {
        jSubmitBtn = JButton("Go")
        jSubmitBtn.setBounds(10, 10, BTN_WIDTH, 20)
        jPanel.add(jSubmitBtn)
        jSubmitBtn.addActionListener {
            work()
        }

        clearLogBtn = JButton("清空日志")
        clearLogBtn.setBounds(BTN_WIDTH + 20, 10, BTN_WIDTH, 20)
        jPanel.add(clearLogBtn)
        clearLogBtn.addActionListener {
            logArea.text = ""
        }


        //日志输出区域
        scrollPane = JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
        val y = jSubmitBtn.bounds.y + jSubmitBtn.bounds.height + 10
        scrollPane.setBounds(0, y, WINDOW_WIDTH, WINDOW_HEIGHT - y)
        jPanel.add(scrollPane)


    }


    private fun work(): Boolean {
        //check account
        var isAccountValid = true
        val account = accountFile.getProperty("account")
        if (account == null || account.isEmpty()) {
            isAccountValid = false
            Log.i("account is empty")
        }

        val pwd = accountFile.getProperty("pwd")
        if (pwd == null || pwd.isEmpty()) {
            isAccountValid = false
            Log.i("pwd is empty")
        }

        if (!isAccountValid) {
            showAccountPanel()
            return false
        }

        if (!checkParameters()) {
            return false
        }
        configFile.putProperty("workspace", project.basePath!!)
        //备份配置文件。
        TaskManager.execute(Runnable {
            jSubmitBtn.isEnabled = false
            Log.i("**************************************")
            createShellFile()
            if (!checkParameters()) {
                //创建脚本
                val list = if (OsUtils.isWindows())
                    listOf("cmd.exe", "/c", project.basePath!! + "/.idea/.shell",
                            project.basePath!!).toMutableList()
                else listOf(project.basePath!! + "/.idea/.shell",
                        project.basePath!!).toMutableList()
                if (!OsUtils.isWindows()) {
                    Runtime.getRuntime().exec("chmod u+x " + project.basePath!! + "/.idea/.shell")
                }
                val retList = RunCmd.executeShell(list)
                for (item in retList) {
                    if (item.isEmpty() or !item.contains(":")) continue
                    val key = item.split(":")[0]
                    if (key.isEmpty()) continue
                    val value = item
                            .replace("$key:", "")
                    configFile.putProperty(key, value)
                }
                jSubmitBtn.isEnabled = true
                return@Runnable
            }
            jSubmitBtn.isEnabled = true
            Log.i("**************************************")
            Utils.printDog()
            makeMission()
            Log.i("*****start remote build*****")

        })
        return true
    }

    private fun createShellFile() {
        val scripFile = File(project.basePath!! + "/.idea/.shell")
        //创建文件
        if (!scripFile.exists()) {
            scripFile.createNewFile()
        }
        val script = Script.getScript()
        scripFile.bufferedWriter().use { out ->
            out.write(script)
            out.close()
        }
        Log.i("create shell file:" + scripFile.exists())
    }

    private fun showAccountPanel() {
        val frame = JFrame("填写账号&密码")
        frame.isResizable = false
        frame.minimumSize = Dimension(200 + BTN_WIDTH, 120)
        val jPanel = JPanel()
        jPanel.layout = null

        val c = frame.contentPane
        c.add(jPanel, BorderLayout.CENTER)
        frame.layout = null
        c.add(jPanel)
        frame.setBounds(0, 0, 200 + BTN_WIDTH, 120)
        jPanel.setBounds(0, 0, frame.width, frame.height)
        frame.isVisible = true
        frame.setLocation(jFrame.x + (jFrame.width - frame.width) / 2,
                jFrame.y + 20)


        val lAccount = JLabel("账号：")
        lAccount.setBounds(10, 10, getLabelWidth(lAccount), 20)
        jPanel.add(lAccount)
        val etAccount = JTextArea("")
        val x = lAccount.x + lAccount.width + 10
        etAccount.setBounds(x, 10, jPanel.width - x - 10, 20)
        jPanel.add(etAccount)

        val lPwd = JLabel("密码：")
        lPwd.setBounds(10, 40, getLabelWidth(lPwd), 20)
        jPanel.add(lPwd)
        val etPwd = JTextArea("")
        etPwd.setBounds(lPwd.x + lPwd.width + 10, 40, jPanel.width - x - 10, 20)
        jPanel.add(etPwd)

        val btn = JButton("确定")
        btn.setBounds((200 + BTN_WIDTH - 80) / 2, lPwd.y + lPwd.height + 20, BTN_WIDTH, 20)
        jPanel.add(btn)
        btn.addActionListener {
            if (etAccount.text == null || etAccount.text.trim().isEmpty()) {
                showAlert("账号不能为空")
                return@addActionListener
            }

            if (etPwd.text == null || etPwd.text.trim().isEmpty()) {
                showAlert("密码不能为空")
                return@addActionListener
            }
            //保存账号配置文件
            accountFile.putProperty("account", etAccount.text.trim())
            accountFile.putProperty("pwd", etPwd.text.trim())
            frame.dispose()
        }

    }

    private fun checkParameters(): Boolean {
        var goAhead = true
        //revision
        val revision = configFile.getProperty("revision")
        Log.i("revision:$revision")
        if (revision.isNullOrEmpty() or !isNumeric(revision)) {
            Log.i(configFile.getProperty("workspace") + ": is not a valid svn repo")
            goAhead = false
        }
        //svn_url
        val repoUrl = configFile.getProperty("repo_url")
        Log.i("repo_url:$repoUrl")
        if (repoUrl.isNullOrEmpty()) {
            Log.i(configFile.getProperty("workspace") + ": is not a valid svn repo")
            goAhead = false
        }
        //patch_file
        val patchFile = configFile.getProperty("patch_file")
        Log.i("patch_file:$patchFile")
        if (!(patchFile != null && File(patchFile).exists())) {
            Log.i(project.basePath + ": can't find patch file")
            goAhead = false
        }
        Log.i("account:" + accountFile.getProperty("account"))
        Log.i("pwd:" + accountFile.getProperty("pwd"))
        Log.i("branch:" + getBranchNameFromUrl())
        Log.i("job:" + getJobNameFromUrl())
        return goAhead
    }


    private fun showAlert(text: String) {
        // 消息对话框无返回, 仅做通知作用
        JOptionPane.showMessageDialog(
                jFrame,
                text,
                "",
                JOptionPane.WARNING_MESSAGE
        )
    }

    private fun getJobNameFromUrl(): String {
        val url = configFile.getProperty("repo_url")
        val index = url?.lastIndexOf("/")!!
        val ret = url.substring(index.plus(1))
        return ret.substring(0, ret.indexOf("-"))
    }

    private fun getBranchNameFromUrl(): String {
        val repoUrl = configFile.getProperty("repo_url")!!
        return repoUrl.substring(repoUrl.lastIndexOf("-") + 1)
    }

    private fun makeMission() {
        val url = getFullUrl(getJobNameFromUrl(),
                "d8434e068f54c2764f030d710672f728",
                getBranchNameFromUrl(),
                accountFile.getProperty("account")!!,
                accountFile.getProperty("account")!!)
        val request = Request.Builder().url(url)
                .addHeader("accept", "application/json")
                .build()
        okHttpClient.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        Log.i("submit mission err:$e")
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        println()
                        when (response?.code()) {
                            201 -> {
                                Log.i("submit mission success:201")
                            }
                            200 -> {
                                Log.i("submit mission success:200")
                            }
                            else -> Log.i("submit mission failed?" + response?.code())
                        }
                    }
                })

    }

    private fun getFullUrl(jobName: String, token: String, branch: String, account: String, pwd: String): HttpUrl {
        return HttpUrl.Builder()
                .scheme("http")
                .host("172.26.71.18")
                .port(8087)
                .addPathSegment("job")
                .addPathSegment(jobName)
                .addPathSegment("buildWithParameters")
                .addQueryParameter("token", token)
                .addQueryParameter("branch", branch)
                .addQueryParameter("svn_account", account)
                .addQueryParameter("svn_pwd", pwd)
                // Each addPathSegment separated add a / symbol to the final url
                // finally my Full URL is:
                // https://subdomain.apiweb.com/api/v1/students/8873?auth_token=71x23768234hgjwqguygqew
                .build()
    }
}