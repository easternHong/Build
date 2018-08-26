package com.main.entry

import com.main.utils.Log
import java.awt.EventQueue
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*


class ReadConfigFile(private var file: File) {
    var property = Properties()

    fun getProperty(key: String): String? {
        return property.getProperty(key)
    }

    fun putProperty(key: String, value: String) {
        property[key] = value
        EventQueue.invokeLater {
            property.store(FileOutputStream(file), "")
        }
    }

    init {
        try {
            if (!file.exists()) {
                Log.e("找不到配置文件")
            }
            val inputStream = FileInputStream(file)
            property.load(inputStream)
            Log.i(":" + property)
        } catch (e: Exception) {
            Log.i("e:$e")
        }
    }

}
