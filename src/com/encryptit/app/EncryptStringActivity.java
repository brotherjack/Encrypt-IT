package com.encryptit.app;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.encryptit.app.R;

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

public class EncryptStringActivity extends Activity {
    private static final String LOG_TAG = EncryptStringActivity.class.getName();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encrypt_string);
        
        final EditText stringEdit = (EditText) findViewById(R.id.StringEdit);
        final Spinner encryptionSelect = (Spinner) findViewById(R.id.EncryptSelect);
        Button encryptItButton = (Button) findViewById(R.id.EncryptItButton);
        //final EditText seedEdit = (EditText) findViewById(R.id.seedEdit);
        
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.EncryptSelectOptions, 
            android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encryptionSelect.setAdapter(spinnerAdapter);
        
        final Context encryptStringActivity = this;
        
        encryptItButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View encryptView) {
            String fileName = stringEdit.getEditableText().toString();
            String encryptionType = encryptionSelect.getSelectedItem().toString();
            //String seed = seedEdit.getEditableText().toString();
            Cipher encCipher;
            Cipher decCipher;
            try{
              SecretKey key = KeyGenerator.getInstance(encryptionType).generateKey();
              encCipher = Cipher.getInstance(encryptionType);
              decCipher = Cipher.getInstance(encryptionType);
              encCipher.init(Cipher.ENCRYPT_MODE, key);
              decCipher.init(Cipher.DECRYPT_MODE, key);
              byte[] encrypted = encryptString(fileName, encCipher);
              String decrypted = decryptString(encrypted, decCipher);
              Toast.makeText(encryptStringActivity, "This is the encrypted string " + new String(encrypted) +
                  "\nThis is the decrypted string " + decrypted, Toast.LENGTH_SHORT).show();
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
    
    private byte[] encryptString(String toEncrypt, Cipher encCipher){
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
    
    private String decryptString(byte[] toDecrypt, Cipher decCipher){
      try{
        byte[] decrypt = decCipher.doFinal(toDecrypt);
        return new String(decrypt, "UTF8");
      } catch(BadPaddingException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(IllegalBlockSizeException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(UnsupportedEncodingException e){
        Log.e(LOG_TAG, e.getMessage());
      }
      return null;
    }
}