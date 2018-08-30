package com.main.panel

import com.main.entry.BuildConfigFile
import com.main.entry.IConfigFile
import com.main.script.Script
import com.main.utils.*
import com.main.utils.log.LogTextAreaOutputStream
import com.main.utils.log.TextAreaOutputStream
import com.offbytwo.jenkins.JenkinsServer
import com.offbytwo.jenkins.model.Artifact
import com.offbytwo.jenkins.model.BuildResult
import main.utils.task.TaskManager
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Okio
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.ComponentEvent
import java.io.File
import java.io.PrintStream
import java.net.URI
import javax.swing.*


object Main {

    private const val WINDOW_WIDTH = 900
    private const val WINDOW_HEIGHT = 400
    private const val BTN_WIDTH = 100
    private var logArea = JTextArea()
    private lateinit var project: com.intellij.openapi.project.Project
    private val okHttpClient = OkHttpClient()
    @JvmStatic
    fun main(args: Array<String>) {
        System.setOut(PrintStream(TextAreaOutputStream(logArea, "")))
        System.setErr(PrintStream(LogTextAreaOutputStream(logArea, "")))
        EventQueue.invokeLater({ init() })
    }

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

    private var jDownLoadBtn: JButton? = null
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

        jFrame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        jFrame.setLocation(100, 70)
        EventQueue.invokeLater({
            jSubmitBtn.isEnabled = !work()
        })
        jFrame.isVisible = true
        Log.i("Welcome!!!")
    }

    private fun addWidgets() {
        jSubmitBtn = JButton("再次编译")
        jSubmitBtn.setBounds(10, 10, BTN_WIDTH, 30)
        jPanel.add(jSubmitBtn)
        jSubmitBtn.addActionListener {
            work()
        }

        clearLogBtn = JButton("清空日志")
        clearLogBtn.setBounds(BTN_WIDTH + 30, 10, BTN_WIDTH, 30)
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
//        不用
//        if (!isAccountValid) {
//            showAccountPanel()
//            return false
//        }

        configFile.putProperty("workspace", project.basePath!!)
        //备份配置文件。
        TaskManager.execute(Runnable {
            jSubmitBtn.isEnabled = false
            Log.i("**************************************")
            createShellFile()
            if (!checkParameters()) {
                jSubmitBtn.isEnabled = true
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
                if (checkParameters()) {
                    //check again
                } else {
                    jSubmitBtn.isEnabled = true
                    return@Runnable
                }
            }
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
        frame.minimumSize = Dimension(200 + BTN_WIDTH, 160)
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
                jFrame.y + 30)


        val lAccount = JLabel("账号：")
        lAccount.setBounds(10, 10, getLabelWidth(lAccount), 30)
        jPanel.add(lAccount)
        val etAccount = JTextArea("")
        val x = lAccount.x + lAccount.width + 10
        etAccount.setBounds(x, 10, jPanel.width - x - 10, 30)
        jPanel.add(etAccount)

        val lPwd = JLabel("密码：")
        lPwd.setBounds(10, 50, getLabelWidth(lPwd), 30)
        jPanel.add(lPwd)
        val etPwd = JTextArea("")
        etPwd.setBounds(lPwd.x + lPwd.width + 10, 50, jPanel.width - x - 10, 30)
        jPanel.add(etPwd)

        val btn = JButton("确定")
        btn.setBounds((200 + BTN_WIDTH - 80) / 2, lPwd.y + lPwd.height + 30, BTN_WIDTH, 30)
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
        return goAhead
    }

    private fun showDownloadBtn(show: Boolean) {
        if (show) {
            if (jDownLoadBtn == null) {
                jDownLoadBtn = JButton("重新拉取so")
                jDownLoadBtn!!.setBounds(clearLogBtn.x + clearLogBtn.width + 20, 10, BTN_WIDTH * 2, 30)
                jPanel.add(jSubmitBtn)
                jDownLoadBtn!!.addActionListener {
                    getArtifacts(finalArtifact!!, Get())
                }
                jPanel.add(jDownLoadBtn)
            }
        }
        jDownLoadBtn?.isVisible = show

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
        val repoUrl = configFile.getProperty("repo_url") ?: return ""
        return repoUrl.substring(repoUrl.lastIndexOf("-") + 1)
                .replace("android_", "")
    }

    private var buildNumber = -1
    private var jobName = ""
    private var branch = ""
    private lateinit var jenkins: JenkinsServer

    private fun makeMission() {
        jenkins = JenkinsServer(URI("http://172.26.71.18:8087/"),
                "yymain",
                "f63133c6e88e5ed717c71fc9a02d2d60")
        branch = getBranchNameFromUrl()
        jobName = getJobNameFromUrl()

        val pMap = HashMap<String, String>()
        val fMap = HashMap<String, File>()
        pMap["branch"] = branch
        pMap["svn_account"] = accountFile.getProperty("account")!!
        pMap["svn_pwd"] = accountFile.getProperty("pwd")!!

        pMap["revision"] = configFile.getProperty("revision")!!
        fMap["patch_file"] = File(configFile.getProperty("patch_file"))
        configFile.putProperty("branch", branch)
        //trigger a build
        buildNumber = jenkins.getJob(jobName).nextBuildNumber
        Log.i("buildNumber.number:$buildNumber")
        jenkins.getJob(jobName).build(pMap, fMap)
        Log.i("isInQueue:" + jenkins.getJob(jobName)?.isInQueue)
        buildState = BUILD_STATE_STARTED
        checkBuildStatus()
        buildState = BUILD_STATE_BUILDING
        jSubmitBtn.isEnabled = false
    }

    private fun checkBuildStatus() {
        TaskManager.executeDelay(runnable, 3000)
    }


    private val runnable = Runnable {
        val list = jenkins.getJob(jobName).allBuilds
        var goAgain = true
        for (item in list) {
            if (item.number == buildNumber) {
                val ret = item.details().result
                Log.i("build $buildNumber status:$ret")
                goAgain = false
                when (ret) {
                    null -> checkBuildStatus()
                    BuildResult.BUILDING -> {
                        checkBuildStatus()
                        buildState = BUILD_STATE_BUILDING
                    }
                    BuildResult.SUCCESS -> {
                        buildState = BUILD_STATE_SUCCESS
                        Log.i("build $buildNumber :SUCCESS")
                        Log.i("this build cost :" + item.details().duration + "ms")
                        val artifacts = item.details().artifacts
                        for (a in artifacts) {
                            Log.i("get artifacts :" + a.fileName)
                        }
                        if (artifacts != null && artifacts.size > 0) {
                            getArtifacts(artifacts[0], Get())
                        }
                        jSubmitBtn.isEnabled = true
                    }
                    else -> {
                        jSubmitBtn.isEnabled = true
                        buildState = BUILD_STATE_FAILED
                        Log.i("build ${buildNumber}failed,please check")
                        Log.i("" + item.url)
                    }
                }
            }
        }
        if (goAgain) {
            Log.i("build $buildNumber enqueuing")
            checkBuildStatus()
        }
    }

    private var finalArtifact: Artifact? = null

    private fun getArtifacts(artifact: Artifact, iGet: IGetArtifact) {
        finalArtifact = artifact
        //http://172.26.71.18:8000/out/
        val uri = "http://172.26.71.18:8087/job/$jobName/lastSuccessfulBuild/artifact/${artifact.relativePath}"
        //1.下载文件
        val DOWNLOAD_CHUNK_SIZE = 2048 //Same as Okio Segment.SIZE

        try {
            iGet?.start()
            val request = Request.Builder().url(uri).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body()
            val contentLength = body.contentLength()
            val source = body.source()

            val file = File(project.basePath + "/.idea/${artifact.fileName}")
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            val sink = Okio.buffer(Okio.sink(file))

            var totalRead: Long = 0
            var read: Long = 0
            while ({ read = source.read(sink.buffer(), DOWNLOAD_CHUNK_SIZE.toLong());read }() != -1L) {
                totalRead += read
                val progress = (totalRead * 100 / contentLength).toInt()
                iGet?.progress(progress)
            }
            sink.writeAll(source)
            sink.flush()
            sink.close()
            iGet?.success()
        } catch (e: Throwable) {
            iGet?.failed(e)
        }
    }

    private const val BUILD_STATE_IDLE = 0
    private const val BUILD_STATE_STARTED = 1
    private const val BUILD_STATE_BUILDING = 2
    private const val BUILD_STATE_FAILED = 3
    private const val BUILD_STATE_SUCCESS = 4
    private var buildState = BUILD_STATE_IDLE

    interface IGetArtifact {
        fun start()
        fun failed(e: Throwable)
        fun progress(progress: Int)
        fun success()
    }

    class Get : IGetArtifact {
        override fun failed(e: Throwable) {
            showDownloadBtn(true)
            Log.i("download failed：$e")
        }

        override fun progress(progress: Int) {
            Log.i("download progressing:$progress")
        }

        override fun success() {
            showDownloadBtn(true)
            Log.i("download success")
            //push to
            val adbProperties = BuildConfigFile(project.basePath!! + "/local.properties")
            val adbPath = adbProperties.getProperty("sdk.dir") + "/platform-tools/adb"
            Log.i("is adb exist? " + File(adbPath).exists())
            val cmd = listOf(adbPath, "push", project.basePath!! + "/.idea/" + finalArtifact?.fileName,
                    "/sdcard/yyplugins/" + finalArtifact?.fileName).toMutableList()
            val retList = RunCmd.executeShell(cmd)
            Log.i("" + retList)
            if (retList.size > 0 && retList[0].contains("[100%]")) {
                // 消息对话框无返回, 仅做通知作用
                val input = JOptionPane.showOptionDialog(jFrame, "so已经更新到手机",
                        "编译完成", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null)
                if (input == JOptionPane.OK_OPTION || input == JOptionPane.CANCEL_OPTION) {
                    // do something
                }
            }
        }

        override fun start() {
            Log.i("download start")
            showDownloadBtn(false)
        }
    }
}