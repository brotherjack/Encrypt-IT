package com.encryptit.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

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

import com.encryptit.exceptions.FileTooBigException;
import com.encryptit.exceptions.KeyGenFailException;

import com.encryptit.util.KeyTools;

public class EncryptFileActivity extends Activity {
	private static final String LOG_TAG = EncryptFileActivity.class.getName();
	private static SharedPreferences mPreferences;
	private static Button mBrowseButton;
	private static EditText mFileNameEdit;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.encrypt_file);

		mFileNameEdit = (EditText) findViewById(R.id.FileNameEdit);
		final Spinner encryptionSelect = (Spinner) findViewById(R.id.EncryptSelect);
		mBrowseButton = (Button) findViewById(R.id.BrowseButton);
		Button encryptItButton = (Button) findViewById(R.id.EncryptItButton);
		final EditText seedEdit = (EditText) findViewById(R.id.seedEdit);
		final CheckBox inPlace = (CheckBox) findViewById(R.id.EncInPlaceCheck);

		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.EncryptSelectOptions,
						android.R.layout.simple_spinner_item);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		encryptionSelect.setAdapter(spinnerAdapter);

		final Context encryptFileActivity = this;

		//If called from EncryptedEditActivity, automatically insert file to be re-encrypted
		//if(this.getIntent().hasExtra(name)){
		//TODO this	
		//}
		
		encryptItButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				String fileName = mFileNameEdit.getEditableText().toString();
				String encryptionType = encryptionSelect.getSelectedItem()
						.toString();
				String seed = seedEdit.getEditableText().toString();

				Context thisContext = encryptFileActivity
						.getApplicationContext();
				mPreferences = thisContext.getSharedPreferences(
						"User Preferences", 0);
				String fileOut = "";

				try {
					String keyDir = mPreferences.getString("keyDir", null);
					String encryptedDir = mPreferences.getString(
							"encryptedDir", null);
					String newFileName;

					if ((keyDir == null) || (encryptedDir == null)) {
						throw new KeyGenFailException(
								KeyGenFailException.failureTypes.DATABASE_ERROR);
					} else if (fileName.compareTo("") == 0) {
						throw new KeyGenFailException(
								KeyGenFailException.failureTypes.EMPTY_FIELD);
					}
					
					String fileIn = fileName;
					Boolean isInPlace = false;
					if(inPlace.isChecked()){
						isInPlace = true;
					} else {
						newFileName = makeRandomFileName();
						fileOut = encryptedDir + "/" + newFileName;
					}
					
					String keyFileName = keyDir + "/" + fileName;
					KeyTools kTool = new KeyTools();

					SecretKeySpec key = kTool.makeKey(keyFileName, seed, LOG_TAG);
					if (key == null)
						throw new KeyGenFailException(
								KeyGenFailException.failureTypes.KEY_NULL);
					else{						
						kTool.saveKey(key, keyDir, fileName, LOG_TAG, encryptFileActivity);
					}

					encryptFile(fileIn, fileOut, encryptionType, key, isInPlace);
				} catch (KeyGenFailException e) {
					String msg = null;
					if (e.fail == KeyGenFailException.failureTypes.KEY_NULL) {
						msg = "Was unable to generate a key!";
						Log.e(LOG_TAG, msg);
						Toast.makeText(encryptFileActivity, msg,
								Toast.LENGTH_SHORT).show();
					}
					if (e.fail == KeyGenFailException.failureTypes.DATABASE_ERROR) {
						msg = "Corrupted value for directory in databse.";
						Log.e(LOG_TAG, msg);
						Toast.makeText(encryptFileActivity, msg,
								Toast.LENGTH_SHORT).show();
					}
					if (e.fail == KeyGenFailException.failureTypes.EMPTY_FIELD) {
						msg = "Please enter a file name to encrypt.";
						Toast.makeText(encryptFileActivity, msg,
								Toast.LENGTH_SHORT).show();
					}
					Toast.makeText(
							encryptFileActivity,
							"Successfuly encrypted \"" + fileName + "\" as \""
									+ " \"" + fileOut + " \" ",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		mBrowseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Intent fileViewIntent = new Intent(EncryptFileActivity.this,
						FileListActivity.class);
				startActivityForResult(fileViewIntent, R.id.return_path_to_load);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == R.id.return_path_to_load) {
			switch (resultCode) {
			case (Activity.RESULT_OK): // If path was returned successfully
				if (resultCode == Activity.RESULT_OK) {
					if (data.hasExtra(getString(R.string.SELECTED_PATH))) {
						Bundle stuff = data.getExtras();
						String path = stuff.getString(getString(R.string.SELECTED_PATH));
						mFileNameEdit.setText(path);
						Log.i(LOG_TAG, "Placed path named \"" + path
								+ "\" into FileNameEdit, EditText.");
					}
				}
				break;
			}
		}
	}

	public static String makeRandomFileName() {
		// Create random value generator
		Random gen = new Random(System.currentTimeMillis());

		// A character in java is two bytes, so this will be a string between 1
		// and 15 characters long
		int stringSize = 1 + gen.nextInt(15) * 2;

		gen.setSeed(System.currentTimeMillis()); // Set to new seed
		String output = "";
		int nInt = 0;
		for (int i = 0; i < stringSize; i++) {
			nInt = gen.nextInt(75) + 48;
			while (nInt == 33 || nInt == 34 || nInt == 124 || nInt == 92
					|| nInt == 62 || nInt == 63 || nInt == 42 || nInt == 58
					|| nInt == 43 || nInt == 91 || nInt == 93 || nInt == 47
					|| nInt == 39) // A reserved character ->  "|\\?*<\":>+[]/'"
				nInt = gen.nextInt(75) + 48;
			output += String.valueOf((char) nInt);
		}
		return output;
	}

	private void encryptFile(String fileInName, String fileOutName,
			String encryptionType, SecretKeySpec key, boolean isInPlace) {
		FileInputStream input = null;
		FileOutputStream output = null;
		Cipher encCipher = null;
		File fileInPlace = null; //To be used to rename the file to random, if user wants it encrypted in place
		try {
			encCipher = Cipher.getInstance(encryptionType);
			encCipher.init(Cipher.ENCRYPT_MODE, key);
			File fileIn = new File(fileInName);
			if (fileIn.length() > Integer.MAX_VALUE)
				throw new FileTooBigException("File\'" + fileInName
						+ "\' is too large too encrypt.");

			input = new FileInputStream(fileIn);
			byte[] plainText = new byte[(int) fileIn.length()];
			input.read(plainText);

			byte[] encrypted = encCipher.doFinal(plainText);

			if(isInPlace){ //User wants to encrypt the file in its present location
				fileInPlace = new File(fileInName);
				output = new FileOutputStream(fileInPlace);
			} else {
				output = new FileOutputStream(new File(fileOutName + ".enc"));
			}
			output.write(encrypted);
			
			if(isInPlace){
				Pattern lastBranch = Pattern.compile("[a-zA-Z0-9-|?*<\":>+.'_ ]*$");
				Matcher matcher = lastBranch.matcher(fileInName);
				matcher.find();
				
				String pathTo = (String)fileInName.subSequence(0, matcher.start()-1);
				String encryptedName = pathTo.concat(makeRandomFileName());
				encryptedName.concat(".enc");
				
				fileInPlace.renameTo(new File(encryptedName));
				
				Toast.makeText(this, "Encrypted \'" + fileInName + "\'.",
						Toast.LENGTH_LONG).show();
			} else{
				Toast.makeText(this, "Encrypted \'" + fileInName + "\' as " + fileOutName + ".enc.",
						Toast.LENGTH_LONG).show();
			}
		} catch (BadPaddingException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (UnsupportedEncodingException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (IllegalBlockSizeException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (FileTooBigException e) {
			Log.e(LOG_TAG, e.getMessage());
			Toast.makeText(this, "Was unable to encrypt file \'" + fileInName
					+ "\' is too large too encrypt.", Toast.LENGTH_SHORT);
		} catch (IOException ioe) {
			Log.e(LOG_TAG, ioe.getMessage());
			Toast.makeText(this, "Was unable to encrypt file \'" + fileInName
					+ "\'.", Toast.LENGTH_SHORT);
		} catch (NoSuchAlgorithmException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (NoSuchPaddingException e) {
			Log.e(LOG_TAG, e.getMessage());
		} catch (InvalidKeyException e) {
			Log.e(LOG_TAG, e.getMessage());
		} finally {
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, "Cannot close input stream!");
					e.printStackTrace();
				}
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					Log.e(LOG_TAG, "Cannot close output stream!");
					e.printStackTrace();
				}
		}
	}// End encryptFile
}
