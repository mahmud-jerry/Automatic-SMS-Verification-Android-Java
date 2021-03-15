package com.mahmudjerrry.automaticsmsverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity implements OTP_Receive.OTPReceiveListener {
    private EditText numberEditText , otpEditText ;
    private Button otpSendButton ,confirmOtpButton ;
    private OTP_Receive otp_receive;
    private static MainActivity ins ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //finding elements
        numberEditText = findViewById(R.id.number_edit_id);
        otpEditText = findViewById(R.id.otp_edit_id);
        otpSendButton = findViewById(R.id.send_otp_btn_id);
        confirmOtpButton = findViewById(R.id.otp_confirm_button_id);
    }
    private void sendOtp(String number){

    }
    private void confirmOtp(String otp){

    }
    private void beginToListen(){
        otp_receive = new OTP_Receive();
        otp_receive.setOTPListener(this);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsRetriever.SMS_RETRIEVED_ACTION);
        this.registerReceiver(otp_receive, intentFilter);

        SmsRetrieverClient client = SmsRetriever.getClient(this /* context */);
        Task<Void> task = client.startSmsRetriever();

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    @Override
    public void onOTPReceived(String otp) {
        try {
            otpEditText.setText(otp);
            confirmOtp(otp);
            if (otp_receive != null) {
                unregisterReceiver(otp_receive);
                otp_receive= null;
            }
        }catch (Exception e){
            Log.e("exception",e.toString());
        }
    }
}