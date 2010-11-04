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
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class EncryptActivity extends Activity {
    private static final String LOG_TAG = EncryptActivity.class.getName();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        EditText fileNameEdit = (EditText) findViewById(R.id.FileNameEdit);
        Spinner EncryptionSelect = (Spinner) findViewById(R.id.EncryptSelect);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.EncryptSelectOptions, 
            android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        EncryptionSelect.setAdapter(spinnerAdapter);
        
        String seed = "This is whatever passphrase is used to protect this content.";
        String plainText = "It's a secret to everybody (not)";
        String EncryptionType = "AES";
        
        try{
          KeyGenerator keyGen = KeyGenerator.getInstance(EncryptionType);
        //Use SHA1 algorithm
          SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");
          secRand.setSeed(seed.getBytes());
          
          keyGen.init(128, secRand);
          SecretKey key = keyGen.generateKey();
          byte[] rawKey = key.getEncoded();
          
          SecretKeySpec skeySpec = new SecretKeySpec(rawKey, EncryptionType);
          Cipher dCipher = Cipher.getInstance(EncryptionType);
          dCipher.init(Cipher.ENCRYPT_MODE, skeySpec);
          
          byte[] encrypted = dCipher.doFinal(plainText.getBytes());
        }catch(NoSuchAlgorithmException nsae){
          Log.e(LOG_TAG, "There is no algorithm that coresponds to " + EncryptionType + ".");
        } catch(NoSuchPaddingException nspe){
          Log.e(LOG_TAG, nspe.getMessage());
        } catch(InvalidKeyException ike){
          Log.e(LOG_TAG, ike.getMessage());
        } catch(BadPaddingException bpe){
          Log.e(LOG_TAG, bpe.getMessage());
        } catch(IllegalBlockSizeException ibse){
          Log.e(LOG_TAG, ibse.getMessage());
        }
    }
}