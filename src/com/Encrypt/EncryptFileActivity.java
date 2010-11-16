package com.Encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import android.app.Activity;
import android.content.Context;
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
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encrypt_file);
        
        final EditText fileNameEdit = (EditText) findViewById(R.id.FileNameEdit);
        final Spinner encryptionSelect = (Spinner) findViewById(R.id.EncryptSelect);
        Button encryptItButton = (Button) findViewById(R.id.EncryptItButton);
        final EditText seedEdit = (EditText) findViewById(R.id.seedEdit);
        
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.EncryptSelectOptions, 
            android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encryptionSelect.setAdapter(spinnerAdapter);
        
        final Context encryptFileActivity = this;
        
        encryptItButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View encryptView) {
            String fileName = fileNameEdit.getEditableText().toString();
            String encryptionType = encryptionSelect.getSelectedItem().toString();
            String seed = seedEdit.getEditableText().toString();
            Cipher encCipher = null;
            Cipher decCipher = null;
            try{
              SecretKey key = KeyGenerator.getInstance(encryptionType).generateKey();
              encCipher = Cipher.getInstance(encryptionType);
              encCipher.init(Cipher.ENCRYPT_MODE, key);
              encryptFile(fileName, "/mnt/sdcard/test.enc", encCipher);
              //decCipher.init(Cipher.DECRYPT_MODE, key);
            } catch (NoSuchPaddingException e) {
              Log.e(LOG_TAG, e.getMessage());
            } catch (NoSuchAlgorithmException e) { 
              Log.e(LOG_TAG, e.getMessage());
            } catch (InvalidKeyException e) { 
              Log.e(LOG_TAG, e.getMessage());
            } 
          }
        });
    }
    
    private void encryptFile(String fileInName, String fileOutName, Cipher encCipher){
      FileInputStream fileReadStream = null;
      FileOutputStream fileWriteStream = null;
      try{
        fileReadStream = new FileInputStream("/mnt/sdcard/" + fileInName);
        fileWriteStream = new FileOutputStream(new File(fileOutName));
        
        long fileLen = fileInName.length();
        //if(fileLen > Integer.MAX_VALUE) return null; //Replace with custom exception
        byte[] fileBytes = new byte[(int)fileLen];
        
        int offset = 0;
        int read = 0;
        while(offset < fileBytes.length 
         && (read=fileReadStream.read(fileBytes, offset, fileBytes.length-offset)) >= 0){
          offset += read;
        }
        
        if (offset < fileBytes.length) {
          throw new IOException("Could not completely read file "+ fileInName);
        }
        
        byte[] enc = encCipher.doFinal(fileBytes);
        fileWriteStream.write(enc);
        
        Toast.makeText(this, "Encrypted \'" + fileInName + "\'.", Toast.LENGTH_SHORT);
      } catch(BadPaddingException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(UnsupportedEncodingException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(IllegalBlockSizeException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(IOException ioe){
        Log.e(LOG_TAG, ioe.getMessage());
        Toast.makeText(this, "Was unable to encrypt file \'" + fileInName + "\'.", 
            Toast.LENGTH_SHORT);
      } finally{
        if (fileReadStream != null){
          try {
            fileReadStream.close();
          } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
          }
        }
        if (fileWriteStream != null){
          try {
            fileWriteStream.close();
          } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage());
          }
        }
      }
    }
}