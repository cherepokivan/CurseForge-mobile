package com.curseforge.mobile.util

import android.util.Log
import java.util.concurrent.ConcurrentLinkedDeque

object Logger {
    private const val LIMIT = 200
    private val buffer = ConcurrentLinkedDeque<String>()

    fun log(tag: String, message: String) {
        val line = "[$tag] $message"
        Log.d("CFMobile", line)
        buffer.addLast(line)
        while (buffer.size > LIMIT) buffer.pollFirst()
    }

    fun recent(limit: Int = 30): String = buffer.takeLast(limit).joinToString("\n")
}

private fun <T> ConcurrentLinkedDeque<T>.takeLast(count: Int): List<T> {
    return this.toList().takeLast(count)
}
