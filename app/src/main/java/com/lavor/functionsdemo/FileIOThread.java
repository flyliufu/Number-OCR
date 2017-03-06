package com.lavor.functionsdemo;

import android.content.Context;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileIOThread {

  private final Context mContext;

  public FileIOThread(Context context) {
    this.mContext = context;
  }

  private void copyFile() {
    File file = new File(Environment.getExternalStorageDirectory(), "tessdata");
    if (!file.exists()) {
      boolean createSuccess = file.mkdirs();
      if (!createSuccess) {
        throw new RuntimeException("Create tessdata failed!");
      }
      if (!file.isDirectory()) {
        throw new RuntimeException("Tessdata isn't a directory!");
      }
    }

    InputStream inputStream = null;
    FileOutputStream fileOutputStream = null;
    String FILE_NAME = "eng.traineddata";
    File path = new File(file, FILE_NAME);
    try {
      boolean newFile = path.createNewFile();
      if (!newFile) return;

      // 得到数据库的输入流
      inputStream = mContext.getAssets().open("tessdata/" + FILE_NAME);
      // 用输出流写到SDcard上面
      fileOutputStream = new FileOutputStream(path);
      // 创建byte数组 用于1KB写一次
      byte[] buffer = new byte[1024];
      int count;
      while ((count = inputStream.read(buffer)) > 0) {
        fileOutputStream.write(buffer, 0, count);
      }
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage());
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

  public void run() {
    copyFile();
  }
}
