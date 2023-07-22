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

class MyActionListener(private val activity: AppCompatActivity, private val dataProvider: MyDataProvider, private val informationArea: TextView, private val statusArea: TextView) : View.OnClickListener, OnLongClickListener, IFtpServiceCallback
{
    val ftpClient = MyFtpClient(this)

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

    companion object
    {
        private val TAG = MyActionListener::class.java.simpleName
    }

    override fun onReceivedFtpResponse(command: String, code: Int, response: String)
    {
        val replyMessage = response.substring(0, (response.indexOf("\r\n") + 2))
        Log.v(TAG, " onReceivedFtpResponse($command/$code) [${replyMessage.length}] $replyMessage")
        if (code == 0)
        {
            // 成功の応答の場合... FTPのシーケンスを進める
            when (command)
            {
                "connect" -> inputUser(replyMessage)
                "user" -> inputPass(replyMessage)
                "pass" -> changeCurrentWorkingDirectory(replyMessage)
                "cwd" -> setAsciiTransferMode(replyMessage)
                "ascii" -> setPassiveMode(replyMessage)
                "passive" -> checkPassivePort(replyMessage)
                "data_port" -> getFileList(replyMessage)
                "list" -> checkListCommand(replyMessage)
            }
        }
        activity.runOnUiThread {
            val message = statusArea.text.toString() + "\r\n[" + command + "] " + replyMessage
            statusArea.text = message
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
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

}
