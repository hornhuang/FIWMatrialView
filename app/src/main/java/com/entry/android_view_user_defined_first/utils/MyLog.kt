package com.entry.android_view_user_defined_first.utils

import android.util.Log

class MyLog {

    companion object{
        private var TAG = "owen"

        fun d(str:String){
            Log.d(TAG, str);
        }
    }
}
