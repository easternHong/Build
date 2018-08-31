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
    private val okHttpClient = OkHttpClient()


    private var buildNumber = -1
    private var jobName = ""
    private var branch = ""
    private lateinit var jenkins: JenkinsServer
    private const val SVN_FILE = "svn/Subversion-1.10.2.zip"
    private const val BUILD_STATE_IDLE = 0
    private const val BUILD_STATE_STARTED = 1
    private const val BUILD_STATE_BUILDING = 2
    private const val BUILD_STATE_FAILED = 3
    private const val BUILD_STATE_BUILD_SUCCESS = 4
    private const val BUILD_STATE_DOWNLOAD_SUCCESS = 5
    private const val BUILD_STATE_PUSH_FAILED = 6
    private var buildState = BUILD_STATE_IDLE


    private const val REPO_URL = "repo_url"
    private const val REVISION = "revision"
    private const val PATCH_FILE = "patch_file"

    private lateinit var mProjectBasePath: String
    private lateinit var mProjectName: String
    private var finalArtifact: Artifact? = null


    @JvmStatic
    fun main(args: Array<String>) {
        System.setOut(PrintStream((TextAreaOutputStream(logArea, ""))))
        Log.instance.init(logArea, scrollPane)
        mProjectBasePath = "Build"
        mProjectName = System.getProperty("user.dir")
        if (!OsUtils.isWindows()) {
            val path = this.javaClass.classLoader.getResource(SVN_FILE).file
            Utils.unZipIt(path, "./.idea/svn")
            Log.i("unzip svn client for windows:$path")
        }
        val file = File(mProjectBasePath.plus("/.idea/.config"))
        configFile = when {
            OsUtils.isWindows() -> BuildConfigFile(file.absolutePath)
            else -> BuildConfigFile(file.absolutePath)
        }
        init()
        showDownloadBtn(true)
    }

    fun startApplication(file: File, project: com.intellij.openapi.project.Project) {
        mProjectBasePath = project.basePath!!
        mProjectName = project.name
        Log.instance.init(logArea, scrollPane)
        System.setOut(PrintStream(TextAreaOutputStream(logArea, "")))
        System.setErr(PrintStream(LogTextAreaOutputStream(logArea, "")))
        configFile = when {
            OsUtils.isWindows() -> BuildConfigFile(file.absolutePath)
            else -> BuildConfigFile(file.absolutePath)
        }
        init()
    }


    private lateinit var clearLogBtn: JButton
    private lateinit var jSubmitBtn: JButton

    private lateinit var jFrame: JFrame
    private lateinit var jPanel: JPanel
    private var scrollPane = JScrollPane(logArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

    private lateinit var configFile: IConfigFile

    private var jDownLoadBtn: JButton? = null

    private fun init() {
        jFrame = JFrame(mProjectName + "插件本地服务器构建辅助工具")
        jFrame.addComponentListener(object : SimpleComponentListener() {
            override fun componentResized(e: ComponentEvent?) {
                //need to resize jPanel
                try {
                    val frameWidth = jFrame.width
                    jPanel.setBounds(0, 0, jFrame.width, jFrame.height)
                    val y = jSubmitBtn.bounds.y + jSubmitBtn.bounds.height + 10
                    scrollPane.setBounds(0, y, frameWidth, jFrame.height - y)
                } catch (e: Exception) {
                }
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
            //jSubmitBtn.isEnabled = !work()
        })
        jFrame.isVisible = true
        if (buildNumber == -1) {
            Log.i("Welcome!!!")
        }
    }

    private fun addWidgets() {
        jSubmitBtn = JButton("再次编译")
        jSubmitBtn.setBounds(10, 10, BTN_WIDTH, 30)
        jPanel.add(jSubmitBtn)
        jSubmitBtn.addActionListener {
            if (buildState == BUILD_STATE_BUILDING) {
                val input = JOptionPane.showOptionDialog(jFrame, "重新编译？",
                        "还没完成，重新开始？", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null)
                TaskManager.execute(Runnable {
                    if (input == JOptionPane.OK_OPTION) {
                        buildState = BUILD_STATE_IDLE
                        // do something
                        //先要取消前面的编译
                        Log.i("停止前一个编译:$buildNumber")
                        val ret = jenkins.getJob(jobName)?.getBuildByNumber(buildNumber)?.Stop()
                        Log.i("$ret")
                        work()
                    }
                })
                return@addActionListener
            }
            work()
        }

        clearLogBtn = JButton("清空日志")
        clearLogBtn.setBounds(BTN_WIDTH + 30, 10, BTN_WIDTH, 30)
        jPanel.add(clearLogBtn)
        clearLogBtn.addActionListener {
            logArea.text = ""
        }

        //日志输出区域
        val y = jSubmitBtn.bounds.y + jSubmitBtn.bounds.height + 10
        scrollPane.setBounds(0, y, WINDOW_WIDTH, WINDOW_HEIGHT - y)
        jPanel.add(scrollPane)
        //不在编译
        if (buildState == BUILD_STATE_IDLE) {
            work()
        }
    }


    private fun work(): Boolean {
        configFile.putProperty("workspace", mProjectBasePath)
        //备份配置文件。
        TaskManager.execute(Runnable {
            Log.i("**************************************")
            //jSubmitBtn.isEnabled = true
            //创建脚本
            if (!OsUtils.isWindows()) {
                createShellFile()
                Runtime.getRuntime().exec("chmod u+x $mProjectBasePath/.idea/.shell")
                val list = listOf("$mProjectBasePath/.idea/.shell", mProjectBasePath).toMutableList()
                val retList = RunCmd.executeShell(list)
                for (item in retList) {
                    val index = item.indexOf(":")
                    if (index != -1) {
                        configFile.putProperty(item.substring(0, index), item.substring(index + 1))
                    }
                }
                if (configFile.getProperty(REPO_URL).isNullOrEmpty()) {
                    Log.i("没有发现svn repo url")
                    return@Runnable
                }
                //get revision
                if (configFile.getProperty(REVISION).isNullOrEmpty()) {
                    Log.i("没有发现svn revision")
                    return@Runnable
                }
                //get revision
                if (configFile.getProperty(PATCH_FILE).isNullOrEmpty()) {
                    Log.i("没有发现 patch_file")
//                    return@Runnable
                }
            } else {
                //get patch_file
                RunCmd.executeShell(listOf("cmd.exe", "/c", "$mProjectBasePath/.idea/svn/bin/svn diff >>diff.patch",
                        mProjectBasePath).toMutableList())
                configFile.putProperty("patch_file", "$mProjectBasePath/diff.patch")
                //get repo_url
                var ret = RunCmd.executeShell(listOf("cmd.exe", "/c", "$mProjectBasePath/.idea/svn/bin/svn info --show-item=url",
                        mProjectBasePath).toMutableList())
                if (ret.size == 0) {
                    Log.i("没有发现svn repo url")
                    return@Runnable
                }
                configFile.putProperty(REPO_URL, ret[0])
                //get revision
                ret = RunCmd.executeShell(listOf("cmd.exe", "/c", "$mProjectBasePath/.idea/svn/bin/svn info --show-item=revision",
                        mProjectBasePath).toMutableList())
                if (ret.size == 0) {
                    Log.i("没有发现svn revision")
                    return@Runnable
                }
                configFile.putProperty(REVISION, ret[0])
            }
            if (checkParameters()) {
                //check again
            } else {
                //jSubmitBtn.isEnabled = true
                return@Runnable
            }
            Log.i("**************************************")
            Utils.printDog()
            makeMission()
            Log.i("*****start remote build*****")
        })
        return true
    }

    private fun createShellFile() {
        val scripFile = File(mProjectBasePath + "/.idea/.shell")
        //创建文件
        if (!scripFile.exists()) {
            scripFile.createNewFile()
        }
        val script = Script.getScript()
        scripFile.bufferedWriter().use { out ->
            out.write(script)
            out.close()
        }
        if (OsUtils.isWindows()) {
            val path = this.javaClass.classLoader.getResource(SVN_FILE).file
            Utils.unZipIt(path, mProjectBasePath + "/.idea/svn")
            Log.i("unzip svn client for windows:$path")
        }
        Log.i("create shell file:" + scripFile.exists())
    }

    private fun checkParameters(): Boolean {
        var goAhead = true
        //revision
        val revision = configFile.getProperty(REVISION)
        Log.i("revision:$revision")
        if (revision.isNullOrEmpty() or !isNumeric(revision)) {
            Log.i(configFile.getProperty("workspace") + ": is not a valid svn repo")
            goAhead = false
        }
        //svn_url
        val repoUrl = configFile.getProperty(REPO_URL)
        Log.i("repo_url:$repoUrl")
        if (repoUrl.isNullOrEmpty()) {
            Log.i(configFile.getProperty("workspace") + ": is not a valid svn repo")
            goAhead = false
        }
        //patch_file
        val patchFile = configFile.getProperty("patch_file")
        Log.i("patch_file:$patchFile")
        if (!(patchFile != null && File(patchFile).exists())) {
            Log.i("$mProjectBasePath: can't find patch file")
            goAhead = false
        }
        return goAhead
    }

    private fun showDownloadBtn(show: Boolean) {
        if (show && jDownLoadBtn == null) {
            jDownLoadBtn = JButton("重新拉取so")
            jDownLoadBtn!!.setBounds(clearLogBtn.x + clearLogBtn.width + 20, 10, BTN_WIDTH * 2, 30)
            jPanel.add(jDownLoadBtn)
            jDownLoadBtn!!.addActionListener {
                getArtifacts(finalArtifact!!, Get())
            }
        }
        jDownLoadBtn?.isVisible = show

    }

    private fun getJobNameFromUrl(): String {
        val url = configFile.getProperty(REPO_URL)
        val index = url?.lastIndexOf("/")!!
        val ret = url.substring(index.plus(1))
        return ret.substring(0, ret.indexOf("-"))
    }

    private fun getBranchNameFromUrl(): String {
        val repoUrl = configFile.getProperty(REPO_URL) ?: return ""
        return repoUrl.substring(repoUrl.lastIndexOf("-") + 1)
                .replace("android_", "")
    }


    private fun makeMission() {
        jenkins = JenkinsServer(URI("http://172.26.71.18:8087/"),
                "yymain",
                "f63133c6e88e5ed717c71fc9a02d2d60")
        branch = getBranchNameFromUrl()
        jobName = getJobNameFromUrl()

        val pMap = HashMap<String, String>()
        val fMap = HashMap<String, File>()
        pMap["branch"] = branch
        pMap[REVISION] = configFile.getProperty(REVISION)!!
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
        //jSubmitBtn.isEnabled = false
    }

    private fun checkBuildStatus() {
        TaskManager.executeDelay(runnable, 3000)
    }


    private
    val runnable = Runnable {
        val list = jenkins.getJob(jobName).allBuilds
        var goAgain = true
        for (item in list) {
            if (item.number == buildNumber) {
                val ret = if (item.details().result == null) BuildResult.BUILDING else item.details().result
                Log.i("build $buildNumber status:$ret")
                goAgain = false
                when (ret) {
                    null -> checkBuildStatus()
                    BuildResult.BUILDING -> {
                        checkBuildStatus()
                        buildState = BUILD_STATE_BUILDING
                    }
                    BuildResult.SUCCESS -> {
                        buildState = BUILD_STATE_BUILD_SUCCESS
                        Log.i("build $buildNumber :SUCCESS")
                        Log.i("this build cost :" + item.details().duration + "ms")
                        val artifacts = item.details().artifacts
                        for (a in artifacts) {
                            Log.i("get artifacts :" + a.fileName)
                        }
                        if (artifacts != null && artifacts.size > 0) {
                            getArtifacts(artifacts[0], Get())
                        }
                        //jSubmitBtn.isEnabled = true
                    }
                    else -> {
                        //jSubmitBtn.isEnabled = true
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


    private fun getArtifacts(artifact: Artifact, iGet: IGetArtifact) {
        finalArtifact = artifact
        val file = File(mProjectBasePath + "/.idea/${artifact.fileName}")
        if (buildState == BUILD_STATE_PUSH_FAILED && file.exists()) {
            iGet.success()
            return
        }
        //http://172.26.71.18:8000/out/
        val uri = "http://172.26.71.18:8087/job/$jobName/lastSuccessfulBuild/artifact/${artifact.relativePath}"
        //1.下载文件
        try {
            iGet.start()
            val request = Request.Builder().url(uri).build()
            val response = okHttpClient.newCall(request).execute()
            val body = response.body()
            val contentLength = body.contentLength()
            val source = body.source()

            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            val sink = Okio.buffer(Okio.sink(file))

            var totalRead: Long = 0
            var read: Long = 0
            while ({ read = source.read(sink.buffer(), 2048);read }() != -1L) {
                totalRead += read
                val progress = (totalRead * 100 / contentLength).toInt()
                iGet.progress(progress)
            }
            sink.writeAll(source)
            sink.flush()
            sink.close()
            iGet.success()
        } catch (e: Throwable) {
            iGet.failed(e)
        }
    }


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
            buildState = BUILD_STATE_DOWNLOAD_SUCCESS
            showDownloadBtn(true)
            Log.i("download success")
            //push to
            val adbProperties = BuildConfigFile("$mProjectBasePath/local.properties")
            val adbPath = adbProperties.getProperty("sdk.dir") + "/platform-tools/adb"
            Log.i("is adb exist? " + File(adbPath).exists())
            val cmd = listOf(adbPath, "push", mProjectBasePath + "/.idea/" + finalArtifact?.fileName,
                    "/sdcard/yyplugins/" + finalArtifact?.fileName).toMutableList()
            val retList = RunCmd.executeShell(cmd)
            Log.i("" + retList)
            if (retList.size > 0 && retList[0].contains("[100%]")) {
                // 消息对话框无返回, 仅做通知作用
                val input = JOptionPane.showOptionDialog(jFrame, "so已经更新到手机",
                        "编译完成", JOptionPane.YES_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null)
                buildState = BUILD_STATE_IDLE
                if (input == JOptionPane.OK_OPTION || input == JOptionPane.CANCEL_OPTION) {
                    // do something
                }
            } else {
                buildState = BUILD_STATE_PUSH_FAILED
                //push 到手机失败，重新push
                showDownloadBtn(true)
            }
        }

        override fun start() {
            Log.i("download start")
            showDownloadBtn(true)
        }
    }
}