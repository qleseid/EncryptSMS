package com.lolson.encryptsms.utility

import java.lang.System.err

/**
 * A custom logging class to help with Unit testing mock issues
 * and simple terminal printing of information
 */
class LogMe {

    //Used to include thread ID in log message (Thanks Professor!)
    private fun thread(msg: String): String{
        return " (${Thread.currentThread().id} ) $msg"
    }

    /**
     * DEBUG style log message
     */
    fun d(msg: String){
        println(":::DEBUG:: ${thread(msg)}")
    }

    /**
     * INFO style log message
     */
    fun i(msg: String){
        println(":::INFO:: ${thread(msg)}")
    }

    /**
     * ERROR style log message
     */
    fun e(msg: String){
        err.println(":::ERROR:: ${thread(msg)}")
    }

    /**
     * WARNING style log message
     */
    fun w(msg: String){
        err.println(":::WARNING:: ${thread(msg)}")
    }
}