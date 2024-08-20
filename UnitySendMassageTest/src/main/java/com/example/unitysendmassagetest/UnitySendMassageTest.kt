package com.example.unitysendmassagetest

import android.util.Log
import com.unity3d.player.UnityPlayer

class UnitySendMassageTest {

    fun runFunc(str: String) {
        Log.d("debug----", "unitySendMassage")
        UnityPlayer.UnitySendMessage(
            "ControllerInfoDisplayUI",
            "onClickDebugLog",
            "sendMassageDebug"
        )
        UnityPlayer.UnitySendMessage("HelloMR", "onClickDebug", "sendMassageDebug()")
    }
}

