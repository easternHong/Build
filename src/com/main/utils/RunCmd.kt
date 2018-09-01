package com.main.utils

import java.io.BufferedReader
import java.io.InputStreamReader


object RunCmd {


    fun executeShell(args: MutableList<String>): ArrayList<String> {
        val list = ArrayList<String>()
        val pb = ProcessBuilder(args)
        val p = pb.start()
        val reader = BufferedReader(InputStreamReader(p.inputStream))
        var line: String? = reader.readLine()
        while (line != null) {
            list.add(line)
            line = reader.readLine()
        }
        return list
    }

}