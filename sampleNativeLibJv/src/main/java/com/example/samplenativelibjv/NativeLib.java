package com.example.samplenativelibjv;

public class NativeLib {

    // Used to load the 'samplenativelibjv' library on application startup.
    static {
        System.loadLibrary("samplenativelibjv");
    }

    /**
     * A native method that is implemented by the 'samplenativelibjv' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}