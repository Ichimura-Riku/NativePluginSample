package com.example.samplelibraryjv;

import android.content.DialogInterface;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import static com.unity3d.player.UnityPlayer.UnitySendMessage;

public class NativeDialog {

    static public void showMessage(Context context, String title, String message) {


        // ボタン入力を受け取るリスナ
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yesボタン
                        UnitySendMessage("CallbackGameObject", "OnClickOk", "OKが押されたよ！");
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // Noボタン
                        UnitySendMessage("UnityTest", "FromAndroid", "メソッド呼び出し");
                        break;
                    default:
                        break;
                }
            }
        };

        // リスナを指定してダイアログ表示
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("YES, YES, YES!", listener)
                .setNegativeButton("...No?", listener)
                .show();
    }
}