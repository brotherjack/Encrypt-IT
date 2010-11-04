package com.Encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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

public class EncryptActivity extends Activity {
    private static final String LOG_TAG = EncryptActivity.class.getName();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        EditText fileNameEdit = (EditText) findViewById(R.id.FileNameEdit);
        Button browseButton = (Button) findViewById(R.id.BrowseButton);
        Spinner EncryptionSelect = (Spinner) findViewById(R.id.EncryptSelect);
        Button EncryptItButton = (Button) findViewById(R.id.EncryptItButton);
        
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.EncryptSelectOptions, 
            android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        EncryptionSelect.setAdapter(spinnerAdapter);
        
        final Context encrytStringActivity = this;
        final String encryptionType = EncryptionSelect.getSelectedItem().toString();
        EncryptItButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View encryptView) {
            String result = encryptString(encryptionType);
            Toast.makeText(encrytStringActivity, result, Toast.LENGTH_LONG).show();
          }
        });
    }
    
    private String encryptString(String encryptionType){
      String seed = "This is whatever passphrase is used to protect this content.";
      String plainText = "It's a secret to everybody (not)";
      
      String result = " ";
      
      try{
        KeyGenerator keyGen = KeyGenerator.getInstance(encryptionType);
      //Use SHA1 algorithm
        SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");
        secRand.setSeed(seed.getBytes());
        
        keyGen.init(128, secRand);
        SecretKey key = keyGen.generateKey();
        byte[] rawKey = key.getEncoded();
        
        SecretKeySpec skeySpec = new SecretKeySpec(rawKey, encryptionType);
        Cipher dCipher = Cipher.getInstance(encryptionType);
        dCipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        
        byte[] encrypted = dCipher.doFinal(plainText.getBytes());
        result = encrypted.toString();
      }catch(NoSuchAlgorithmException nsae){
        Log.e(LOG_TAG, "There is no algorithm that coresponds to " + encryptionType + ".");
      } catch(NoSuchPaddingException nspe){
        Log.e(LOG_TAG, nspe.getMessage());
      } catch(InvalidKeyException ike){
        Log.e(LOG_TAG, ike.getMessage());
      } catch(BadPaddingException bpe){
        Log.e(LOG_TAG, bpe.getMessage());
      } catch(IllegalBlockSizeException ibse){
        Log.e(LOG_TAG, ibse.getMessage());
      }
      
      return result;
    }
}