package com.main.script

import com.main.utils.OsUtils


object Script {

    private const val WINDOWS_BAT = "\n" +
            "@echo off\n" +
            "cd %1\n" +
            "dir\n" +
            "\n" +
            "start TortoiseProc.exe /command:status %1\n" +
            "exit"


    private const val UNIX_SHELL = ""

    fun getScript(): String {
        return if (OsUtils.isWindows()) WINDOWS_BAT else UNIX_SHELL
    }
}