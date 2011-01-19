package com.encryptit.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.encryptit.exceptions.FileTooBigException;
import com.encryptit.util.KeyTools;

public class EncryptedEditActivity extends Activity {
	private static final String LOG_TAG = EncryptedEditActivity.class.getName();
	
	private static String mFileName = null;
	private static String mKeyName = null;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);
		
		//Get file name and key file name from DiscreteEditText
		Intent startThis = this.getIntent();
		if(startThis.hasExtra(getString(R.string.NAME_OF_FILE))){
			mFileName = startThis.getStringExtra(getString(R.string.NAME_OF_FILE));
			if(startThis.hasExtra(getString(R.string.NAME_OF_KEY))){
				mKeyName = startThis.getStringExtra(getString(R.string.NAME_OF_KEY));
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
		//Initialize Widgets
		final EditText notePad = (EditText) findViewById(R.id.NotePad);
		final Button saveButton = (Button) findViewById(R.id.SaveButton);
		final Button discardButton = (Button) findViewById(R.id.DiscardButton);
		
		//Initialize key tools, decrypt text of encrypted file for view/edit
		KeyTools kTool = new KeyTools();
		loadEncryptedFile(mFileName, kTool.getKey(mKeyName, LOG_TAG), notePad);
		
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				
			}
		});
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

				String ciphertext = new String(encrypted);
				
				notePad.setText(ciphertext);
				notePad.invalidate();
			} catch (NoSuchPaddingException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (NoSuchAlgorithmException e) {
				Log.e(LOG_TAG, e.getMessage());
			} catch (InvalidKeyException e) {
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
	