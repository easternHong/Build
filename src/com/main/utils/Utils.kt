package com.main.utils

import java.text.SimpleDateFormat
import java.util.*
import javax.swing.JLabel
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

fun getLabelWidth(lable: JLabel): Int {
    val font = lable.font // JLabel所使用的字体
    val text = lable.text // JLabel文字内容
    val fm = lable.getFontMetrics(font) // 获取字体规格
    return fm.stringWidth(text) // 宽（像素）
}

