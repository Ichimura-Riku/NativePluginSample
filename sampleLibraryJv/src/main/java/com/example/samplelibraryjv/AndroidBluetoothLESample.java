package com.example.samplelibraryjv;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

public class AndroidBluetoothLESample {
    public static int sampleFunc(){return 0;}
    public static void staticFunction(){
        Log.d("debug-----","staticFunction" );
        UnityPlayer.UnitySendMessage("ControllerInfoDisplayUI", "onClickDebug", "static void debug");
    }


}
