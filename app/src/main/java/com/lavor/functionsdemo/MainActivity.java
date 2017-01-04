package com.lavor.functionsdemo;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
	//训练数据路径，必须包含tesseract文件夹
	//识别语言英文
	static final String DEFAULT_LANGUAGE = "eng";
	//识别语言简体中文
	static final String CHINESE_LANGUAGE = "chi_sim";
	private android.widget.ImageView english;
	private android.widget.TextView englishtext;
	private android.widget.ImageView simplechinese;
	private android.widget.TextView simplechinesetext;
	private String FILE_NAME = "eng.traineddata";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.simplechinesetext = (TextView) findViewById(R.id.simple_chinese_text);
		this.simplechinese = (ImageView) findViewById(R.id.simple_chinese);
		this.englishtext = (TextView) findViewById(R.id.english_text);
		this.english = (ImageView) findViewById(R.id.english);

		copyFile();
	}

	public void ocr(View view) {
		//英文识别
		EnglishOCR();
		//简体中文识别
		// SimpleChineseOCR();
	}

	public void EnglishOCR() {
		//设置图片可以缓存
		english.setDrawingCacheEnabled(true);
		//获取缓存的bitmap
		final Bitmap bmp = english.getDrawingCache();
		final TessBaseAPI baseApi = new TessBaseAPI();
		//初始化OCR的训练数据路径与语言

		boolean init = baseApi.init(Environment.getExternalStorageDirectory().getAbsolutePath(), DEFAULT_LANGUAGE);
		//设置识别模式
		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		//设置要识别的图片
		baseApi.setImage(bmp);
		english.setImageBitmap(bmp);
		englishtext.setText(baseApi.getUTF8Text());
		baseApi.clear();
		baseApi.end();

	}

	public void takePhone(View v) {
		startActivity(new Intent(getApplicationContext(), CameraActivity.class));
	}

	public void SimpleChineseOCR() {
		//设置图片可以缓存
		simplechinese.setDrawingCacheEnabled(true);
		//获取缓存的bitmap
		final Bitmap bmp = simplechinese.getDrawingCache();
		final TessBaseAPI baseApi = new TessBaseAPI();
		//初始化OCR的训练数据路径与语言
		baseApi.init("", CHINESE_LANGUAGE);
		//设置识别模式
		baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
		//设置要识别的图片
		baseApi.setImage(bmp);
		simplechinese.setImageBitmap(bmp);
		simplechinesetext.setText(baseApi.getUTF8Text());
		baseApi.clear();
		baseApi.end();
	}

	public void copyFile() {
		File file = new File(Environment.getExternalStorageDirectory(), "tessdata");
		boolean createSuccess = file.mkdirs();
		if (!createSuccess) return;

		InputStream inputStream = null;
		FileOutputStream fileOutputStream = null;
		File path = new File(file, FILE_NAME);
		try {
			boolean newFile = path.createNewFile();
			if (!newFile) return;

			// 得到数据库的输入流
			inputStream = getAssets().open("tessdata/" + FILE_NAME);
			// 用输出流写到SDcard上面
			fileOutputStream = new FileOutputStream(path);
			// 创建byte数组 用于1KB写一次
			byte[] buffer = new byte[1024];
			int count;
			while ((count = inputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, count);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 最后关闭就可以了
			try {
				if (fileOutputStream != null) {
					fileOutputStream.flush();
					fileOutputStream.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}
