package com.main.utils


object OsUtils {

    private var OS = ""

    fun getOsName(): String {
        if (OS == "") {
            OS = System.getProperty("os.name"); }
        return OS
    }

    fun isWindows(): Boolean {
        return getOsName().startsWith("Windows")
    }

    fun isMac(): Boolean {
        return getOsName().startsWith("Mac")
    }

    fun isLinux(): Boolean {
        return getOsName().startsWith("Linux")
    }


    @JvmStatic
    fun main(args: Array<String>) {
        println(getOsName())
        val map = System.getenv()
        for (item in map) {
            println(map[item.key])
        }
    }
}

fun isNumeric(str: String?): Boolean {
    return str != null && str.matches("-?\\d+(\\.\\d+)?".toRegex())  //match a number with optional '-' and decimal.
}