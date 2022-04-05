package com.app.okra.data.preference

import android.content.SharedPreferences
import com.app.okra.OkraApplication

/*
* This class basically provide access to store and fetch data from the shared preference
* */
object PreferenceManager {

    val PREFS_FILENAME = "okra.prefs"
    val DEVICE_TOKEN: String? = "device_token"
    val AUTH_TOKEN: String? = "auth_token"
    private var sharedPref: SharedPreferences =
        OkraApplication.getApplicationContext().getSharedPreferences(PREFS_FILENAME,0)


    fun getInt(key: String?): Int {
        return sharedPref.getInt(key, 0)
    }

    fun putInt(key: String?, value: Int) {
        val editor = sharedPref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun putString(key: String?, value: String?) {
        val editor = sharedPref.edit()
        if (value != null) {
            editor.putString(key, value) // Commit the edits!
            editor.apply()
        }
    }

    fun getString(key: String?) = sharedPref.getString(key, "")

    fun putBoolean(key: String?, value: Boolean) {
        val editor = sharedPref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBoolean(key: String?) = sharedPref.getBoolean(key, false)

    fun clearAllPrefs() {
        val editor = sharedPref.edit()
        editor.clear()
        editor.apply()
    }
}