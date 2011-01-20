package com.encryptit.util;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class PreferenceTools {
	private SharedPreferences mPreferences;
	private Context mThisContext;
	private static final String AUTHORITY = Environment
	.getExternalStorageDirectory().toString() + "/Encrypt-IT/";
	
	public PreferenceTools(Context con){
		mThisContext = con;
		mPreferences = mThisContext.getSharedPreferences("User Preferences", 0);
	}
	
	public SharedPreferences getSharedPref(){ return mPreferences; }

	public boolean getIsFirstRun() {
		return mPreferences.getBoolean("firstRun", true);
	}

	private void setRanForFirstTime() {
		SharedPreferences.Editor edit = mPreferences.edit();
		edit.putBoolean("firstRun", false);
		edit.commit();
	}

	public void firstRunPreferences() {
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
	}
}