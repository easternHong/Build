package com.main.utils

import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants


object Utils {

    private val dateFormat = SimpleDateFormat("MM-dd HH:mm:ss:SSS", Locale.getDefault())
    fun centerText(textPane: JTextPane) {
        val doc = textPane.styledDocument
        val center = SimpleAttributeSet()
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER)
        doc.setParagraphAttributes(0, doc.length, center, false)
    }

    fun getTime(): String {
        return dateFormat.format(Date(System.currentTimeMillis()))
    }

    fun printDog() {
        Log.i("")
    }
}

