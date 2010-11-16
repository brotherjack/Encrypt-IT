package com.Encrypt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EncryptActivity extends Activity {
    private static final String LOG_TAG = EncryptActivity.class.getName();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
       
        Button encryptStringButton = (Button) findViewById(R.id.EncryptStringButton);
        Button encryptFileButton = (Button) findViewById(R.id.EncryptFileButton);
       
        encryptStringButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View encryptView) {
            Intent encStrIntent = new Intent(EncryptActivity.this, EncryptStringActivity.class);
            startActivity(encStrIntent);
          }
        });
        
        encryptFileButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View v) {
            Intent encFileIntent = new Intent(EncryptActivity.this, EncryptFileActivity.class);
            startActivity(encFileIntent);
          }
        });
    }
}