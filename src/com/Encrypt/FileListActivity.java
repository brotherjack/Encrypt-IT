package com.Encrypt;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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
  private final String LOG_TAG = FileListActivity.class.getName();
  private final String LOADING_INTENT_KEY = "callingActivity";
  private static SharedPreferences mPreferences = null;

  /**
   * Creates the ListActivity with the files from the sdcard root and loads a
   * file into the content viewer when the user selects it
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    Context thisActContext = this.getApplicationContext();
    mPreferences = thisActContext.getSharedPreferences("User Preferences", 0);
    
    String rootDir = mPreferences.getString("rootDir", 
        Environment.getExternalStorageDirectory().toString());
    
    File dir = new File(rootDir);
    mCardContents = sortDirectoryContents(dir.listFiles()); // sort in alpha order

    this.setListAdapter(new ArrayAdapter<String>(this,
        android.R.layout.simple_list_item_1, mCardContents)); // Arrange list
    
    ListView fileView = this.getListView();
    fileView.setTextFilterEnabled(true);
    
    final ComponentName callingActivity = this.getCallingActivity();
    
    // Create a listener to respond correctly when the user selects an activity.
    fileView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        mSelected = (String) parent.getItemAtPosition(position); // selected
        // item
        Bundle fileNameBundle = new Bundle();
        fileNameBundle.putString("selected_file", mSelected);

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
    });
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
    
    directories.add(".");
    directories.add("..");
    
    for (int i = 0; i < contents.length; i++) {
      // position on all listed files
      
      if (!contents[i].isDirectory()) {
        String[] pathBreak = contents[i].toString().split("/");
        files.add(pathBreak[pathBreak.length-1]); // If not directory, add
      }
      else {
        String[] pathBreak = contents[i].toString().split("/");
        directories.add("/" + pathBreak[pathBreak.length-1]); // If not directory, add
      }
    }
    Collections.sort(directories);
    Collections.sort(files);
    directories.addAll(files);
    return directories.toArray(new String[directories.size()]);
  }
}