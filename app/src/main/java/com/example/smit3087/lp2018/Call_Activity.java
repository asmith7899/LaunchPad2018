package com.example.smit3087.lp2018;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class Call_Activity extends AppCompatActivity {
    TelephonyManager mTel;
    private static final int PHONE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_);
        mTel = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        if (isTelEnabled()) {
            checkForPhonePermission();
        }
    }

    private boolean isTelEnabled() {
        if (mTel != null && mTel.getSimState() == TelephonyManager.SIM_STATE_READY) {
            return true;
        }
        return false;
    }

    private void checkForPhonePermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
            // Permission not yet granted. Use requestPermissions().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    PHONE_REQUEST);
        } else {
            // Permission already granted.
        }

    }
    @Override
public void onRequestPermissionsResult(int requestCode,
                   String permissions[], int[] grantResults) {
   switch (requestCode) {
       case PHONE_REQUEST: {
           if (permissions[0].equalsIgnoreCase
                       (Manifest.permission.CALL_PHONE)
                       && grantResults[0] ==
                       PackageManager.PERMISSION_GRANTED) {
               // Permission was granted.
           } else {
               // Permission denied
               Toast.makeText(this,
                           getString(R.string.failure_permission),
                           Toast.LENGTH_SHORT).show();
               // Disable the call button
               disableCallButton();
           }
       }
   }
}
private class phoneCallListener extends PhoneStateListener {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                // Incoming call is ringing.
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                // Phone call is active -- off the hook.
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                // Phone is idle before and after phone call.
                break;
            default:
                // Must be an error. Raise an exception or just log it.
                break;
        }
    }
}
public void disableCallButton() { //I'm thinking that this method should send us back to main - maybe with a message

}
}
