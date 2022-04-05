package com.app.okra.utils

class Event<out T>(private val content: T) {

    var isAlreadyHandled = false

    fun getContent(): T? {
        if (!isAlreadyHandled) {
            isAlreadyHandled = true
            return content
        }
        return null
    }

    fun update() {
        if (!isAlreadyHandled) {
            isAlreadyHandled = true
        }
    }

    @Synchronized
    fun peekContent(): T {
        return content
    }
}