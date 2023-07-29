package net.osdn.ja.gokigen.testsampleapp

import android.widget.CheckBox
import android.widget.EditText

class MyDataProvider(private val address: EditText, private val user: EditText, private val pass: EditText, private val check1: CheckBox, private val check2: CheckBox,  private val check3: CheckBox)
{
    var ipAddress: String? = null

    fun getAddress() : String
    {
        return (ipAddress?: address.text.toString())
    }

    fun setAddress(address: String)
    {
        ipAddress = address
    }

    fun getUser() : String
    {
        return (user.text.toString())
    }

    fun getPass() : String
    {
        return (pass.text.toString())
    }

    fun isChecked1() : Boolean
    {
        return (check1.isChecked)
    }

    fun isChecked2() : Boolean
    {
        return (check2.isChecked)
    }

    fun isChecked3() : Boolean
    {
        return (check3.isChecked)
    }
}
