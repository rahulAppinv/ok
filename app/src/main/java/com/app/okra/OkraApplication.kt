package com.app.okra

import android.app.Application
import android.content.Context


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