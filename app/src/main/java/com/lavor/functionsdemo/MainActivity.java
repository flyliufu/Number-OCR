package com.lavor.functionsdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
	private String FILE_NAME = "eng.traineddata";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		copyFile();
	}

	public void ocr(View view) {
		startActivity(new Intent(getApplicationContext(), CameraActivity.class));
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
