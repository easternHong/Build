package com.main.utils

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
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

    /**
     * Unzip it
     * @param zipFile input zip file
     * @param output zip file output folder
     */
    fun unZipIt(zipFile: String, outputFolder: String) {

        val buffer = ByteArray(1024)
        try {
            //create output directory is not exists
            val folder = File(outputFolder)
            if (!folder.exists()) {
                folder.mkdir()
            }
            //get the zip file content
            val zis = ZipInputStream(FileInputStream(zipFile))
            //get the zipped file list entry
            var ze: ZipEntry? = zis.nextEntry
            while (ze != null) {
                val fileName = ze.name
                val newFile = File(outputFolder + File.separator + fileName)
                Log.i("""file unzip : ${newFile.absoluteFile}""")
                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                File(newFile.parent).mkdirs()
                val fos = FileOutputStream(newFile)
                var len = 0
                while ({ len = zis.read(buffer);len }() > 0) {
                    fos.write(buffer, 0, len)
                }
                fos.close()
                ze = zis.nextEntry
            }
            zis.closeEntry()
            zis.close()
            Log.i("unzip file done")
        } catch (ex: IOException) {
            Log.i("unzip file failed:$ex")
        }

    }
}

fun getLabelWidth(lable: JLabel): Int {
    val font = lable.font // JLabel所使用的字体
    val text = lable.text // JLabel文字内容
    val fm = lable.getFontMetrics(font) // 获取字体规格
    return fm.stringWidth(text) // 宽（像素）
}

