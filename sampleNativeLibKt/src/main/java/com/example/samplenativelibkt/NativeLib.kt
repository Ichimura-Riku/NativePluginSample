package com.example.samplenativelibkt

class NativeLib {

    /**
     * A native method that is implemented by the 'samplenativelibkt' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'samplenativelibkt' library on application startup.
        init {
            System.loadLibrary("samplenativelibkt")
        }
    }
}