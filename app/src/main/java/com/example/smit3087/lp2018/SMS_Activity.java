package com.example.smit3087.lp2018;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class SMS_Activity extends AppCompatActivity {
final static int SMS_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_);
    }
    private void checkForSmsPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS) !=
                PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, getString(R.string.permission_not_granted), Toast.LENGTH_LONG).show();
            requestPermissions( new String[]{Manifest.permission.SEND_SMS}, SMS_REQUEST);

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_REQUEST);
        } else {
            // Permission already granted. Enable the message button.

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case SMS_REQUEST: {
                if (permissions[0].equalsIgnoreCase(Manifest.permission.SEND_SMS)
                        && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted.
                } else {
                    // Permission denied.
                    Toast.makeText(MainActivity.this,
                            getString(R.string.failure_permission),
                            Toast.LENGTH_SHORT).show();
                    // Disable the message button.
                    disableSmsButton();
                }
            }
        }
    }
    public void disableSmsButton() {//Add functionality

    }


}
