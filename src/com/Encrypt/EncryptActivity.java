package com.Encrypt;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.SecureRandomSpi;

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
        
        final EditText fileNameEdit = (EditText) findViewById(R.id.FileNameEdit);
        Button browseButton = (Button) findViewById(R.id.BrowseButton);
        final Spinner encryptionSelect = (Spinner) findViewById(R.id.EncryptSelect);
        Button encryptItButton = (Button) findViewById(R.id.EncryptItButton);
        final EditText seedEdit = (EditText) findViewById(R.id.seedEdit);
        
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.EncryptSelectOptions, 
            android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encryptionSelect.setAdapter(spinnerAdapter);
        
        final Context encryptStringActivity = this;
        
        encryptItButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View encryptView) {
            String fileName = fileNameEdit.getEditableText().toString();
            String encryptionType = encryptionSelect.getSelectedItem().toString();
            String seed = seedEdit.getEditableText().toString();
            
            SecretKeySpec key = generateSecretKey(encryptionType, "SHA1PRNG",seed);
            String encResult = encryptString(key, encryptionType, fileName, true);
            String decResult = encryptString(key, encryptionType, encResult, false);
            String result = "Original string is " + fileName + ".\nEncrypted string is " + encResult + ".\nDecrypted result is " + decResult + ".";
            Toast.makeText(encryptStringActivity, result, Toast.LENGTH_LONG).show();
          }
        });
    }
    
    private SecretKeySpec generateSecretKey(String encryptionType, 
                                        String algorithm, String seed){
      byte[] rawKey = null;  
      try{
          KeyGenerator keyGen = KeyGenerator.getInstance(encryptionType);
        //Use SHA1 algorithm
          SecureRandom secRand = SecureRandom.getInstance(algorithm);
          secRand.setSeed(seed.getBytes());
          
          keyGen.init(128, secRand);
          SecretKey key = keyGen.generateKey();
          rawKey = key.getEncoded();
          
        } catch(NoSuchAlgorithmException nsae){
          Log.e(LOG_TAG, "There is no algorithm that coresponds to " + encryptionType + ".");
        }       
        return new SecretKeySpec(rawKey, encryptionType);
    }
    
    private String encryptString(SecretKeySpec skeySpec, String encryptionType, 
        String fileToEncrypt, boolean encrypt){
      String result = " ";
      
      try{
        Cipher dCipher = Cipher.getInstance(encryptionType);
        if(encrypt)
          dCipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        else
          dCipher.init(Cipher.DECRYPT_MODE, skeySpec);
        
        byte[] encrypted = dCipher.doFinal(fileToEncrypt.getBytes());
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
    
//    private String decryptString(String encryptionType, String algorithm, String fileToDecrypt){
//      String result = " ";
//      try{
//        Secure
//      }catch(NoSuchAlgorithmException nsae){
//        Log.e(LOG_TAG, "There is no algorithm that coresponds to " + encryptionType + ".");
//      } catch(NoSuchPaddingException nspe){
//        Log.e(LOG_TAG, nspe.getMessage());
//      } catch(InvalidKeyException ike){
//        Log.e(LOG_TAG, ike.getMessage());
//      } catch(BadPaddingException bpe){
//        Log.e(LOG_TAG, bpe.getMessage());
//      } catch(IllegalBlockSizeException ibse){
//        Log.e(LOG_TAG, ibse.getMessage());
//      }
//      return result;
//    }
}