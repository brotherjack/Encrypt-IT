package com.Encrypt;

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
        Button decryptItButton = (Button) findViewById(R.id.DecryptItButton);
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
            try{
              //SecretKey key = KeyGenerator.getInstance(encryptionType).generateKey();
              SecretKeySpec key = makeKey("/mnt/sdcard/usefulcmds.txt", seed);
              if(key == null) 
                throw new KeyGenerationFailureException("Was unable to generate a key!");
              else
                saveKey(key, "/mnt/sdcard/usefulcmds.txt");
              encCipher = Cipher.getInstance(encryptionType);
              encCipher.init(Cipher.ENCRYPT_MODE, key);
              encryptFile("/mnt/sdcard/usefulcmds.txt", "/mnt/sdcard/usefulcmds.enc", encCipher);
            } catch (NoSuchPaddingException e) {
              Log.e(LOG_TAG, e.getMessage());
            } catch (NoSuchAlgorithmException e) { 
              Log.e(LOG_TAG, e.getMessage());
            } catch (InvalidKeyException e) { 
              Log.e(LOG_TAG, e.getMessage());
            } catch(KeyGenerationFailureException e) {
              Log.e(LOG_TAG, e.getMessage());
              Toast.makeText(encryptFileActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
          }
        });
        //Fix when Key form is implemented
        /*decryptItButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View encryptView) {
            String fileName = fileNameEdit.getEditableText().toString();
            String encryptionType = encryptionSelect.getSelectedItem().toString();
            String seed = seedEdit.getEditableText().toString();
            Cipher decCipher = null;
            try{
              //SecretKey key = KeyGenerator.getInstance(encryptionType).generateKey();
              SecretKeySpec key = makeKey(fileName, seed);
              
              decCipher = Cipher.getInstance(encryptionType);
              decCipher.init(Cipher.DECRYPT_MODE, key);
              //encryptFile("test.enc", "/mnt/sdcard/test", decCipher);
              //decCipher.init(Cipher.DECRYPT_MODE, key);
            } catch (NoSuchPaddingException e) {
              Log.e(LOG_TAG, e.getMessage());
            } catch (NoSuchAlgorithmException e) { 
              Log.e(LOG_TAG, e.getMessage());
            } catch (InvalidKeyException e) { 
              Log.e(LOG_TAG, e.getMessage());
            } 
          }
        });*/
    }
    
    private SecretKeySpec makeKey(String fileName, String seed){
      try {
        SecureRandom secRand = SecureRandom.getInstance("SHA1PRNG");
        secRand.setSeed(seed.getBytes("UTF-8"));
        
        byte[] key = (fileName + secRand.toString()).getBytes("UTF-8");
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        
        key = sha1.digest(key);
        //Reduce key to 128 bytes
        byte[] shorterKey = new byte[16];
        for(int i=0; i < 16; i++)
          shorterKey[i] = key[i];
        
        return new SecretKeySpec(shorterKey, "AES");
      } catch (NoSuchAlgorithmException e) {
        Log.e(LOG_TAG, e.getMessage());
      } catch (UnsupportedEncodingException e) {
        Log.e(LOG_TAG, e.getMessage());
      }
      return null;
    }
    
    private void saveKey(SecretKeySpec key, String fileName){
      FileInputStream input = null;
      FileOutputStream output = null;
      try{
        File fileIn = new File(fileName);
        File fileOut = new File(fileName+".key");
        input = new FileInputStream(fileIn);
        output = new FileOutputStream(fileOut);
        byte[] bytes = new byte[(int)fileIn.length()];
        while(input.read() != -1){
          output.write(bytes);
        }
      } catch(IOException e){
        Log.e(LOG_TAG, e.getMessage());
      } finally{
        if(input != null) try {
          input.close();
        } catch (IOException e) {
          Log.e(LOG_TAG, "Cannot close input stream!");
          e.printStackTrace();
        }
        if(output != null) try {
          output.close();
        } catch (IOException e) {
          Log.e(LOG_TAG, "Cannot close output stream!");
          e.printStackTrace();
        }
      }
    }
    
    private void encryptFile(String fileInName, String fileOutName, Cipher encCipher){
      FileInputStream input = null;
      FileOutputStream output = null;

      try{
        File fileIn = new File(fileInName);
        if(fileIn.length() > Integer.MAX_VALUE) throw new FileTooBigException("File\'" + 
            fileInName + "\' is too large too encrypt.");
        
        input = new FileInputStream(new File(fileInName));
        byte[] plainText = new byte[(int)fileIn.length()];
        input.read(plainText);
        
        byte[] encrypted = encCipher.doFinal(plainText);
        
        output = new FileOutputStream(new File(fileOutName));
        output.write(encrypted);

        Toast.makeText(this, "Encrypted \'" + fileInName + "\'.", Toast.LENGTH_SHORT).show();
      } catch(BadPaddingException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(UnsupportedEncodingException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch(IllegalBlockSizeException e){
        Log.e(LOG_TAG, e.getMessage());
      } catch (FileTooBigException e){
        Log.e(LOG_TAG, e.getMessage());
        Toast.makeText(this, "Was unable to encrypt file \'" + fileInName + 
            "\' is too large too encrypt.", Toast.LENGTH_SHORT);
      }catch(IOException ioe){
        Log.e(LOG_TAG, ioe.getMessage());
        Toast.makeText(this, "Was unable to encrypt file \'" + fileInName + "\'.", 
            Toast.LENGTH_SHORT);
      } finally{
        if(input != null) try {
          input.close();
        } catch (IOException e) {
          Log.e(LOG_TAG, "Cannot close input stream!");
          e.printStackTrace();
        }
        if(output != null) try {
          output.close();
        } catch (IOException e) {
          Log.e(LOG_TAG, "Cannot close output stream!");
          e.printStackTrace();
        }
      }
    }//End encryptFile
    
    private class FileTooBigException extends Exception{
      static final long serialVersionUID = 1877;
      FileTooBigException(String msg){
        super(msg);
      }
    }
    
    private class KeyGenerationFailureException extends Exception{
      static final long serialVersionUID = 1878;
      KeyGenerationFailureException(String msg){
        super(msg);
      }
    }
}