package com.lavor.functionsdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liufu on 2017/2/3.
 */

public class ResultActivity extends AppCompatActivity implements View.OnClickListener {
	//识别语言英文
	static final String DEFAULT_LANGUAGE = "eng";
	private EditText mEtResult;
	private ImageView mIvResult;

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
