package com.Encrypt;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class EncryptActivity extends Activity {
    private static final String LOG_TAG = EncryptActivity.class.getName();
    private static SharedPreferences mPreferences = null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        if(mPreferences == null){
          firstRunPreferences();
        }
        
        Button encryptStringButton = (Button) findViewById(R.id.EncryptStringButton);
        Button encryptFileButton = (Button) findViewById(R.id.EncryptFileButton);
        Button decryptFileButton = (Button) findViewById(R.id.DecryptFileButton);
       
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
        
        decryptFileButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View v) {
            Intent decFileIntent = new Intent(EncryptActivity.this, DecryptFileActivity.class);
            startActivity(decFileIntent);
          }
        });
    }
    
    public boolean getIsFirstRun(){
      return mPreferences.getBoolean("firstRun", true);
    }
    
    private void setRanForFirstTime(){
      SharedPreferences.Editor edit = mPreferences.edit();
      edit.putBoolean("firstRun", false);
      edit.commit();
    }
    
    private void firstRunPreferences(){
      Context thisContext = this.getApplicationContext();
      mPreferences = thisContext.getSharedPreferences("User Preferences", 0);
      
      //PLACE ACTIVITY FOR USER TO SELECT DIRECTORY PLACEMENT HERE
      //REPLACE ABOVE CODE WITH THE CODE BELOW TO BUILD DIRECTORIES AND EDIT CONFIG
      
      //Build directories for encrypted files, decrypted files, and keys
      SharedPreferences.Editor edit = mPreferences.edit();
      
      File encryptedDir = new File(
          Environment.getExternalStorageDirectory().toString() + "/Encrypt-IT/EncryptedFiles");
      encryptedDir.mkdirs();
      edit.putString("encryptedDir", encryptedDir.getAbsolutePath());
      
      File decryptedDir = new File(
          Environment.getExternalStorageDirectory().toString() + "/Encrypt-IT/DecryptedFiles");
      decryptedDir.mkdirs();
      edit.putString("decryptedDir", decryptedDir.getAbsolutePath());
      
      File keyDir = new File(
          Environment.getExternalStorageDirectory().toString() + "/Encrypt-IT/Keys");
      keyDir.mkdirs();
      edit.putString("keyDir", keyDir.getAbsolutePath());

      //Write user preferences to database
      edit.commit();
      
      setRanForFirstTime();
      
      Toast.makeText(this, "Program has run for first time!", Toast.LENGTH_SHORT).show();
    }
}