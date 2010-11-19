package com.Encrypt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class DecryptFileActivity extends Activity {
    private static final String LOG_TAG = EncryptFileActivity.class.getName();
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.decrypt_file);
        
        final EditText fileNameEdit = (EditText) findViewById(R.id.FileNameEdit);
        final EditText keyNameEdit = (EditText) findViewById(R.id.KeyNameEdit);
        final Spinner encryptionSelect = (Spinner) findViewById(R.id.EncryptSelect);
        Button decryptItButton = (Button) findViewById(R.id.DecryptItButton);
        
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.EncryptSelectOptions, 
            android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        encryptionSelect.setAdapter(spinnerAdapter);
        
        final Context decryptThis = this;
      
        decryptItButton.setOnClickListener(new OnClickListener(){
          @Override
          public void onClick(View encryptView) {
            String fileName = fileNameEdit.getEditableText().toString();
            String encryptionType = encryptionSelect.getSelectedItem().toString();
            String keyFileName = keyNameEdit.getEditableText().toString(); 
            boolean sdcardAccessible = true;
            try{
              sdcardAccessible = canReadAndWrite();
              if(sdcardAccessible) DecryptFile(encryptionType, fileName, keyFileName);
            } catch(ReadAndWriteFileException rawfe){
              Log.e(LOG_TAG, rawfe.getMessage());
              Toast.makeText(decryptThis, rawfe.getMessage(), Toast.LENGTH_SHORT).show();
            } 
          }
        });
    }
    
    private boolean canReadAndWrite() throws ReadAndWriteFileException{
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
          // We can read and write the media
          return true;
      } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
          // Read only
          throw new ReadAndWriteFileException("Sdcard is mounted as read-only."); 
      } else {
          throw new ReadAndWriteFileException("Sdcard is unavailable for read or write operations.");
      }
    }
    
    private void DecryptFile(String encryptionType, String fileName, String keyFileName){
      try{
        Cipher decCipher = Cipher.getInstance(encryptionType);
        SecretKeySpec key = getKey("/mnt/sdcard/usefulcmds.txt.key"); 
        decCipher.init(Cipher.DECRYPT_MODE, key);
      
        File fileIn = new File("/mnt/sdcard/usefulcmds.enc");
        if(fileIn.length() > Integer.MAX_VALUE) 
          throw new FileTooBigException("File\'/mnt/sdcard/usefulcmds.enc\' is too large too encrypt.");
        
        FileInputStream input = new FileInputStream(fileIn);
        byte[] encrypted = new byte[(int)fileIn.length()];
        input.read(encrypted);
        
        byte[] decrypt = decCipher.doFinal(encrypted);
        
        FileOutputStream output = new FileOutputStream(new File("/mnt/sdcard/decrypted.txt"));
        output.write(decrypt);  
        
        Toast.makeText(this, "Decrypted \'decrypted.txt\'.", Toast.LENGTH_SHORT).show();
      } catch (NoSuchPaddingException e) {
        Log.e(LOG_TAG, e.getMessage());
      } catch (NoSuchAlgorithmException e) { 
        Log.e(LOG_TAG, e.getMessage());
      } catch (InvalidKeyException e) { 
        Log.e(LOG_TAG, e.getMessage());
      } catch (IllegalBlockSizeException e) {
        Log.e(LOG_TAG, e.getMessage());
      } catch (BadPaddingException e) {
        Log.e(LOG_TAG, e.getMessage());
      } catch (FileNotFoundException e) {
        Log.e(LOG_TAG, e.getMessage());
      } catch (IOException e) {
        Log.e(LOG_TAG, e.getMessage());
      } catch(FileTooBigException e){
        Log.e(LOG_TAG, e.getMessage());
      }
      
    }
        
    private SecretKeySpec getKey(String fileName){
      FileInputStream input = null;
      
      try{
        File fileIn = new File(fileName);
        
        input = new FileInputStream(fileIn);
        
        byte[] bytes = new byte[(int)fileIn.length()];
        while(input.read(bytes) != -1){
          continue;
        }
        return new SecretKeySpec(bytes, "AES");
      } catch(IOException e){
        Log.e(LOG_TAG, e.getMessage());
      } finally{
        if(input != null) try {
          input.close();
        } catch (IOException e) {
          Log.e(LOG_TAG, "Cannot close input stream!");
          e.printStackTrace();
        }
      }
      return null;
    }
    
    private class FileTooBigException extends Exception{
      static final long serialVersionUID = 1877;
      FileTooBigException(String msg){
        super(msg);
      }
    }
    
    private class ReadAndWriteFileException extends Exception{
      static final long serialVersionUID = 1848;
      ReadAndWriteFileException(String msg){
        super(msg);
      }
    }
}