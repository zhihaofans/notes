package com.zhihaofans.androidbox.view


import android.support.design.widget.Snackbar
import com.zhihaofans.androidbox.gson.FirimUpdateGson
import com.google.gson.Gson
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloadListener
import com.liulishuo.filedownloader.FileDownloader
import com.orhanobut.logger.Logger
import okhttp3.*
import org.jetbrains.anko.*
import com.wx.android.common.util.FileUtils
import com.wx.android.common.util.SharedPreferencesUtils

class MainActivity : AppCompatActivity() {
    private val sysUtil = SystemUtil()
   fun checkUpdate(context: Context) {
        //删除上一次更新下载的安装包
        if (AppUtils.getVersionCode(context).toString() == SharedPreferencesUtils.getString("update_version")) {
            val temp_str = SharedPreferencesUtils.getString("download_file_path")
            if (FileUtils.isFileExist(temp_str)) {
                FileUtils.deleteFile(temp_str)
                SharedPreferencesUtils.remove("download_file_path")
                SharedPreferencesUtils.remove("update_version")
            }
        }
        val client = OkHttpClient()
        val url = "http://api.fir.im/apps/latest/xxx?api_token=xxx"
        request.url(url)
        val call = client.newCall(request.build())
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Snackbar.make(coordinatorLayout_main, "检测更新失败", Snackbar.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val resBody = response.body()
                var responseStr = ""
                if (resBody != null) {
                    responseStr = resBody.string()
                    Logger.d(responseStr)
                    val firimUpdateGson: FirimUpdateGson = g.fromJson(responseStr, FirimUpdateGson::class.java)
                    Logger.d(AppUtils.getVersionCode(context).toString())
                    val onlineVersionCode: Int? = firimUpdateGson.version.toIntOrNull()
                    if (onlineVersionCode is Int) {
                        Logger.d("$onlineVersionCode\nInt")
                    } else {
                        Logger.d(onlineVersionCode)
                    }
                    if (onlineVersionCode == null) {
                        Logger.e(onlineVersionCode)
                    } else if (onlineVersionCode > AppUtils.getVersionCode(this@MainActivity)) {
                        FileUtils.makeDirs(context.externalCacheDir.absolutePath + "/update/")
                        val downloadFilePath = context.externalCacheDir.absolutePath + "/update/" + AppUtils.getPackageName(this@MainActivity) + "_" + firimUpdateGson.versionShort + ".apk"
                        runOnUiThread {
                            Snackbar.make(coordinatorLayout_main, "发现更新，是否要更新？", Snackbar.LENGTH_LONG).setAction("更新", {
                                alert {
                                    title = "${firimUpdateGson.versionShort}(${firimUpdateGson.version})"
                                    message = "更新时间:${sysUtil.time2date(firimUpdateGson.updated_at.toLong() * 1000)}\n更新日志:${firimUpdateGson.changelog}"
                                    positiveButton(R.string.text_update, {
                                        val loading = indeterminateProgressDialog(message = "Please wait a bit…", title = "Loading...")
                                        loading.setCanceledOnTouchOutside(false)
                                        FileDownloader.getImpl().create(firimUpdateGson.install_url)
                                                .setPath(downloadFilePath)
                                                .setListener(object : FileDownloadListener() {
                                                    override fun pending(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                                                        Logger.d("FileDownloader\npending\n$firimUpdateGson.install_url\nBytes$soFarBytes/$totalBytes")
                                                    }

                                                    override fun connected(task: BaseDownloadTask?, etag: String?, isContinue: Boolean, soFarBytes: Int, totalBytes: Int) {
                                                        Logger.d("FileDownloader\nconnected\n$firimUpdateGson.install_url\nBytes:$soFarBytes/$totalBytes")
                                                    }

                                                    override fun progress(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                                                        Logger.d("FileDownloader\nconnected\n$firimUpdateGson.install_url\nBytes:$soFarBytes/$totalBytes")
                                                    }

                                                    override fun blockComplete(task: BaseDownloadTask?) {
                                                        Logger.d("FileDownloader\nblockComplete\n$firimUpdateGson.install_url")
                                                    }

                                                    override fun retry(task: BaseDownloadTask?, ex: Throwable?, retryingTimes: Int, soFarBytes: Int) {
                                                        Logger.d("FileDownloader\nretry\n$firimUpdateGson.install_url\nBytes:$soFarBytes\nTimes:$retryingTimes")
                                                    }

                                                    override fun completed(task: BaseDownloadTask) {
                                                        Logger.d("FileDownloader\ncompleted\n$firimUpdateGson.install_url\n$downloadFilePath")
                                                        runOnUiThread {
                                                            loading.dismiss()
                                                            SharedPreferencesUtils.put("update_version", firimUpdateGson.versionShort)
                                                            SharedPreferencesUtils.put("download_file_path", downloadFilePath)
                                                        }
                                                        sysUtil.installApk(this@MainActivity, downloadFilePath)
                                                    }

                                                    override fun paused(task: BaseDownloadTask, soFarBytes: Int, totalBytes: Int) {
                                                        Logger.d("FileDownloader\npaused\n$firimUpdateGson.install_url\nBytes:$soFarBytes/$totalBytes")
                                                    }

                                                    override fun error(task: BaseDownloadTask, e: Throwable) {
                                                        Logger.e("FileDownloader\nerror\n$firimUpdateGson.install_url\n${e.message}")
                                                        runOnUiThread {
                                                            loading.dismiss()
                                                            Snackbar.make(coordinatorLayout_main, "更新失败", Snackbar.LENGTH_SHORT).show()
                                                        }
                                                        e.printStackTrace()
                                                    }

                                                    override fun warn(task: BaseDownloadTask) {
                                                        Logger.w("FileDownloader\nwarn\n$firimUpdateGson.install_url\n$downloadFilePath")

                                                    }
                                                }).start()
                                    })
                                    negativeButton("打开网页", {
                                        sysUtil.chromeCustomTabs(this@MainActivity, firimUpdateGson.update_url)
                                    })
                                }.show()
                            }).show()
                        }
                    }
                }
            }
        })
    }
}
