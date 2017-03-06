package com.lavor.functionsdemo;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class InitActivity extends AppCompatActivity {

  private static final String TAG = "InitActivity";
  private AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {
    @Override protected Boolean doInBackground(Void... params) {
      Log.d(TAG, "doInBackground: ");
      try {
        new FileIOThread(getBaseContext()).run();
      } catch (Exception e) {
        e.printStackTrace();
        Log.d(TAG, "doInBackground: " + e.getMessage());
        return false;
      }
      return true;
    }

    @Override protected void onPostExecute(Boolean aBoolean) {
      Log.d(TAG, "onPostExecute: " + aBoolean);
      App.setFlag(getApplicationContext(), aBoolean);
      if (aBoolean) {
        startActivity(new Intent(getApplicationContext(), CameraActivity.class));
        finish();
      } else {
        Toast.makeText(getBaseContext(), "初始化数据失败请联系管理员", Toast.LENGTH_LONG).show();
      }
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_init);

    if (!App.getFlag()) {
      asyncTask.execute();
    } else {
      startActivity(new Intent(getApplicationContext(), CameraActivity.class));
      finish();
    }
  }
}
