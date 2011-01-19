package com.encryptit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.encryptit.app.EncryptFileActivity;
import com.encryptit.app.R;
import com.encryptit.exceptions.FileTooBigException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class EncryptedEditActivity extends Activity {
	private static final String LOG_TAG = EncryptedEditActivity.class.getName();
	private static SharedPreferences mPreferences;
	
	private static String mFileName = null;
	private static String mKeyName = null;
	
	private final String NAME_OF_FILE = "name.file"; //Name of file to be sent to EncryptedEdit
	private final String NAME_OF_KEY = "name.key"; //Name of key to be sent to EncryptedEdit
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		
		//Get file name and key file name from DiscreteEditText
		Intent startThis = this.getIntent();
		if(startThis.hasExtra(NAME_OF_FILE)){
			mFileName = startThis.getStringExtra(NAME_OF_FILE);
			if(startThis.hasExtra(NAME_OF_KEY)){
				mKeyName = startThis.getStringExtra(NAME_OF_KEY);
			}  else{
				String errorMsg = "EncryptEditActivity has not recieved the name of the file to be edited from "
					+ "DiscreteTextActivity.";
				Log.e(LOG_TAG, errorMsg);
				Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
				this.finish();
			}
		} else{
			String errorMsg = "EncryptEditActivity has not recieved the name of the file to be edited from "
				+ "DiscreteTextActivity.";
			Log.e(LOG_TAG, errorMsg);
			Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
			this.finish();
		}
		
		final EditText notePad = (EditText) findViewById(R.id.NotePad);
		
		loadEncryptedFile(mFileName, mKeyName, notePad);
	}

	private void loadEncryptedFile(String fileInName, SecretKeySpec key, EditText notePad) {
		try {
			//TODO make encryption type editable by user, or drawn from key file
			try {
				Cipher decCipher = Cipher.getInstance("AES");
				decCipher.init(Cipher.DECRYPT_MODE, key);

				File fileIn = new File(fileInName);
				if (fileIn.length() > Integer.MAX_VALUE)
					throw new FileTooBigException("File " + fileInName
							+ " is too large too decrypt.");

				FileInputStream input = new FileInputStream(fileIn);
				byte[] encrypted = new byte[(int) fileIn.length()];
				input.read(encrypted);

				byte[] decrypt = decCipher.doFinal(encrypted);

				FileOutputStream output = new FileOutputStream(new File(outputName));
				output.write(decrypt);

				Toast.makeText(this, "Decrypted \"" + outputName + "\".",
						Toast.LENGTH_SHORT).show();
			} catch (NoSuchPaddingException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (InvalidKeyException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (IllegalBlockSizeException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (BadPaddingException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (FileNotFoundException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (IOException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (FileTooBigException e) {
				Log.e(LOG_TAG, e.getMessage());
			}
		}// End loadEncryptedFile
	}
}
	
