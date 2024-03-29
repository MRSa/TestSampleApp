package net.osdn.ja.gokigen.testsampleapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity()
{
    private lateinit var myListener: MyActionListener
    private lateinit var myDataProvider: MyDataProvider
    override fun onCreate(savedInstanceState: Bundle?)
    {
        Log.v(TAG, " ----- onCreate() -----")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        startup()

    }

    private fun startup()
    {
        try
        {
            // ボタンと表示エリアを設定する
            myDataProvider = MyDataProvider(findViewById(R.id.ip_address), findViewById(R.id.ftp_username), findViewById(R.id.ftp_password), findViewById(R.id.checkbox1), findViewById(R.id.checkbox2), findViewById(R.id.checkbox3))
            myListener = MyActionListener(this, myDataProvider, findViewById(R.id.text_information), findViewById(R.id.text_status))
            findViewById<Button>(R.id.btnConnect).visibility = View.INVISIBLE
            findViewById<Button>(R.id.btnDisconnect).visibility = View.INVISIBLE
            findViewById<Button>(R.id.btnWifiSet).visibility = View.INVISIBLE

            // TextViewの表示エリアをスクロールできるようにする
            findViewById<TextView>(R.id.text_status).setMovementMethod(ScrollingMovementMethod())

            // 権限が確保されているか確認する
            if (allPermissionsGranted())
            {
                Log.v(TAG, "allPermissionsGranted() : true")
                onReadyClass()
            }
            else
            {
                Log.v(TAG, "====== REQUEST PERMISSIONS ======")
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            }
        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun onReadyClass()
    {
        // アプリケーションの動作開始！
        try
        {
            // ボタン押下を有効化する
            enableButton(R.id.btnConnect)
            enableButton(R.id.btnDisconnect)
            enableButton(R.id.btnWifiSet)

        }
        catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    private fun enableButton(id: Int)
    {
        // ボタン押下を有効化する
        val btn1 = findViewById<Button>(id)
        btn1.setOnClickListener(myListener)
        btn1.setOnLongClickListener(myListener)
        btn1.visibility = View.VISIBLE
    }

    private fun allPermissionsGranted() : Boolean
    {
        var result = true
        for (param in REQUIRED_PERMISSIONS)
        {
            if (ContextCompat.checkSelfPermission(baseContext, param) != PackageManager.PERMISSION_GRANTED)
            {
                // Permission Denied
                if (param == Manifest.permission.READ_EXTERNAL_STORAGE)
                {
                    // この場合は権限付与の判断を除外 (デバイスが JELLY_BEAN よりも古く、READ_EXTERNAL_STORAGE がない場合）
                }
                else if ((param == Manifest.permission.ACCESS_MEDIA_LOCATION)&&(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q))
                {
                    //　この場合は権限付与の判断を除外 (デバイスが (10) よりも古く、ACCESS_MEDIA_LOCATION がない場合）
                }
                else
                {
                    result = false
                }
            }
        }
        return (result)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.v(TAG, "------------------------- onRequestPermissionsResult() ")
        if (requestCode == REQUEST_CODE_PERMISSIONS)
        {
            if (allPermissionsGranted())
            {
                onReadyClass()
            }
            else
            {
                Log.v(TAG, "----- onRequestPermissionsResult() : false")
                Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    companion object
    {
        private val TAG = MainActivity::class.java.simpleName

        private const val REQUEST_CODE_PERMISSIONS = 10

        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.VIBRATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            //Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        )
    }
}
