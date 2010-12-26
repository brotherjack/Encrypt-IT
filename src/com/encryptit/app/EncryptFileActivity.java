package com.encryptit.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import com.encryptit.app.R;

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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class EncryptFileActivity extends Activity {
  private static final String LOG_TAG = EncryptFileActivity.class.getName();
  private static SharedPreferences mPreferences;
  private static final int RETURN_PATH_TO_LOAD = 83;
  private final String SELECTED_PATH = "selected.path";
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

    ArrayAdapter<CharSequence> spinnerAdapter =
        ArrayAdapter.createFromResource(this, R.array.EncryptSelectOptions,
            android.R.layout.simple_spinner_item);
    spinnerAdapter
        .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    encryptionSelect.setAdapter(spinnerAdapter);

    final Context encryptFileActivity = this;

    encryptItButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View encryptView) {
        String fileName = mFileNameEdit.getEditableText().toString();
        String encryptionType = encryptionSelect.getSelectedItem().toString();
        String seed = seedEdit.getEditableText().toString();

        Context thisContext = encryptFileActivity.getApplicationContext();
        mPreferences = thisContext.getSharedPreferences("User Preferences", 0);

        try {
          String keyDir = mPreferences.getString("keyDir", null);
          String encryptedDir = mPreferences.getString("encryptedDir", null);

          if ((keyDir == null) || (encryptedDir == null)) {
            throw new KeyGenFailException(
                KeyGenFailException.failureTypes.DATABASE_ERROR);
          } else if (fileName.compareTo("") == 0) {
            throw new KeyGenFailException(
                KeyGenFailException.failureTypes.EMPTY_FIELD);
          }

          String fileIn = "/mnt/sdcard/usefulcmds.txt";
          String fileOut = encryptedDir + "/" + fileName;
          String keyFileName = keyDir + "/" + fileName;

          SecretKeySpec key = makeKey(keyFileName, seed);
          if (key == null)
            throw new KeyGenFailException(
                KeyGenFailException.failureTypes.KEY_NULL);
          else
            saveKey(key, keyFileName);

          encryptFile(fileIn, fileOut, encryptionType, key);
        } catch (KeyGenFailException e) {
          String msg = null;
          if (e.fail == KeyGenFailException.failureTypes.KEY_NULL) {
            msg = "Was unable to generate a key!";
            Log.e(LOG_TAG, msg);
            Toast.makeText(encryptFileActivity, msg, Toast.LENGTH_SHORT).show();
          }
          if (e.fail == KeyGenFailException.failureTypes.DATABASE_ERROR) {
            msg = "Corrupted value for directory in databse.";
            Log.e(LOG_TAG, msg);
            Toast.makeText(encryptFileActivity, msg, Toast.LENGTH_SHORT).show();
          }
          if (e.fail == KeyGenFailException.failureTypes.EMPTY_FIELD) {
            msg = "Please enter a file name to encrypt.";
            Toast.makeText(encryptFileActivity, msg, Toast.LENGTH_SHORT).show();
          }
        }
      }
    });

    mBrowseButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View encryptView) {
        Intent fileViewIntent =
            new Intent(EncryptFileActivity.this, FileListActivity.class);
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
              mFileNameEdit.setText(path);
              Log.i(LOG_TAG, "Placed path named \"" + path
                  + "\" into FileNameEdit, EditText.");
            }
          }
          break;
      }
    }
  }

  private SecretKeySpec makeKey(String fileName, String seed) {
    try {
      SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");
      secRand.setSeed(seed.getBytes("UTF-8"));

      byte[] key = (fileName + secRand.toString()).getBytes("UTF-8");
      MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

      key = sha1.digest(key);
      // Reduce key to 128 bytes
      byte[] shorterKey = new byte[16];
      for (int i = 0; i < 16; i++)
        shorterKey[i] = key[i];

      return new SecretKeySpec(shorterKey, "AES");
    } catch (NoSuchAlgorithmException e) {
      Log.e(LOG_TAG, e.getMessage());
    } catch (UnsupportedEncodingException e) {
      Log.e(LOG_TAG, e.getMessage());
    }
    return null;
  }

  private void saveKey(SecretKeySpec key, String fileName) {
    FileOutputStream output = null;
    try {
      File fileOut = new File(fileName + ".key");

      output = new FileOutputStream(fileOut);

      output.write(key.getEncoded());
    } catch (IOException e) {
      Log.e(LOG_TAG, e.getMessage());
    } finally {
      if (output != null) try {
        output.close();
      } catch (IOException e) {
        Log.e(LOG_TAG, "Cannot close output stream!");
        e.printStackTrace();
      }
    }
  }

  private void encryptFile(String fileInName, String fileOutName,
      String encryptionType, SecretKeySpec key) {
    FileInputStream input = null;
    FileOutputStream output = null;
    Cipher encCipher = null;
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

      output = new FileOutputStream(new File(fileOutName + ".enc"));
      output.write(encrypted);

      Toast.makeText(this, "Encrypted \'" + fileInName + "\'.",
          Toast.LENGTH_SHORT).show();
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
      Toast.makeText(this,
          "Was unable to encrypt file \'" + fileInName + "\'.",
          Toast.LENGTH_SHORT);
    } catch (NoSuchAlgorithmException e) {
      Log.e(LOG_TAG, e.getMessage());
    } catch (NoSuchPaddingException e) {
      Log.e(LOG_TAG, e.getMessage());
    } catch (InvalidKeyException e) {
      Log.e(LOG_TAG, e.getMessage());
    } finally {
      if (input != null) try {
        input.close();
      } catch (IOException e) {
        Log.e(LOG_TAG, "Cannot close input stream!");
        e.printStackTrace();
      }
      if (output != null) try {
        output.close();
      } catch (IOException e) {
        Log.e(LOG_TAG, "Cannot close output stream!");
        e.printStackTrace();
      }
    }
  }// End encryptFile

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
