package com.encryptit.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Context;
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
import com.encryptit.util.PreferenceTools;

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
		
		//Get context and activity
		final Context thisCon = this;
		final Activity thisAct = EncryptedEditActivity.this;
		
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				//TODO is this vulnerable?
				/*String notePadText = notePad.getText().toString();
				Intent recryptEditIntent = new Intent(EncryptedEditActivity.this,
						EncryptFileActivity.class);
				recryptEditIntent.putExtra(getString(R.string.NAME_OF_FILE), mFileName);
				recryptEditIntent.putExtra(name, value);
				startActivity(recryptEditIntent);*/
				Random gen = new Random(System.currentTimeMillis());

				// A character in java is two bytes, so this will be a string between 10
				// and 50 characters long
				int stringSize = 10 + gen.nextInt(15) * 2;

				gen.setSeed(System.currentTimeMillis()); // Set to new seed
				String seed = "";
				int nInt = 0;
				for (int i = 0; i < stringSize; i++) {
					nInt = gen.nextInt(75) + 48;
					seed += String.valueOf((char) nInt);
				}
				PreferenceTools pTools = new PreferenceTools(thisCon);
				SecretKeySpec key = EncryptFileActivity.matchKeyToFile(mFileName, "AES", seed, true, pTools.getSharedPref(), thisAct, thisCon);
				//EncryptFileActivity.encryptFile(mFileName, mFileName, "AES", key, true, thisCon);
				saveEncryptedFile(mFileName, key, notePad);
			}
		});
	}

	private void loadEncryptedFile(String fileInName, SecretKeySpec key, EditText notePad) {
			//TODO make encryption type editable by user, or drawn from key file
		CipherInputStream input = null;
		try {
			Cipher decCipher = Cipher.getInstance("AES");
			decCipher.init(Cipher.DECRYPT_MODE, key);

			File fileIn = new File(fileInName);
			if (fileIn.length() > Integer.MAX_VALUE)
				throw new FileTooBigException("File " + fileInName
						+ " is too large too decrypt.");

			input = new CipherInputStream(new FileInputStream(fileIn), decCipher);
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
		} finally{ 
			try {
				input.close();
			} catch (IOException e) {
				Log.w(LOG_TAG, e.getMessage());
			}
		}
	}// End loadEncryptedFile
	
	private void saveEncryptedFile(String fileOutName, SecretKeySpec key, EditText notePad) {
		//TODO make encryption type editable by user, or drawn from key file
	CipherOutputStream output = null;
	try {
		Cipher encCipher = Cipher.getInstance("AES");
		encCipher.init(Cipher.ENCRYPT_MODE, key);

		File fileOut = new File(fileOutName);
		if (fileOut.length() > Integer.MAX_VALUE)
			throw new FileTooBigException("File " + fileOutName
					+ " is too large too decrypt.");

		output = new CipherOutputStream(new FileOutputStream(fileOut), encCipher);
		byte[] encrypted = new byte[(int) fileOut.length()];
		output.write(encrypted);

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
	} finally{ 
		try {
			output.close();
		} catch (IOException e) {
			Log.w(LOG_TAG, e.getMessage());
		}
	}
}// End saveEncryptedFile
}
	