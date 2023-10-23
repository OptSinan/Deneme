package com.example.optpasssdk


import OptimusPassSDK

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.optpasssdk.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.buttonInit.setOnClickListener {
            val result1 =
                OptimusPassSDK.sharedInstance.init("http://metro.optimusyazilim.com.tr:9009")

            println("sonucu bu mainactivity INIT= " + result1 + " ++++")

        }

        binding.buttonBiometric.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result = OptimusPassSDK.sharedInstance.authenticate(
                    "Biyometrik Doğrulama",
                    context = this@MainActivity
                )
                println("sonucu bu mainactivity Bimoetrik= " + result + " ++++")
            }
        }


        binding.buttonSkew.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result1 = OptimusPassSDK.sharedInstance.checkDateSkew()
                println("sonucu bu mainactivity SKEW Result = " +  result1 + "SKEW " + result1?.skew?.toString() + " ++++")

            }
        }

        binding.buttonBeginActivation.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result3 = OptimusPassSDK.sharedInstance.beginActivation(
                    "3",
                    "1",
                    "123456",
                    "pn",
                    "dcfb99b0-4589-11ee-be56-0242ac120002",
                    "1"
                )
                println("sonucu bu mainactivity BEGİN  " + result3?.toString() + " ++++")
            }
        }

        binding.buttonCompleteActivation.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result2 = OptimusPassSDK.sharedInstance.completeActivation(
                    this@MainActivity,
                    "123456"
                )
                println("sonucu bu mainactivity COMPLATE = " + result2?.toString() + " ++++")
            }
        }



        binding.buttonAccountList.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result2 = OptimusPassSDK.sharedInstance.accountList(
                    this@MainActivity,
                )
                println("sonucu bu mainactivity  ACCOUNT LİST " + result2?.toString() + " ++++")
            }
        }

        binding.buttonGetPinCode.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val result2 = OptimusPassSDK.sharedInstance.getPinCode(
                    3, this@MainActivity
                )
                println("sonucu bu mainactivity  Pin kode  = " + result2?.toString() + " ++++")
            }
        }


        binding.buttonRemoveAccount.setOnClickListener {
            val result2 = OptimusPassSDK.sharedInstance.removeAccount("3", this)
            println("sonucu bu mainactivity REMOVE ACCOUNT  " + result2?.toString() + " ++++")
        }
    }
}
