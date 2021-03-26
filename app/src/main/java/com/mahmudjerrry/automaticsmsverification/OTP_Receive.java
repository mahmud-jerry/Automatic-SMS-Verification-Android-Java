package com.mahmudjerrry.automaticsmsverification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.Status;

public class OTP_Receive extends BroadcastReceiver {
    private MainActivity otpListener;

    /**
     * @param otpListener
     */
    public void setOTPListener(MainActivity otpListener) {
        this.otpListener = otpListener;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            Status status = (Status) extras.get(SmsRetriever.EXTRA_STATUS);
            switch (status.getStatusCode()) {
                case CommonStatusCodes
                        .SUCCESS:
                    String message = (String) extras.get(SmsRetriever.EXTRA_SMS_MESSAGE);
                    try{
                        if (otpListener != null) {
                            otpListener.onOTPReceived(message.split(":")[1]);
                        }
                    }catch (Exception e){
                        Log.e("exception",e.toString());
                    }

                    break;
                case CommonStatusCodes.TIMEOUT:
                    break;
            }
        }
    }
    public interface OTPReceiveListener {

        void onOTPReceived(String otp);
    }
}