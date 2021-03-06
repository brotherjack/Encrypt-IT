package com.encryptit.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.*;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Class that handles the creation of a file list from which the user can select
 * a flash file to load.
 * 
 * @author Thomas Adriaan Hellinger
 * 
 */
public class FileListActivity extends ListActivity {
  private String[] mCardContents; // A list of files in current directory
  private String mSelected; // File name of the SWF file to load
  private static String mRootDir; // Root of this application
  private static String mCurrentDir; // Directory we are currently at
  private static ListActivity mFileListAct; // This activity
  

  private static final int NULL_RETURN = -1;
  private static final int PATH_TO_FILE = 0; // Returning path to file to
  // decrypt?
  
  private static final String PHONE_ROOT =
      Environment.getExternalStorageDirectory().toString();
  private final String LOG_TAG = FileListActivity.class.getName();
  private final String SELECTED_PATH = "selected.path";
  private final String SELECTED_TYPE = "selected.type";

  private static int mReturnType = NULL_RETURN;
  private static SharedPreferences mPreferences = null;

  /**
   * Creates the ListActivity with the files from the sdcard root and loads a
   * file into the content viewer when the user selects it
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // If this is called by DecryptFileActivity, find out whether
    // program is to return path to file to decrypt or key to do it with
    if (this.getIntent().hasExtra(SELECTED_TYPE)) {
      // Default is "path to file" if not specified, but should have an extra
      // value.
      mReturnType = this.getIntent().getIntExtra(SELECTED_TYPE, PATH_TO_FILE);
    }

    Context thisActContext = this.getApplicationContext();
    mFileListAct = this;
    mPreferences = thisActContext.getSharedPreferences("User Preferences", 0);

    mRootDir =
        mPreferences.getString("rootDir", Environment
            .getExternalStorageDirectory().toString());

    loadDir(mRootDir);

    ListView fileView = this.getListView();
    fileView.setTextFilterEnabled(true);

    mSelected = mRootDir;
    mCurrentDir = mRootDir;
    final ComponentName callingActivity = this.getCallingActivity();

    // Create a listener to respond correctly when the user selects an activity.
    fileView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        mSelected = (String) parent.getItemAtPosition(position); // selected
        // item
        File selected = new File(mCurrentDir.concat(mSelected));
        if (mSelected.contentEquals("..")) {
          if (mCurrentDir.contentEquals(PHONE_ROOT)) {
            Toast.makeText(mFileListAct,
                "Cannot access files outside of sdcard root!",
                Toast.LENGTH_SHORT).show();
          } else {
            // Strip trailing backspace character
            mCurrentDir = mCurrentDir.substring(0, mCurrentDir.length() - 1);

            // Compile and match directory to the last directory on path
            Pattern pattern = Pattern.compile("/[a-zA-Z0-9-]*$");
            Matcher matcher = pattern.matcher(mCurrentDir);
            matcher.find();

            // Remove last directory on the path
            mCurrentDir = (String) mCurrentDir.substring(0, matcher.start());

            loadDir(mCurrentDir);
            Log.i(LOG_TAG, "Working directory for Encrypt-It is " + mCurrentDir);

            mFileListAct.setListAdapter(new ArrayAdapter<String>(mFileListAct,
                android.R.layout.simple_list_item_1, mCardContents));
            view.invalidate();
          }
        } else if (selected.isDirectory()) {
          mCurrentDir = mCurrentDir.concat(mSelected);

          loadDir(mCurrentDir);
          Log.i(LOG_TAG, "Working directory for Encrpt-It is " + mCurrentDir);

          mFileListAct.setListAdapter(new ArrayAdapter<String>(mFileListAct,
              android.R.layout.simple_list_item_1, mCardContents));
          view.invalidate();

        } else {
          Bundle fileNameBundle = new Bundle();
          fileNameBundle
              .putString(SELECTED_PATH, mCurrentDir + "/" + mSelected);
          if(mReturnType != NULL_RETURN){
            fileNameBundle.putInt(SELECTED_TYPE, mReturnType);
          }

          //Return path to calling activity for file to be be encrypted or decrypted (or key)
          Intent sendFileToBrowser =
              new Intent(FileListActivity.this, callingActivity.getClass());
          sendFileToBrowser.putExtras(fileNameBundle); // Bundle data w/ intent
          if (getParent() == null) { // Ensure that parent activity is not null
            FileListActivity.this.setResult(RESULT_OK, sendFileToBrowser);
          } else {
            getParent().setResult(RESULT_OK, sendFileToBrowser);
          }
          FileListActivity.this.finish(); // End activity, return result
        }
      }
    });
  }

  private void loadDir(String dirToLoad) {
    File dir = new File(dirToLoad);
    mCardContents = sortDirectoryContents(dir.listFiles()); // sort in alpha
    // order

    this.setListAdapter(new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, mCardContents)); // Arrange list
  }

  /**
   * Arranges list contents in alphabetical order.
   * 
   * @author Thomas Adriaan Hellinger
   * @param contents - String from current directory.
   * @return String array of alphabetically sorted files in current directory.
   */
  private String[] sortDirectoryContents(File[] contents) {
    ArrayList<String> files = new ArrayList<String>();
    ArrayList<String> directories = new ArrayList<String>();

    directories.add("..");

    for (int i = 0; i < contents.length; i++) {
      // position on all listed files

      if (!contents[i].isDirectory()) {
        String[] pathBreak = contents[i].toString().split("/");
        files.add(pathBreak[pathBreak.length - 1]); // If not directory, add
      } else {
        String[] pathBreak = contents[i].toString().split("/");
        directories.add("/" + pathBreak[pathBreak.length - 1]); // If not
        // directory,
        // add
      }
    }
    Collections.sort(directories);
    Collections.sort(files);
    directories.addAll(files);
    return directories.toArray(new String[directories.size()]);
  }
}
