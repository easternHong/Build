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


    private const val UNIX_SHELL = "\n" +
            "\n" +
            "\n" +
            "cd $1 \n" +
            "revision=`svn info --show-item=revision`\n" +
            "if [ \"\$revision\" == \"\" ]\n" +
            "then\n" +
            "\techo \"there is no revision \"\t\n" +
            "else\n" +
            "\techo \"revision:\"\$revision\n" +
            "fi\n" +
            "\n" +
            "\n" +
            "repo_url=`svn info --show-item=url`\n" +
            "if [ \"\$repo_url\" == \"\" ]\n" +
            "then\n" +
            "\techo \"there is no repo_url \"\n" +
            "else\n" +
            "\techo \"repo_url:\"\$repo_url\n" +
            "fi\n" +
            ">diff.patch" +
            "`svn diff >>diff.patch`\n" +
            "if [ -f \"diff.patch\" ]\n" +
            "then\n" +
            "\techo \"patch_file:\"\$1\"/diff.patch\"\n" +
            "else\n" +
            "\techo \"patch file not found.\"\n" +
            "fi"

    fun getScript(): String {
        return if (OsUtils.isWindows()) WINDOWS_BAT else UNIX_SHELL
    }
}