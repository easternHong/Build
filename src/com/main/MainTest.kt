package com.main

import okhttp3.*
import java.io.File
import java.io.IOException


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
        val requestBody = FormBody.Builder()
                .add("token", "d8434e068f54c2764f030d710672f728")
                .add("branch", "4566")
                .build()
        val client = OkHttpClient()
        val request = Request.Builder().url("http://172.26.71.18:8087/job/pluginhomepage/buildWithParameters?token=d8434e068f54c2764f030d710672f728&branch=34567")
                .addHeader("accept", "application/json")
                .post(requestBody).build()
        client.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println()
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        println()
                    }
                })
    }
}