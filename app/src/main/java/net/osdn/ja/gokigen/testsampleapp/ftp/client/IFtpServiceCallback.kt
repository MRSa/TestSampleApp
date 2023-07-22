package net.osdn.ja.gokigen.testsampleapp.ftp.client

interface IFtpServiceCallback
{
    fun onReceivedFtpResponse(command: String, response: String)
}