package net.osdn.ja.gokigen.testsampleapp.ftp.client

import android.util.Log

object SimpleLogDumper
{
    private val TAG = SimpleLogDumper::class.java.simpleName

    // デバッグ用：ログにバイト列を出力する
    fun dumpBytes(header: String, data: ByteArray?)
    {
        if (data == null)
        {
            Log.v(TAG, "DATA IS NULL")
            return
        }
        if (data.size > 8192)
        {
            Log.v(TAG, " --- DUMP DATA IS TOO LONG... " + data.size + " bytes.")
            return
        }
        var index = 0
        var message: StringBuffer
        message = StringBuffer()
        for (item in data)
        {
            index++
            message.append(String.format("%02x ", item))
            if (index >= 16)
            {
                Log.v(TAG, "$header $message")
                index = 0
                message = StringBuffer()
            }
        }
        if (index != 0)
        {
            Log.v(TAG, "$header $message")
        }
        System.gc()
    }
}
