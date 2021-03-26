package com.mahmudjerrry.automaticsmsverification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements OTP_Receive.OTPReceiveListener {
    private EditText numberEditText , otpEditText ;
    private Button otpSendButton ,confirmOtpButton ;
    private OTP_Receive otp_receive;
    private static MainActivity ins ;
    ProgressDialog progressDialog ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //finding elements
        numberEditText = findViewById(R.id.number_edit_id);
        otpEditText = findViewById(R.id.otp_edit_id);
        otpSendButton = findViewById(R.id.send_otp_btn_id);
        confirmOtpButton = findViewById(R.id.otp_confirm_button_id);

        otpSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(numberEditText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Insert Number!", Toast.LENGTH_SHORT).show();
                }else{
                    sendOtp(numberEditText.getText().toString());
                }
            }
        });
        confirmOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(otpEditText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this, "Please Insert your Code!", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(MainActivity.this, "Further post request for otp confirmation", Toast.LENGTH_SHORT).show();
                }
            }
        });
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        //get hash code for your app
        MainActivity.AppSignatureHelper helper = new AppSignatureHelper(this);
        Log.e("hashcode",helper.getAppSignatures().toString());

    }
    private void sendOtp(String number){
        progressDialog.show();
        beginToListen();
        //make your request to the server
        //with the string number
        //then your server will send to the number
        //desired code and also with 11 digit hash code
        //there will request to the server as well as
        //begin to listen will wait for 5 minutes if any sms come
        //to the devices .

        //when you send the code from server
        //you will also save the code to server
        //for further confirmation of OTP
    }
    private void confirmOtp(String otp){
        //There will have a post request to your server
        //to check if the desire code is correct or not .
        // you can use volley , retrofit etc to make a post request .
    }
    private void beginToListen(){

        //this portion of code is to retrive code from on onReceived broadcast reciever
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
                    Log.v("ListenerSuccess",aVoid.toString());
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Listener Failed",e.toString());
            }
        });
    }

    @Override
    public void onOTPReceived(String otp) {
        progressDialog.dismiss();
        Toast.makeText(MainActivity.this, "Receved OTP , Make post request for further confirmation", Toast.LENGTH_SHORT).show();
        try {
            otpEditText.setText(otp);
            //now send the otp and number to check otp
            confirmOtp(otp);
            if (otp_receive != null) {
                unregisterReceiver(otp_receive);
                otp_receive= null;
            }
        }catch (Exception e){
            Log.e("exception",e.toString());
        }
    }
    /**
     * This is a helper class to generate your message hash to be included in your SMS message.
     *
     * Without the correct hash, your app won't recieve the message callback. This only needs to be
     * generated once per app and stored. Then you can remove this helper class from your code.
     */
    public class AppSignatureHelper extends ContextWrapper {
        public  final String TAG = AppSignatureHelper.class.getSimpleName();

        private static final String HASH_TYPE = "SHA-256";
        public static final int NUM_HASHED_BYTES = 9;
        public static final int NUM_BASE64_CHAR = 11;

        public AppSignatureHelper(Context context) {
            super(context);
        }

        /**
         * Get all the app signatures for the current package
         * @return
         */
        public ArrayList<String> getAppSignatures() {
            ArrayList<String> appCodes = new ArrayList<>();

            try {
                // Get all package signatures for the current package
                String packageName = getPackageName();
                PackageManager packageManager = getPackageManager();
                Signature[] signatures = packageManager.getPackageInfo(packageName,
                        PackageManager.GET_SIGNATURES).signatures;

                // For each signature create a compatible hash
                for (Signature signature : signatures) {
                    String hash = hash(packageName, signature.toCharsString());
                    if (hash != null) {
                        appCodes.add(String.format("%s", hash));
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Unable to find package to obtain hash.", e);
            }
            return appCodes;
        }

        private  String hash(String packageName, String signature) {
            String appInfo = packageName + " " + signature;
            try {
                MessageDigest messageDigest = MessageDigest.getInstance(HASH_TYPE);
                messageDigest.update(appInfo.getBytes(StandardCharsets.UTF_8));
                byte[] hashSignature = messageDigest.digest();

                // truncated into NUM_HASHED_BYTES
                hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES);
                // encode into Base64
                String base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING | Base64.NO_WRAP);
                base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);

                Log.d(TAG, String.format("pkg: %s -- hash: %s", packageName, base64Hash));
                return base64Hash;
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "hash:NoSuchAlgorithm", e);
            }
            return null;
        }
    }
}