package net.osdn.ja.gokigen.testsampleapp.utils.storefile

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class DataStoreLocal(private val context: Context)
{
    /**
     *   保存用ディレクトリを準備する（ダメな場合はアプリ領域のディレクトリを確保する）
     *
     */
    private fun prepareLocalOutputDirectory(): File
    {
        val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        mediaDir?.mkdirs()
        return (if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir)
    }

    fun storeBinaryDataLocal(fileName: String, data: ByteArray)
    {
        Log.v(TAG, " storeImage() : $fileName")
        try
        {
            val photoFilePath = File(prepareLocalOutputDirectory(), "P" + SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + "_" + fileName)
            val outputStream = FileOutputStream(photoFilePath)
            outputStream.write(data, 0, data.size)
            outputStream.flush()
            outputStream.close()
            Log.v(TAG, "  - - - - STORED: $photoFilePath ")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = DataStoreLocal::class.java.simpleName
        private const val FILENAME_FORMAT = "yyyyMMdd_HHmmss"
    }
}
