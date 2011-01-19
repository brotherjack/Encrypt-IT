package com.encryptit.app;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.encryptit.app.R;
import com.encryptit.exceptions.FileTooBigException;
import com.encryptit.util.KeyTools;

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
		KeyTools kTool = new KeyTools();
		
		loadEncryptedFile(mFileName, kTool.getKey(mKeyName, LOG_TAG), notePad);
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

				CipherInputStream input = new CipherInputStream(new FileInputStream(fileIn), decCipher);
				byte[] encrypted = new byte[(int) fileIn.length()];
				input.read(encrypted);

				byte[] decrypt = decCipher.doFinal(encrypted);
				String ciphertext = new String(decrypt);
				
				notePad.setText(ciphertext);
				notePad.invalidate();
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
			} finally{ }
		}finally{}
	}// End loadEncryptedFile
}
	