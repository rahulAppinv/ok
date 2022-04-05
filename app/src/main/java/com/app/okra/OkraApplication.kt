package com.app.okra

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle


class OkraApplication : Application(){

    init {
        instance = this
    }

    companion object {
        private var instance: OkraApplication? = null
        fun getApplicationContext(): Context {
            return instance!!.applicationContext
        }

        fun getApplicationInstance(): OkraApplication? {
            return instance
        }

    }


}