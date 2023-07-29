package net.osdn.ja.gokigen.testsampleapp

import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.View.OnLongClickListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.osdn.ja.gokigen.testsampleapp.ftp.client.FtpCommand
import net.osdn.ja.gokigen.testsampleapp.ftp.client.IFtpServiceCallback
import net.osdn.ja.gokigen.testsampleapp.ftp.client.MyFtpClient
import net.osdn.ja.gokigen.testsampleapp.utils.communication.SimpleHttpClient
import net.osdn.ja.gokigen.testsampleapp.utils.storefile.DataStoreLocal
import java.io.ByteArrayOutputStream

class MyActionListener(private val activity: AppCompatActivity, private val dataProvider: MyDataProvider, private val informationArea: TextView, private val statusArea: TextView) : View.OnClickListener, OnLongClickListener, IFtpServiceCallback
{
    private val dataStore = DataStoreLocal(activity)
    private val httpClient = SimpleHttpClient()
    private val ftpClient = MyFtpClient(this)
    private val imageFileList = ArrayList<String>()
    private var dataReceiveError = 0

    override fun onClick(p0: View?)
    {
        when (p0?.id) {
            R.id.btnConnect -> pushedConnect()
            R.id.btnDisconnect -> pushedDisconnect()
            R.id.btnWifiSet -> pushedWifiSet()
        }
    }

    override fun onLongClick(p0: View?): Boolean
    {
        return (when (p0?.id)
        {
            R.id.btnConnect -> pushedConnect2()
            R.id.btnDisconnect -> pushedDisconnect2()
            R.id.btnWifiSet -> pushedWifiSet2()
            else -> false
        })
    }

    private fun pushedConnect()
    {
        try
        {
            val address = dataProvider.getAddress()
            Log.v(TAG, "Connect to device ($address)")
            val message = "${activity.getString(R.string.lbl_connect)} $address "

            imageFileList.clear()
            ftpClient.connect(address)
            informationArea.text = message
            statusArea.text = ""
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    private fun pushedDisconnect()
    {
        try
        {
            ftpClient.disconnect()
            informationArea.text = activity.getString(R.string.lbl_disconnect)
            statusArea.text = ""
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedWifiSet()
    {
        Log.v(TAG, "pushedWifiSet()")
        try
        {
            activity.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun pushedConnect2() : Boolean
    {
        try
        {
            informationArea.text = ""
            statusArea.text = ""
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }
    private fun pushedDisconnect2() : Boolean
    {
        try
        {
            informationArea.text = ""
            statusArea.text = ""
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    private fun pushedWifiSet2() : Boolean
    {
        try
        {
            informationArea.text = ""
            statusArea.text = ""
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (true)
    }

    override fun onReceivedFtpResponse(command: String, code: Int, response: String)
    {
         if (code == 0)
        {
            // 成功の応答の場合... FTPのシーケンスを進める
            when (command)
            {
                "connect" -> inputUser(response)
                "user" -> inputPass(response)
                "pass" -> changeCurrentWorkingDirectory(response)
                "cwd" -> setAsciiTransferMode(response)
                "ascii" -> setPassiveMode(response)
                "passive" -> checkPassivePort(response)
                "data_port" -> getFileList(response)
                "list" -> checkListCommand(response)
                "data" -> parseFileList(response)
                "quit" -> checkQuitResponse(response)
            }
        }
        else
        {
            Log.v(TAG, " onReceivedFtpResponse($command/$code) [${response.length}] $response")
            //onReceivedDataError(response)
/**/
            when (command)
            {
                "receiveFromDevice(data)" -> onReceivedDataError(response)
            }
/**/
        }
        activity.runOnUiThread {
            val message = statusArea.text.toString() + "\r\n[" + command + "] " + response
            statusArea.text = message
        }
    }

    private fun onReceivedDataError(response: String)
    {
        try
        {
            Log.v(TAG, "  ----- onReceivedDateError() : $response")
            dataReceiveError++
            if (dataReceiveError > DATA_RECEIVE_ERROR_MAX)
            {
                Log.v(TAG,  "= = = = = REACHED ERROR MAX COUNT : $dataReceiveError")
                ftpClient.enqueueCommand(FtpCommand("quit", "QUIT\r\n"))
                dataReceiveError = 0
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun inputUser(response: String)
    {
        try
        {
            if (response.startsWith("220"))
            {
                val user = dataProvider.getUser()
                ftpClient.enqueueCommand(FtpCommand("user", "USER $user\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun inputPass(response: String)
    {
        try
        {
            if (response.startsWith("331"))
            {
                val pass = dataProvider.getPass()
                ftpClient.enqueueCommand(FtpCommand("pass", "PASS $pass\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun changeCurrentWorkingDirectory(response: String)
    {
        try
        {
            if (response.startsWith("230"))
            {
                ftpClient.enqueueCommand(FtpCommand("cwd", "CWD /1/DCIM\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setAsciiTransferMode(response: String)
    {
        try
        {
            if (response.startsWith("250"))
            {
                ftpClient.enqueueCommand(FtpCommand("ascii", "TYPE A\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun setPassiveMode(response: String)
    {
        try
        {
            if (response.startsWith("200"))
            {
                ftpClient.enqueueCommand(FtpCommand("passive", "PASV\r\n"))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkPassivePort(response: String)
    {
        try
        {
            if (response.startsWith("227"))
            {
                Log.v(TAG, " Check Passive Port : $response")
                ftpClient.decidePassivePort(response)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    private fun getFileList(response: String)
    {
        try
        {
            dataReceiveError = 0
            ftpClient.openPassivePort(response)
            ftpClient.enqueueCommand(FtpCommand("list", "LIST\r\n"))
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }
    private fun checkListCommand(response: String)
    {
        try
        {
            Log.v(TAG, "RESPONSE: $response")
            if ((response.startsWith("226"))||((response.startsWith("150"))&&(response.contains("226"))))
            {
                ftpClient.enqueueCommand(FtpCommand("quit", "QUIT\r\n"))
            }
            else if (response.startsWith("150"))
            {
                Log.v(TAG, "RESP. 150")
                // 226の応答受信のために、すこし待つ...
                // （ただし、DATAポートのタイムアウトを待った方が、全データ受信できそう）
                try
                {
                    Log.v(TAG, " -----RECEIVE WAIT 1000ms")
                    Thread.sleep(1000)
                }
                catch (ee: Exception)
                {
                    ee.printStackTrace()
                }
                ftpClient.enqueueCommand(FtpCommand("list", "LIST\r\n", true))
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun parseFileList(response: String)
    {
        try
        {
            Log.v(TAG, "parseFileList")
            imageFileList.clear()
            val fileList = response.split("\r\n")
            for (files in fileList)
            {
                val fileData = files.split(Regex("\\s+"))
                if (fileData.size > 8)
                {
                    val imageFile = fileData[8]
                    imageFileList.add(imageFile)
                    //val imageDate = "${fileData[5]} ${fileData[6]} ${fileData[7]}" // MM DD YYYY
                    //imageDateList.add(fileData[16])
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun checkQuitResponse(response: String)
    {
        try
        {
            Log.v(TAG, "RESPONSE: $response")
            ftpClient.disconnect()

            if (dataProvider.isChecked2())
            {
                getImageFiles()
            }
            else
            {
                activity.runOnUiThread {
                    val message = activity.getString(R.string.finished_connection) + imageFileList.size
                    informationArea.text = message
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun getImageFiles()
    {
        try
        {
            val thread = Thread {
                try
                {
                    activity.runOnUiThread {
                        informationArea.text = activity.getString(R.string.get_images)
                    }
                    for (file in imageFileList)
                    {
                        val urlToGet = if (dataProvider.isChecked3())
                        {
                            "http://${dataProvider.getAddress()}/DCIM/T/$file" // サムネイルデータの取得
                        }
                        else
                        {
                            "http://${dataProvider.getAddress()}/DCIM/O/$file" // オリジナルデータの取得
                        }
                        val byteStream = ByteArrayOutputStream()
                        byteStream.reset()
                        Log.v(TAG, "GET $urlToGet")
                        httpClient.httpGetBytes(urlToGet, HashMap(), DEFAULT_TIMEOUT,
                            object : SimpleHttpClient.IReceivedMessageCallback {
                                override fun onCompleted() {
                                    // Log.v(TAG, " onCompleted() : $file")
                                    dataStore.storeBinaryDataLocal(file, byteStream.toByteArray())
                                    byteStream.reset()
                                }

                                override fun onErrorOccurred(e: java.lang.Exception?) {
                                    Log.v(TAG, " onErrorOccurred() : ${e?.message}")
                                }

                                override fun onReceive(
                                    readBytes: Int,
                                    length: Int,
                                    size: Int,
                                    data: ByteArray?
                                ) {
                                    //Log.v(TAG, "onReceive($readBytes, $length, $size)")
                                    if (data != null)
                                    {
                                        byteStream.write(data, 0, size)
                                    }
                                }
                            }
                        )
                    }
                    activity.runOnUiThread {
                        val message = activity.getString(R.string.finished_get_images) + imageFileList.size
                        informationArea.text = message
                    }
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object
    {
        private val TAG = MyActionListener::class.java.simpleName
        private const val DEFAULT_TIMEOUT = 15000
        private const val DATA_RECEIVE_ERROR_MAX = 5
    }
}
