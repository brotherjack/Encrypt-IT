package com.encryptit.app;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class DiscreteTextActivity extends Activity {
	private static final String LOG_TAG = DiscreteTextActivity.class.getName();
	
	private static EditText mFileNameEdit;
	private static EditText mKeyNameEdit;
	private static Activity mDiscreteThis;
	
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
				fileViewIntent.putExtra(getString(R.string.SELECTED_TYPE), R.id.path_to_file);
				startActivityForResult(fileViewIntent, R.id.return_path_to_load);
			}
		});

		keyBrowseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Intent fileViewIntent = new Intent(DiscreteTextActivity.this,
						FileListActivity.class);
				fileViewIntent.putExtra(getString(R.string.SELECTED_TYPE), R.id.path_to_key_file);
				startActivityForResult(fileViewIntent, R.id.return_path_to_load);
			}
		});
		
		readItButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View encryptView) {
				Intent encryptEditIntent = new Intent(DiscreteTextActivity.this,
						EncryptedEditActivity.class);
				encryptEditIntent.putExtra(getString(R.string.NAME_OF_FILE), mFileNameEdit.getText().toString());
				encryptEditIntent.putExtra(getString(R.string.NAME_OF_KEY), mKeyNameEdit.getText().toString());
				startActivity(encryptEditIntent);
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
						int type = stuff.getInt(getString(R.string.SELECTED_TYPE));
						if (type == R.id.path_to_file) {
							mFileNameEdit.setText(path);
							Log.i(LOG_TAG, "Placed path named \"" + path
									+ "\" into FileNameEdit, EditText.");
						} else if (type == R.id.path_to_key_file) {
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
}