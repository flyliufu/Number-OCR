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
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.lavor.functionsdemo.bean.EnterpriseInfo;
import com.lavor.functionsdemo.bean.JSONEntity;

import java.io.File;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author liufu on 2017/2/3.
 */

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {

	private EditText mEtResult;
	private ImageView mIvResult;
	private Observer<JSONEntity<EnterpriseInfo>> mQueryObserver = new Observer<JSONEntity<EnterpriseInfo>>() {
		@Override
		public void onCompleted() {

		}

		@Override
		public void onError(Throwable e) {

		}

		@Override
		public void onNext(JSONEntity<EnterpriseInfo> e) {
			if ("0000".equals(e.getCode())) {
				EnterpriseInfo result = e.getResult();
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_result);

		initViews();
	}

	private void initViews() {
		mIvResult = (ImageView) findViewById(R.id.iv_result);
		mEtResult = (EditText) findViewById(R.id.et_result);
		findViewById(R.id.btn_retake).setOnClickListener(this);
		findViewById(R.id.btn_submit).setOnClickListener(this);

		String result = getIntent().getStringExtra("result");
		int length = result.length();
		int a = length % 4 == 0 ? length / 4 : length / 4 + 1;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < a; i++) {
			int e = (i + 1) * 4;
			if (e > result.length()) e = length;
			sb.append(result.substring(i * 4, e));
			sb.append("	");
		}

		App.getHttpAPI().query("posboss", result).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mQueryObserver);

		mEtResult.setText(sb.toString());
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
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_retake:
				Intent intent = new Intent(this, CameraActivity.class);
				startActivity(intent);
				finish();
				break;
			case R.id.btn_submit:
				break;
		}
	}
}
