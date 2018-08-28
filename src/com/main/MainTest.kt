package com.main

import okhttp3.*
import java.io.File
import java.io.IOException


object MainTest {


    @JvmStatic
    fun main(args: Array<String>) {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"))
        val file = File("C:\\Users\\eastern\\Downloads\\build_remote.bat")
        if (!file.exists()) {
            println("文件不存在")
        }
        val repo_url = "https://svn.yy.com/repos/src/dwmobile/pluginhomepage/android/branches/pluginhomepage-android_7.10.0_maint"
        println(repo_url.substring(repo_url.lastIndexOf("-")))
        val client = OkHttpClient()

        val url = getFullUrl("pluginhomepage", "d8434e068f54c2764f030d710672f728", "7.10.0_maint",
                "hongdongsheng@yy.com", "Hunt2014y")
        val request = Request.Builder().url(url)
                .addHeader("accept", "application/json")
                .build()
        client.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call?, e: IOException?) {
                        println()
                    }

                    override fun onResponse(call: Call?, response: Response?) {
                        println()
                        when (response?.code()) {
                            201 -> {
                                println("submit mission success")
                            }
                            200 -> {
                                println("submit mission success")
                            }
                            else -> println("submit mission failed?" + response?.code())
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