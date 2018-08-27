package com.main.entry

import com.main.utils.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

open class BuildConfigFile(var path: String) : IConfigFile {

    override fun getProperty(key: String): String? {
        return property.getProperty(key)
    }

    override fun putProperty(key: String, value: String) {
        property[key] = value
        property.store(FileOutputStream(File(path)), "")
    }

    private var property = Properties()

    init {
        init()
    }

    private fun init() {
        try {
            val file = File(path)
            property.load(FileInputStream(file))
        } catch (e: Exception) {
            Log.i("read configFile:$path,$e")
        }
    }

}