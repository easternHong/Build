package com.main

import com.main.utils.RunCmd
import java.io.File


object MainTest {


    @JvmStatic
    fun main(args: Array<String>) {

        File("somefile.txt").bufferedWriter().use { out ->
            out.write("nihaooo")
            out.newLine()
            out.write("nihaooo")
            out.write("nihaooo")
            out.newLine()
            out.write("nihaooo")
            out.close()
        }

        System.out.println("Working Directory = " +
                System.getProperty("user.dir"))
        val file = File("C:\\Users\\eastern\\Downloads\\build_remote.bat")
        if (!file.exists()) {
            println("文件不存在")
        }
        val ret = RunCmd.executeShell(listOf("cmd.exe", "/c", "build_remote.bat", "C:\\Users\\eastern").toMutableList())
        for (item in ret) {
            println("...$item")
        }
    }
}