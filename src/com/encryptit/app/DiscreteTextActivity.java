package com.encryptit.app;

import android.app.Activity;
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

public class DiscreteTextActivity extends Activity {
	private static final String LOG_TAG = DiscreteTextActivity.class.getName();
	private static final int PATH_TO_FILE = 0; // Returning path to file to
	// decrypt?
	private static final int PATH_TO_KEY_FILE = 1; // or returning path to key
	// file?
	
	private static final int RETURN_PATH_TO_LOAD = 83;
	private final String SELECTED_PATH = "selected.path";
	private final String SELECTED_TYPE = "selected.type";
	
	private final String NAME_OF_FILE = "name.file"; //Name of file to be sent to EncryptedEdit
	private final String NAME_OF_KEY = "name.key"; //Name of key to be sent to EncryptedEdit
	
	private static EditText mFileNameEdit;
	private static EditText mKeyNameEdit;
	private static Activity mDiscreteThis;
	private static SharedPreferences mPreferences;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discrete_text);
		
		mFileNameEdit = (EditText) findViewById(R.id.FileNameEdit);
		mKeyNameEdit = (EditText) findViewById(R.id.KeyNameEdit);
		final Button keyBrowseButton = (Button) findViewById(R.id.BrowseKeyButton);
		final Button fileBrowseButton = (Button) findViewById(R.id.BrowseButton);
		Button readItButton = (Button) findViewById(R.id.ReadItButton);

		mDiscreteThis = this;
		
		fileBrowseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Intent fileViewIntent = new Intent(DiscreteTextActivity.this,
						FileListActivity.class);
				fileViewIntent.putExtra(SELECTED_TYPE, PATH_TO_FILE);
				startActivityForResult(fileViewIntent, RETURN_PATH_TO_LOAD);
			}
		});

		keyBrowseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Intent fileViewIntent = new Intent(DiscreteTextActivity.this,
						FileListActivity.class);
				fileViewIntent.putExtra(SELECTED_TYPE, PATH_TO_KEY_FILE);
				startActivityForResult(fileViewIntent, RETURN_PATH_TO_LOAD);
			}
		});
		
		readItButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Intent encryptEditIntent = new Intent(DiscreteTextActivity.this,
						EncryptedEditActivity.class);
				encryptEditIntent.putExtra(NAME_OF_FILE, mFileNameEdit.getEditableText());
				encryptEditIntent.putExtra(NAME_OF_KEY, mKeyNameEdit.getEditableText());
				startActivity(encryptEditIntent);
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
									mDiscreteThis,
									"Unknown error in returning type from file viewer.  Please contact the developer",
									Toast.LENGTH_SHORT).show();
						}
					}
				}
				break;
			}
		}
	}

	private class FileTooBigException extends Exception {
		static final long serialVersionUID = 1877;

		FileTooBigException(String msg) {
			super(msg);
		}
	}

	private static class KeyGenFailException extends Exception {
		static final long serialVersionUID = 1878;

		public enum failureTypes {
			KEY_NULL, DATABASE_ERROR, EMPTY_FIELD
		}

		private failureTypes fail;

		KeyGenFailException(failureTypes failureType) {
			fail = failureType;
		}
	}
}