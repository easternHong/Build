package com.main.entry


interface IConfigFile {

    fun getProperty(key: String): String?

    fun putProperty(key: String, value: String)
}