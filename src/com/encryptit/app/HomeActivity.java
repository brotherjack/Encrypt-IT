package com.encryptit.app;

import java.io.File;

import com.encryptit.app.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.encryptit.util.PreferenceTools;

public class HomeActivity extends Activity {
  private static final String LOG_TAG = HomeActivity.class.getName();

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    
    PreferenceTools pTools = new PreferenceTools(this.getApplicationContext());
    
    if (pTools.getIsFirstRun()) {
      Log.i(LOG_TAG, "First Run: Setting up preferences for phone.");
      pTools.firstRunPreferences();
    }

    final Button encryptStringButton =
        (Button) findViewById(R.id.EncryptStringButton);
    final Button encryptFileButton = (Button) findViewById(R.id.EncryptFileButton);
    final Button decryptFileButton = (Button) findViewById(R.id.DecryptFileButton);
    final Button readFileButton = (Button) findViewById(R.id.ReadFileButton);

    encryptStringButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View encryptView) {
        Intent encStrIntent =
            new Intent(HomeActivity.this, EncryptStringActivity.class);
        startActivity(encStrIntent);
      }
    });

    encryptFileButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent encFileIntent =
            new Intent(HomeActivity.this, EncryptFileActivity.class);
        startActivity(encFileIntent);
      }
    });

    decryptFileButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent decFileIntent =
            new Intent(HomeActivity.this, DecryptFileActivity.class);
        startActivity(decFileIntent);
      }
    });
    
    readFileButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent readFileIntent =
              new Intent(HomeActivity.this, DiscreteTextActivity.class);
          startActivity(readFileIntent);
        }
      });
  }
  /*
  public boolean getIsFirstRun() {
    return mPreferences.getBoolean("firstRun", true);
  }

  private void setRanForFirstTime() {
    SharedPreferences.Editor edit = mPreferences.edit();
    edit.putBoolean("firstRun", false);
    edit.commit();
  }

  private void firstRunPreferences() {
    Context thisContext = this.getApplicationContext();
    mPreferences = thisContext.getSharedPreferences("User Preferences", 0);

    // PLACE ACTIVITY FOR USER TO SELECT DIRECTORY PLACEMENT HERE
    // REPLACE ABOVE CODE WITH THE CODE BELOW TO BUILD DIRECTORIES AND EDIT
    // CONFIG

    // Build directories for encrypted files, decrypted files, and keys
    SharedPreferences.Editor edit = mPreferences.edit();

    File rootDir = new File(AUTHORITY);
    rootDir.mkdirs();
    edit.putString("rootDir", rootDir.getAbsolutePath());

    File encryptedDir = new File(AUTHORITY + "EncryptedFiles");
    encryptedDir.mkdirs();
    edit.putString("encryptedDir", encryptedDir.getAbsolutePath());

    File decryptedDir = new File(AUTHORITY + "DecryptedFiles");
    decryptedDir.mkdirs();
    edit.putString("decryptedDir", decryptedDir.getAbsolutePath());

    File keyDir = new File(AUTHORITY + "Keys");
    keyDir.mkdirs();
    edit.putString("keyDir", keyDir.getAbsolutePath());

    // Write user preferences to database
    edit.commit();

    setRanForFirstTime();
  }*/
}
