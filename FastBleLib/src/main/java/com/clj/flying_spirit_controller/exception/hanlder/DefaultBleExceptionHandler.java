package com.clj.flying_spirit_controller.exception.hanlder;

import android.content.Context;
import android.util.Log;

import com.clj.flying_spirit_controller.exception.ConnectException;
import com.clj.flying_spirit_controller.exception.GattException;
import com.clj.flying_spirit_controller.exception.InitiatedException;
import com.clj.flying_spirit_controller.exception.OtherException;
import com.clj.flying_spirit_controller.exception.TimeoutException;

public class DefaultBleExceptionHandler extends BleExceptionHandler {

    private static final String TAG = "BleExceptionHandler";
    private Context context;

    public DefaultBleExceptionHandler(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    protected void onConnectException(ConnectException e) {
        Log.e(TAG, e.getDescription());
    }

    @Override
    protected void onGattException(GattException e) {
        Log.e(TAG, e.getDescription());
    }

    @Override
    protected void onTimeoutException(TimeoutException e) {
        Log.e(TAG, e.getDescription());
    }

    @Override
    protected void onInitiatedException(InitiatedException e) {
        Log.e(TAG, e.getDescription());
    }

    @Override
    protected void onOtherException(OtherException e) {
        Log.e(TAG, e.getDescription());
    }
}
