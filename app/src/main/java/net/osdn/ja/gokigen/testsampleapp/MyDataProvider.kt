package net.osdn.ja.gokigen.testsampleapp

import android.widget.CheckBox
import android.widget.EditText

class MyDataProvider(private val address: EditText, private val user: EditText, private val pass: EditText, private val check1: CheckBox, private val check2: CheckBox, private val check3: CheckBox)
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
        return (pass.text.toString())
    }

    fun getCheck1() : Boolean
    {
        return (check1.isChecked)
    }

    fun getCheck2() : Boolean
    {
        return (check2.isChecked)
    }

    fun getCheck3() : Boolean
    {
        return (check3.isChecked)
    }
}
