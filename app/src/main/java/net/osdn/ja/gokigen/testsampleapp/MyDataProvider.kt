package net.osdn.ja.gokigen.testsampleapp

import android.widget.EditText

class MyDataProvider(private val address: EditText, private val user: EditText, private val pass: EditText)
{


    fun getAddress() : String
    {
        return (address.text.toString())
    }

    fun getUser() : String
    {
        return (user.text.toString())
    }

    fun getPass() : String
    {
        return (user.text.toString())
    }

}