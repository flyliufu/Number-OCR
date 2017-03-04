package com.lavor.functionsdemo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.lavor.functionsdemo.bean.EnterpriseInfo;
import com.lavor.functionsdemo.bean.JSONEntity;
import java.io.File;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author liufu on 2017/2/3.
 */

public class ResultActivity extends AppCompatActivity
    implements View.OnClickListener, View.OnFocusChangeListener, TextWatcher {

  private static final String TAG = "ResultActivity";
  private EditText mEtResult;
  private ImageView mIvResult;
  private Observer<JSONEntity<EnterpriseInfo>> mQueryObserver =
      new Observer<JSONEntity<EnterpriseInfo>>() {
        @Override public void onCompleted() {

        }

        @Override public void onError(Throwable e) {
          Log.e(TAG, "onError: ", e);
        }

        @Override public void onNext(JSONEntity<EnterpriseInfo> e) {
          if ("0000".equals(e.getCode())) {
            EnterpriseInfo r = e.getResult();
            String string = getString(R.string.result, r.getEnterpriseName(), r.getCompanyType(),
                r.getAddress(), r.getLegalPerson(), r.getRegisteredCapital(), r.getCreationDate(),
                r.getStartDate(), r.getEndDate(), r.getBusinessScope(),
                r.getRegistrationAuthority());
            Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
            intent.putExtra("str", string);
            startActivity(intent);
          } else {
            Toast.makeText(getApplicationContext(), e.getMsg(), Toast.LENGTH_SHORT).show();
          }
        }
      };
  private String result;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_result);

    initViews();
  }

  private void initViews() {
    mIvResult = (ImageView) findViewById(R.id.iv_result);
    mEtResult = (EditText) findViewById(R.id.et_result);

    findViewById(R.id.btn_retake).setOnClickListener(this);
    findViewById(R.id.btn_submit).setOnClickListener(this);
    mEtResult.setOnFocusChangeListener(this);
    result = getIntent().getStringExtra("result");
    // mEtResult.setText(result);

    int length = result.length();
    int a = length % 4 == 0 ? length / 4 : length / 4 + 1;
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < a; i++) {
      int e = (i + 1) * 4;
      if (e > result.length()) e = length;
      sb.append(result.substring(i * 4, e));
      sb.append("	");
    }

    mEtResult.setText(sb.toString());
    mEtResult.addTextChangedListener(this);
    File parent = Environment.getExternalStorageDirectory();
    File pic = new File(parent, "pic.jpg");
    Bitmap mBitmap = BitmapFactory.decodeFile(pic.getPath());
    if (mBitmap != null) {

      Drawable drawable = new BitmapDrawable(getResources(), mBitmap);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
        mIvResult.setBackground(drawable);
      } else {
        mIvResult.setBackgroundDrawable(drawable);
      }
    }

    query();
  }

  private void query() {
    result = check(mEtResult.getText().toString());
    App.getHttpAPI()
        .query("posboss", result)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(mQueryObserver);
  }

  private String check(String str) {
    return str.replaceAll("\t", "")
        .replace(" ", "")
        .replace("\n", "")
        .replace("I", "1")
        .replace("i", "1")
        .replace("z", "2")
        .replace("Z", "2")
        .replace("o", "0")
        .replace("O", "0");
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_submit:
        query();
        break;
      case R.id.btn_retake:
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
        finish();
        break;
    }
  }

  @Override public void onFocusChange(View v, boolean hasFocus) {
    if (hasFocus) {

    }
  }

  @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

  }

  @Override public void onTextChanged(CharSequence s, int start, int before, int count) {

  }

  @Override public void afterTextChanged(Editable s) {

  }
}
