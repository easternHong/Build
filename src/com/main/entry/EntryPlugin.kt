package com.main.entry

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.main.panel.Main
import java.io.File


class EntryPlugin : AnAction() {


    override fun actionPerformed(event: AnActionEvent?) {
        val project = event?.getData(PlatformDataKeys.PROJECT)
        val file = File(project?.basePath.plus("/.idea/.config"))
        Main.startApplication(file, project!!)
    }
}