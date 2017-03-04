package com.lavor.functionsdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

/**
 * @author liufu on 2017/3/3.
 */

public class DialogActivity extends Activity {

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_dialog);

    TextView textView = (TextView) findViewById(R.id.tv_result);
    String str = getIntent().getStringExtra("str");
    textView.setText(str);
    textView.setMovementMethod(new ScrollingMovementMethod());
  }

  public void onClick(View v) {
    finish();
  }
}
