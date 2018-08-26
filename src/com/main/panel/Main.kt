package com.main.panel

import com.main.entry.ReadConfigFile
import com.main.utils.*
import com.main.utils.log.LogTextAreaOutputStream
import com.main.utils.log.TextAreaOutputStream
import main.utils.task.TaskManager
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.event.ComponentEvent
import java.io.File
import java.io.PrintStream
import javax.swing.*
import javax.swing.text.DefaultCaret


object Main {

    private const val WINDOW_WIDTH = 700
    private const val WINDOW_HEIGHT = 400
    private const val BTN_WIDTH = 100
    private var logArea = JTextArea()
    private lateinit var project: com.intellij.openapi.project.Project
//    @JvmStatic
//    fun main(args: Array<String>) {
//        System.setOut(PrintStream(TextAreaOutputStream(logArea, "")))
//        System.setErr(PrintStream(LogTextAreaOutputStream(logArea, "")))
//        configFile = ReadConfigFile(File("/Users/eastern/project/RemoteBuild/src/config"))
//        EventQueue.invokeLater({ init() })
//    }

    fun startApplication(file: File, project: com.intellij.openapi.project.Project) {
        System.setOut(PrintStream(TextAreaOutputStream(logArea, "")))
        System.setErr(PrintStream(LogTextAreaOutputStream(logArea, "")))
        configFile = ReadConfigFile(file)
        this.project = project
        EventQueue.invokeLater({ init() })
    }


    //    private lateinit var etSvnBranch: JTextArea
//    private lateinit var etAccount: JTextArea
//    private lateinit var etPwd: JTextArea
//    private lateinit var btnOpenDir: JButton
    private lateinit var clearLogBtn: JButton
    private lateinit var jSubmitBtn: JButton
    private lateinit var jFrame: JFrame
    private lateinit var jPanel: JPanel
    private lateinit var scrollPane: JScrollPane

    private lateinit var configFile: ReadConfigFile

    private fun init() {
        jFrame = JFrame(project.name + "插件本地服务器构建辅助工具")
        jFrame.addComponentListener(object : SimpleComponentListener() {
            override fun componentResized(e: ComponentEvent?) {
                //need to resize jPanel
                val frameWidth = jFrame.width
                jPanel.setBounds(0, 0, jFrame.width, jFrame.height)
//                clearLogBtn.setBounds(frameWidth - BTN_WIDTH - 10, 130, BTN_WIDTH, 20)
//                btnOpenDir.setBounds(frameWidth - BTN_WIDTH - 10, 90, BTN_WIDTH, 20)

                val y = jSubmitBtn.bounds.y + jSubmitBtn.bounds.height + 10
                scrollPane.setBounds(0, y, frameWidth, jFrame.height - y)

//                etSvnBranch.setBounds(70, 90, frameWidth - 70 - BTN_WIDTH - 20, 20)

//                jSubmitBtn.setBounds((frameWidth - BTN_WIDTH) / 2, 130, BTN_WIDTH, 20)
            }
        })
        //固定大小
//        jFrame.isResizable = false
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

        jFrame.isVisible = true
        jFrame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        jFrame.setLocation(100, 70)
    }

    public fun updateToolTitle(prefix: String, title: String) {
        jFrame.title = prefix + title
    }

    private fun addWidgets() {
//        val tvAccount = JLabel("svn账号:")
//        tvAccount.setBounds(5, 10, 80, 20)
//        jPanel.add(tvAccount)
//
//        etAccount = JTextArea("")
//        etAccount.setBounds(70, 10, 200, 20)
//        jPanel.add(etAccount)
//        etAccount.text = configFile.getProperty("account")
//
//        //svn 密码
//        val tvPwd = JLabel("svn密码:")
//        tvPwd.setBounds(5, 50, 60, 20)
//        jPanel.add(tvPwd)
//
//        etPwd = JTextArea("")
//        etPwd.setBounds(70, 50, 200, 20)
//        jPanel.add(etPwd)
//        etPwd.text = configFile.getProperty("pwd")
//
//        //工作目录
//        val tvSvnBranch = JLabel("工作目录:")
//        tvSvnBranch.setBounds(5, 90, 60, 20)
//        jPanel.add(tvSvnBranch)
//
//        etSvnBranch = JTextArea("")
//        etSvnBranch.setBounds(70, 90, WINDOW_WIDTH - 70 - BTN_WIDTH - 20, 20)
//        jPanel.add(etSvnBranch)
//        etSvnBranch.text = configFile.getProperty("workspace")
//
//
//        btnOpenDir = addFileOpen()
//        btnOpenDir.setBounds(WINDOW_WIDTH - BTN_WIDTH - 10, 90, BTN_WIDTH, 20)
//        jPanel.add(btnOpenDir)

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


    private fun work() {
        //check account
        var isAccountValid = true
        val account = configFile.getProperty("account")
        if (account == null || account.isEmpty()) {
            isAccountValid = false
        }

        val pwd = configFile.getProperty("pwd")
        if (pwd == null || pwd.isEmpty()) {
            isAccountValid = false
        }

        if (!isAccountValid) {
            showAccountPanel()
        }

        //check dir
//        if (etSvnBranch.text == null || etSvnBranch.text.isEmpty()) {
//            showAlert("请选择工作目录")
//            return
//        } else {
//            val file = File(etSvnBranch.text.trim())
//            if (!file.exists() || !file.isDirectory) {
//                showAlert("请重新选择工作目录")
//                return
//            }
//        }

        configFile.putProperty("workspace", project.basePath!!)
        //备份配置文件。
        TaskManager.execute(Runnable {
            jSubmitBtn.isEnabled = false
            Log.i("**************************************")

            if (!checkParameters()) {
                val list = listOf("/Users/eastern/project/RemoteBuild/script/shell.sh",
                        configFile.getProperty("workspace")!!).toMutableList()
                val retList = RunCmd.executeShell(list)
                for (item in retList) {
                    if (item.isEmpty() or !item.contains(":")) continue
                    val key = item.split(":")[0]
                    if (key.isEmpty()) continue
                    val value = item
                            .replace(key + ":", "")
                    configFile.putProperty(key, value)
                }
                jSubmitBtn.isEnabled = true
                return@Runnable
            }
            jSubmitBtn.isEnabled = true
            Log.i("**************************************")
            Utils.printDog()
            Log.i("*****start remote build*****")

        })
    }

    private fun showAccountPanel() {
        val frame = JFrame("填写账号&密码")
        jFrame.isResizable = false
        frame.minimumSize = Dimension(200, 120)
        val jPanel = JPanel()
        jPanel.layout = null

        val c = frame.contentPane
        c.add(jPanel, BorderLayout.CENTER)
        frame.layout = null
        c.add(jPanel)

        val lAccount = JLabel("账号：")
        lAccount.setBounds(10, 10, 40, 20)
        jPanel.add(lAccount)
        val etAccount = JTextArea("")
        etAccount.setBounds(60, 10, 200 - 60 - 10, 20)
        jPanel.add(etAccount)
//        etAccount.caretPosition = etAccount.document.length - 1

        val lPwd = JLabel("密码：")
        lPwd.setBounds(10, 40, 40, 20)
        jPanel.add(lPwd)
        val etPwd = JTextArea("")
        etPwd.setBounds(60, 40, 200 - 60 - 10, 20)
        jPanel.add(etPwd)
//        etPwd.caretPosition = etPwd.document.length - 1

        val btn = JButton("确定")
        btn.setBounds((200 - 80) / 2, 70, 80, 20)
        jPanel.add(btn)

        frame.setBounds(0, 0, 200, 120)
        jPanel.setBounds(0, 0, frame.width, frame.height)
        frame.isVisible = true
        frame.setLocation(jFrame.x + (jFrame.width - frame.width) / 2,
                jFrame.y + 20)
    }

    private fun checkParameters(): Boolean {
        var goAhead = true
        //revision
        val revision = configFile.getProperty("revision")
        Log.i("revision:" + revision)
        if (revision.isNullOrEmpty() or !isNumeric(revision)) {
            Log.i(configFile.getProperty("workspace") + ": is not a valid svn repo")
            goAhead = false
        }
        //svn_url
        val repoUrl = configFile.getProperty("repo_url")
        Log.i("repo_url:" + repoUrl)
        if (repoUrl.isNullOrEmpty()) {
            Log.i(configFile.getProperty("workspace") + ": is not a valid svn repo")
            goAhead = false
        }
        //patch_file
        val patchFile = configFile.getProperty("patch_file")
        Log.i("patch_file:" + patchFile)
        if (!(patchFile != null && File(patchFile).exists())) {
            Log.i(configFile.getProperty("workspace") + ": is not a valid svn repo")
            goAhead = false
        }
        //jenkins url


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

//    private fun addFileOpen(): JButton {
//
//        val btnOpenDir = JButton("选择目录")
//
//        btnOpenDir.isFocusable = false
//
//        btnOpenDir.addMouseListener(object : MouseAdapter() {
//
//            override fun mouseClicked(e: MouseEvent?) {
//                //设定当前可选择的文件类型，设定为 DIRECTORIES_ONLY，即只能选择文件夹
//                //如果没有设定，默认为 FILES_ONLY，即只能选择文件
//
//                val chooser = JFileChooser()
//                chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
//                //将chooser 设定为 可多选
//                //如果没有设定，默认为 false，即只能单选
//                chooser.isMultiSelectionEnabled = true
//                val value = chooser.showOpenDialog(jFrame)
//                if (value == JFileChooser.APPROVE_OPTION) {
//                    // //创建一个文件对象，接收返回值
//                    // //getSelectedFile()只能返回选中文件夹中的第一个文件夹
//                    // File dir=chooser.getSelectedFile();
//                    // System.out.println(dir.getAbsolutePath());
//                    //getSelectedFiles() 返回所有选中的文件夹
//                    val dirx = chooser.selectedFiles
//                    for (i in dirx.indices) {
//                        println(dirx[i].absolutePath)
//                        etSvnBranch.text = dirx[i].absolutePath
//                    }
//                }
//            }
//        })
//        return btnOpenDir
//    }


}