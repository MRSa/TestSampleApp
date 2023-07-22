package net.osdn.ja.gokigen.testsampleapp.ftp.client

import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.util.ArrayDeque
import java.util.Queue

class MyFtpClient(private val callbackReceiver: IFtpServiceCallback)
{
    private var isStart = false
    private var isConnected = false
    private var socket: Socket? = null
    private var dataOutputStream: DataOutputStream? = null
    //private var bufferedReader: BufferedReader? = null
    private val commandQueue : Queue<FtpCommand> = ArrayDeque()

    private var isStartDataPort = false
    private var isConnectedDataPort = false
    private var socketDataPort: Socket? = null

    fun connect(address: String)
    {
        try
        {
            Log.v(TAG, "connect to $address")
            val thread = Thread {
                try
                {
                    val tcpNoDelay = true
                    Log.v(TAG, " connect() : $address")
                    socket = Socket()
                    socket?.reuseAddress = true
                    socket?.keepAlive = true
                    socket?.tcpNoDelay = true
                    if (tcpNoDelay)
                    {
                        socket?.keepAlive = false
                        socket?.setPerformancePreferences(0, 2, 0)
                        socket?.oobInline = true
                        socket?.reuseAddress = false
                        socket?.trafficClass = 0x80
                    }
                    socket?.connect(InetSocketAddress(address, FTP_CONTROL_PORT), 0)
                    dataOutputStream = DataOutputStream(socket?.getOutputStream())
                    isConnected = true

                    val connectCommand = FtpCommand("connect", "connect")
                    receiveFromDevice(connectCommand)  // 接続後の一発目は、自動で読み込んでみる
                    receiverThread()
                }
                catch (e: Exception)
                {
                    e.printStackTrace()
                    callbackReceiver.onReceivedFtpResponse("connect", -1, e.message?:"EXCEPTION")
                }
            }
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    fun enqueueCommand(command: FtpCommand): Boolean
    {
        try
        {
            Log.v(TAG, " Command Enqueue : ${command.command} : ${command.value}")
            return commandQueue.offer(command)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (false)
    }

    fun disconnect()
    {
        try
        {
            // 通信関連のクローズ
            closeOutputStream()
            closeSocket()
            isStart = false
            isConnected = false
            commandQueue.clear()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        System.gc()
    }

    fun decidePassivePort(response: String)
    {
        try
        {
            // データポートの IPアドレスとポート番号を応答データから切り出す
            val pickupString = response.substring((response.indexOf("(") + 1), response.indexOf(")"))
            val dataStringArray = pickupString.split(",")
            val dataPortAddress = dataStringArray[0] + "." + dataStringArray[1] + "." +  dataStringArray[2] + "." +  dataStringArray[3]
            val dataPort = dataStringArray[4].toInt() * 256 + dataStringArray[5].toInt()
            val passiveAddress = "$dataPortAddress:$dataPort:\r\n"
            Log.v(TAG, " - - - - - -  data Port : $passiveAddress ($pickupString  ${dataStringArray.size})")
            callbackReceiver.onReceivedFtpResponse("data_port", 0, passiveAddress)
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        sleep(RECEIVE_WAIT_MS)
    }

    fun openPassivePort(address: String): Boolean
    {
        var response = true
        try
        {
            // データポートをオープンして受信できるようにする
            Log.v(TAG, "openPassivePort($address)")
            val accessPoint = address.split(":")
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            response = false
        }
        return (response)
    }

    private fun closeOutputStream()
    {
        try
        {
            dataOutputStream?.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        dataOutputStream = null
    }

    private fun closeSocket()
    {
        try
        {
            socket?.close()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        socket = null
    }

    private fun receiverThread()
    {
        if (isStart)
        {
            // すでにコマンドのスレッド動作中なので抜ける
            return
        }
        isStart = true
        Log.v(TAG, " receiverThread() : START")
        val thread = Thread {
            try
            {
                while (isStart)
                {
                    try
                    {
                        val command = commandQueue.poll()
                        if (command != null)
                        {
                            issueCommand(DUMP_LOG, command)
                            sleep(COMMAND_POLL_QUEUE_MS)

                            Log.v(TAG, " --- RECEIVE WAIT FOR REPLY --- ")
                            receiveFromDevice(command)
                        }
                        sleep(COMMAND_POLL_QUEUE_MS)
                    }
                    catch (e: Exception)
                    {
                        e.printStackTrace()
                        callbackReceiver.onReceivedFtpResponse("receiverThread(1)", -1, e.message?:"EXCEPTION")
                    }
                }
            }
            catch (e: Exception)
            {
                e.printStackTrace()
                callbackReceiver.onReceivedFtpResponse("receiverThread(2)", -1, e.message?:"EXCEPTION")
            }
        }
        try
        {
            thread.start()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callbackReceiver.onReceivedFtpResponse("receiverThread(0)", -1, e.message?:"EXCEPTION")
        }
    }


    private fun receiveFromDevice(command: FtpCommand)
    {
        try
        {
            val byteArray = ByteArray(PACKET_BUFFER_SIZE)
            val inputStream: InputStream? = socket?.getInputStream()
            if (inputStream == null)
            {
                Log.v(TAG, " InputStream is NULL... RECEIVE ABORTED.")
                callbackReceiver.onReceivedFtpResponse("receiveFromDevice($command)", -1, "InputStream is NULL...")
                return
            }

            // 初回データが受信バッファにデータが溜まるまで待つ...
            var readBytes = waitForReceive(inputStream, COMMAND_POLL_QUEUE_MS, MAX_RETRY_WAIT_COUNT)
            if (readBytes < 0)
            {
                // リトライオーバー検出
                Log.v(TAG, "  ----- DETECT RECEIVE RETRY OVER... -----")
                callbackReceiver.onReceivedFtpResponse("receiveFromDevice($command)", -1, "Receive timeout (${COMMAND_POLL_QUEUE_MS * MAX_RETRY_WAIT_COUNT} ms)")
            }

            // 受信したデータをバッファに突っ込む
            val byteStream = ByteArrayOutputStream()
            while (readBytes > 0)
            {
                readBytes = inputStream.read(byteArray, 0, PACKET_BUFFER_SIZE)
                if (readBytes <= 0)
                {
                    Log.v(TAG," RECEIVED MESSAGE FINISHED ($readBytes)")
                    break
                }
                byteStream.write(byteArray, 0, readBytes)
                sleep(RECEIVE_WAIT_MS)
                readBytes = inputStream.available()
            }
            val responseString = String(byteArray)
            callbackReceiver.onReceivedFtpResponse(command.command, 0, responseString)
            System.gc()
        }
        catch (t: Throwable)
        {
            t.printStackTrace()
            callbackReceiver.onReceivedFtpResponse("receiveFromDevice", -1, t.message?:"EXCEPTION")
        }
    }

    private fun issueCommand(isDumpReceiveLog: Boolean, command: FtpCommand)
    {
        try
        {
            val byteArray = command.value.toByteArray()
            if (byteArray.isEmpty())
            {
                // メッセージボディがない。終了する
                Log.v(TAG, " SEND BODY IS NOTHING.")
                callbackReceiver.onReceivedFtpResponse(command.command, -1, "SEND COMMAND IS NOTHING.")
                return
            }
            if (dataOutputStream == null)
            {
                Log.v(TAG, " DataOutputStream is null.")
                callbackReceiver.onReceivedFtpResponse(command.command, -1, "DataOutputStream is null.")
                return
            }
            if (isDumpReceiveLog)
            {
                // ログに送信メッセージを出力する
                SimpleLogDumper.dumpBytes("SEND[" + byteArray.size + "] ", byteArray)
            }

            // (データを)送信
            dataOutputStream?.write(byteArray)
            dataOutputStream?.flush()
        }
        catch (e: Exception)
        {
            e.printStackTrace()
            callbackReceiver.onReceivedFtpResponse(command.command, -1, e.message?:"EXCEPTION")
        }
    }

    private fun waitForReceive(inputStream: InputStream, delayMs: Int, retry_count: Int): Int
    {
        var retryCount = retry_count
        var isLogOutput = true
        var readBytes = 0
        try
        {
            while (readBytes <= 0)
            {
                sleep(delayMs)
                readBytes = inputStream.available()
                if (readBytes <= 0)
                {
                    if (isLogOutput)
                    {
                        Log.v(TAG, "  ----- waitForReceive:: is.available() WAIT... : " + delayMs + "ms")
                        isLogOutput = false
                    }
                    retryCount--
                    if (retry_count < 0)
                    {
                        return (-1)
                    }
                }
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
        return (readBytes)
    }

    private fun sleep(delayMs: Int)
    {
        try
        {
            Thread.sleep(delayMs.toLong())
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    companion object {
        private val TAG = MyFtpClient::class.java.simpleName
        private const val DUMP_LOG = true
        private const val COMMAND_POLL_QUEUE_MS = 15
        private const val MAX_RETRY_WAIT_COUNT = 20
        private const val RECEIVE_WAIT_MS = 50
        private const val PACKET_BUFFER_SIZE = 8192

        private const val FTP_CONTROL_PORT = 21
        // private const val FTP_DATA_PORT = 20

    }

}