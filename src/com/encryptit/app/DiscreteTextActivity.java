package com.encryptit.app;

import android.app.Activity;
import android.os.Bundle;

public class DiscreteTextActivity extends Activity {
	private static final String LOG_TAG = DiscreteTextActivity.class.getName();
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.discrete_text);

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