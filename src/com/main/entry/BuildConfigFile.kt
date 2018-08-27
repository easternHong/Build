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
        try {
            property[key] = value
            property.store(FileOutputStream(path), "")
        } catch (e: Exception) {
            Log.i("e:$e")
        }
    }

    private var property = Properties()

    init {
        init()
    }

    private fun init() {
        try {
            val file = File(path)
            if (!file.exists()) {
                file.createNewFile()
            }
            property.load(FileInputStream(file))
        } catch (e: Exception) {
            Log.i("read configFile:$path,$e")
        }
    }

}