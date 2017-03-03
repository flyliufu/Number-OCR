package com.lavor.functionsdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
    implements View.OnClickListener, View.OnFocusChangeListener {

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
            mTvResult.setText(getString(R.string.result, r.getEnterpriseName(), r.getCompanyType(),
                r.getAddress(), r.getLegalPerson(), r.getRegisteredCapital(), r.getCreationDate(),
                r.getStartDate(), r.getEndDate(), r.getBusinessScope()));
          }
        }
      };
  private TextView mTvResult;
  private String result;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_result);

    initViews();
  }

  private void initViews() {
    mIvResult = (ImageView) findViewById(R.id.iv_result);
    mEtResult = (EditText) findViewById(R.id.et_result);
    mTvResult = (TextView) findViewById(R.id.tv_result);
    findViewById(R.id.btn_retake).setOnClickListener(this);
    findViewById(R.id.btn_submit).setOnClickListener(this);
    mEtResult.setOnFocusChangeListener(this);
    result = getIntent().getStringExtra("result");
    mEtResult.setText(result);

    //		int length = result.length();
    //		int a = length % 4 == 0 ? length / 4 : length / 4 + 1;
    //		StringBuffer sb = new StringBuffer();
    //		for (int i = 0; i < a; i++) {
    //			int e = (i + 1) * 4;
    //			if (e > result.length()) e = length;
    //			sb.append(result.substring(i * 4, e));
    //			sb.append("	");
    //		}
    //
    //		mEtResult.setText(sb.toString());
    File parent = Environment.getExternalStorageDirectory();
    File pic = new File(parent, "pic.jpg");
    Bitmap mBitmap = BitmapFactory.decodeFile(pic.getPath());
    if (mBitmap != null) {

      mTvResult.setMovementMethod(new ScrollingMovementMethod());
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
    result = mEtResult.getText().toString();

    App.getHttpAPI()
        .query("posboss", result)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(mQueryObserver);
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      Intent intent = new Intent(this, CameraActivity.class);
      startActivity(intent);
      finish();
      return true;
    }
    return super.onKeyDown(keyCode, event);
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
}
