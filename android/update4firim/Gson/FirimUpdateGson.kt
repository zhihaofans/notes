package com.zhihaofans.androidbox.gson

/**
 * Created by zhihaofans on 2018/3/29.
 */
data class FirimUpdateGson(
        val name: String,
        val version: String,
        val changelog: String,
        val updated_at: Int,
        val versionShort: String,
        val install_url: String,
        val update_url: String,
        val binary: FirimUpdateBinaryGson
)

data class FirimUpdateBinaryGson(
        val fsize: Int
)
