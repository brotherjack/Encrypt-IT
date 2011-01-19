package com.encryptit.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

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
import android.os.Environment;
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
import com.encryptit.exceptions.ReadAndWriteFileException;
import com.encryptit.util.KeyTools;

public class DecryptFileActivity extends Activity {
	private static final String LOG_TAG = EncryptFileActivity.class.getName();
	private static final int PATH_TO_FILE = 0; // Returning path to file to
	// decrypt?
	private static final int PATH_TO_KEY_FILE = 1; // or returning path to key
	// file?
	
	private static final int RETURN_PATH_TO_LOAD = 83;
	private final String SELECTED_PATH = "selected.path";
	private final String SELECTED_TYPE = "selected.type";

	private static EditText mFileNameEdit;
	private static EditText mKeyNameEdit;
	private static EditText mDecryptNameEdit;
	private static Activity mDecryptThis;
	private static SharedPreferences mPreferences;

	public static String mOutput = ""; // To determine the decrypted file name

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.decrypt_file);

		mFileNameEdit = (EditText) findViewById(R.id.FileNameEdit);
		mKeyNameEdit = (EditText) findViewById(R.id.KeyNameEdit);
		mDecryptNameEdit = (EditText) findViewById(R.id.DecryptNameEdit);
		final Spinner encryptionSelect = (Spinner) findViewById(R.id.EncryptSelect);
		final Button keyBrowseButton = (Button) findViewById(R.id.BrowseKeyButton);
		final Button fileBrowseButton = (Button) findViewById(R.id.BrowseButton);
		final CheckBox inPlace = (CheckBox) findViewById(R.id.DecInPlaceCheck);
		Button decryptItButton = (Button) findViewById(R.id.DecryptItButton);

		ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter
				.createFromResource(this, R.array.EncryptSelectOptions,
						android.R.layout.simple_spinner_item);
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		encryptionSelect.setAdapter(spinnerAdapter);

		mDecryptThis = this;
		
		inPlace.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				if(inPlace.isChecked()){
					mDecryptNameEdit.setEnabled(false);
				} else {
					mDecryptNameEdit.setEnabled(true);
				}
			}
		});
			

		decryptItButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Context thisContext = mDecryptThis.getApplicationContext();
				mPreferences = thisContext.getSharedPreferences(
						"User Preferences", 0);
				String decryptPath = mPreferences.getString("decryptedDir", null);

				String outputName = mDecryptNameEdit.getEditableText()
						.toString();
				if(outputName.equals("")){
					outputName = "default";
				}
				String encryptionType = encryptionSelect.getSelectedItem()
						.toString();
				String keyFileName = mKeyNameEdit.getEditableText().toString();
				boolean sdcardAccessible = true;
				try {
					sdcardAccessible = canReadAndWrite();
					if (sdcardAccessible){
						boolean decInPlace = false;
						if(inPlace.isChecked()){
							decInPlace = true;	
						}
						DecryptFile(encryptionType, keyFileName,
									outputName, decryptPath, decInPlace);
					}
				} catch (ReadAndWriteFileException rawfe) {
					Toast.makeText(mDecryptThis, rawfe.getMessage(),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		fileBrowseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Intent fileViewIntent = new Intent(DecryptFileActivity.this,
						FileListActivity.class);
				fileViewIntent.putExtra(SELECTED_TYPE, PATH_TO_FILE);
				startActivityForResult(fileViewIntent, RETURN_PATH_TO_LOAD);
			}
		});

		keyBrowseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Intent fileViewIntent = new Intent(DecryptFileActivity.this,
						FileListActivity.class);
				fileViewIntent.putExtra(SELECTED_TYPE, PATH_TO_KEY_FILE);
				startActivityForResult(fileViewIntent, RETURN_PATH_TO_LOAD);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RETURN_PATH_TO_LOAD) {
			switch (resultCode) {
			case (Activity.RESULT_OK): // If path was returned successfully
				if (resultCode == Activity.RESULT_OK) {
					if (data.hasExtra(SELECTED_PATH)) {
						Bundle stuff = data.getExtras();
						String path = stuff.getString(SELECTED_PATH);
						int type = stuff.getInt(SELECTED_TYPE);
						if (type == PATH_TO_FILE) {
							mFileNameEdit.setText(path);
							Log.i(LOG_TAG, "Placed path named \"" + path
									+ "\" into FileNameEdit, EditText.");
						} else if (type == PATH_TO_KEY_FILE) {
							mKeyNameEdit.setText(path);
							Log.i(LOG_TAG, "Placed path named \"" + path
									+ "\" into KeyNameEdit, EditText.");
						} else {
							Log.e(LOG_TAG,
									"Unknown error in finding type to return from file viewer.");
							Toast.makeText(
									mDecryptThis,
									"Unknown error in returning type from file viewer.  Please contact the developer",
									Toast.LENGTH_SHORT).show();
						}
					}
				}
				break;
			}
		}
	}

	private boolean canReadAndWrite() throws ReadAndWriteFileException {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			return true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// Read only
			throw new ReadAndWriteFileException(
					"Sdcard is mounted as read-only.");
		} else {
			throw new ReadAndWriteFileException(
					"Sdcard is unavailable for read or write operations.");
		}
	}

	private void DecryptFile(String encryptionType, String keyFileName, 
			String outputName, String decryptPath, boolean inPlace) {
		try {
			Cipher decCipher = Cipher.getInstance(encryptionType);
			KeyTools kTool = new KeyTools();
			SecretKeySpec key = kTool.getKey(mKeyNameEdit.getEditableText()
					.toString(), LOG_TAG);
			decCipher.init(Cipher.DECRYPT_MODE, key);

			String filePath = mFileNameEdit.getEditableText().toString();
			File fileIn = new File(filePath);
			if (fileIn.length() > Integer.MAX_VALUE)
				throw new FileTooBigException("File " + filePath
						+ " is too large too encrypt.");

			FileInputStream input = new FileInputStream(fileIn);
			byte[] encrypted = new byte[(int) fileIn.length()];
			input.read(encrypted);

			byte[] decrypt = decCipher.doFinal(encrypted);

			if (outputName.equals("")) {
				outputName = EncryptFileActivity.makeRandomFileName();
			} else if(inPlace){
				outputName = filePath;
			} else {
				outputName = decryptPath + "/" + outputName;
			}

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

	}
}
