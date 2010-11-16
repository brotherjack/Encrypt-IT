package com.Encrypt;

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
              decCipher.init(Cipher.DECRYPT_MODE, key);
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
    
    private byte[] encryptFile(String toEncrypt, Cipher encCipher){
      try{
        byte[] utf8 = toEncrypt.getBytes("UTF8");
        byte[] enc = encCipher.doFinal(utf8);
        return enc; 
      } catch(BadPaddingException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(UnsupportedEncodingException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(IllegalBlockSizeException e){
        Log.e(LOG_TAG, e.getMessage());
      }
      return null;
    }
}