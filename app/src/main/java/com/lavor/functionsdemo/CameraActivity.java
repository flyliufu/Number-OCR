package com.lavor.functionsdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CameraActivity extends AppCompatActivity
    implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback,
    Camera.ShutterCallback {

  private static final String TAG = "CameraActivity";
  private SurfaceView mRvCamera;
  private SurfaceHolder holder;
  private Camera camera;
  private Display display;
  private RectImageView mIvPhotoRect;
  private byte[] mData;
  //识别语言英文
  static final String DEFAULT_LANGUAGE = "eng";
  private Point mPoint;
  private ImageView mIvResult;
  private boolean isFinish = true;
  private Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
    @Override public void onPreviewFrame(byte[] data, Camera camera) {
      if (isFinish && !isPause && isOk) {
        isOk = false;
        Log.d(TAG, "onPreviewFrame: ");
        isFinish = false;
        mData = data;
        exeTask();
      }
    }
  };
  private Rect mDrawRect;
  private boolean isPause;
  private boolean isOk;

  private Handler mHandler = new Handler() {
    @Override public void handleMessage(Message msg) {
      isOk = true;
      mHandler.sendEmptyMessageDelayed(0, 1500);
    }
  };

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_camera);
    Log.d(TAG, "onCreate: ");
    initViews();
  }

  private AsyncTask<Void, Void, Boolean> asyncTask = new AsyncTask<Void, Void, Boolean>() {
    @Override protected Boolean doInBackground(Void... params) {
      Log.d(TAG, "doInBackground: ");
      try {
        new FileIOThread(getBaseContext()).run();
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
      return true;
    }

    @Override protected void onPostExecute(Boolean aBoolean) {
      Log.d(TAG, "onPostExecute: ");
      App.setFlag(getApplicationContext(), aBoolean);
      if (aBoolean) {
        Toast.makeText(getApplicationContext(), "初始化成功", Toast.LENGTH_LONG).show();
      } else {
        finish();
      }
    }
  };

  private void initViews() {
    Log.d(TAG, "initViews: ");
    if (!App.getFlag()) {
      asyncTask.execute();
    }

    // 获取屏幕信息
    WindowManager mManger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    display = mManger.getDefaultDisplay();
    mIvResult = (ImageView) findViewById(R.id.iv_result);
    mRvCamera = (SurfaceView) findViewById(R.id.sv_camera);
    mIvPhotoRect = (RectImageView) findViewById(R.id.iv_photo_rect);
    mIvPhotoRect.setOnTouchListener(new View.OnTouchListener() {
      @Override public boolean onTouch(View v, MotionEvent event) {
        autoFocus();
        return false;
      }
    });

    findViewById(R.id.btn_take_photo).setOnClickListener(this);
    holder = mRvCamera.getHolder();//获得句柄
    holder.setFormat(PixelFormat.TRANSLUCENT);//translucent半透明 transparent透明
    if (holder != null) {
      holder.setKeepScreenOn(true);// 屏幕常亮
      holder.addCallback(this);// 为SurfaceView的句柄添加一个回调函数
    }
  }

  /**
   * 开始拍照时调用该方法
   * 2.3以后支持多摄像头，所以开启前可以通过getNumberOfCameras先获取摄像头数目，再通过 getCameraInfo得到需要开启的摄像头id，然后传入Open函数开启摄像头
   */
  @Override public void surfaceCreated(SurfaceHolder holder) {
    Log.d(TAG, "surfaceCreated: ");
    openCamera(holder);
  }

  private void openCamera(SurfaceHolder holder) {
    Log.d(TAG, "openCamera: ");
    try {
      int cameraIndex = findBackCamera();
      if (cameraIndex != -1) {
        initCamera(holder, cameraIndex);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initCamera(SurfaceHolder holder, int cameraIndex) throws IOException {
    Log.d(TAG, "initCamera: ");
    camera = Camera.open(cameraIndex);
    camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
    camera.setPreviewCallback(mPreviewCallback);
    camera.setDisplayOrientation(0); // 相机自然的角度
    camera.startPreview();
  }

  @Override protected void onPause() {
    Log.d(TAG, "onPause: ");
    super.onPause();
    isPause = true;
    release();
  }

  public void autoFocus() {
    Log.d(TAG, "autoFocus: ");
    if (this.camera != null) {
      synchronized (this.camera) {
        try {
          if (this.camera.getParameters().getSupportedFocusModes() != null
              && this.camera.getParameters().getSupportedFocusModes().contains("auto")) {
            this.camera.autoFocus(new Camera.AutoFocusCallback() {
              public void onAutoFocus(boolean success, Camera camera) {
              }
            });
          } else {
            Toast.makeText(this.getBaseContext(), "没有对焦功能", Toast.LENGTH_LONG).show();
          }
        } catch (Exception var4) {
          var4.printStackTrace();
          this.camera.stopPreview();
          this.camera.startPreview();
          Toast.makeText(this, "toast_autofocus_failure", Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  /**
   * 获取预览图像大小
   */
  private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
    Log.d(TAG, "getOptimalPreviewSize: ");
    final double ASPECT_TOLERANCE = 0.1;
    double targetRatio = (double) h / w;

    if (sizes == null) return null;

    Camera.Size optimalSize = null;
    double minDiff = Double.MAX_VALUE;

    int targetHeight = h;

    for (Camera.Size size : sizes) {
      double ratio = (double) size.width / size.height;
      if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
      if (Math.abs(size.height - targetHeight) < minDiff) {
        optimalSize = size;
        minDiff = Math.abs(size.height - targetHeight);
      }
    }

    if (optimalSize == null) {
      minDiff = Double.MAX_VALUE;
      for (Camera.Size size : sizes) {
        if (Math.abs(size.height - targetHeight) < minDiff) {
          optimalSize = size;
          minDiff = Math.abs(size.height - targetHeight);
        }
      }
    }
    return optimalSize;
  }

  @Override protected void onResume() {
    Log.d(TAG, "onResume: ");
    super.onResume();
    isFinish = true;
    isPause = false;
    mHandler.sendEmptyMessageDelayed(0, 1500);
  }

  /**
   * 设置照片格式
   */
  private void setParameter() {
    Log.d(TAG, "setParameter: ");

    if (camera == null) return;
    try {
      Camera.Parameters parameters = camera.getParameters(); // 获取各项参数
      parameters.setPictureFormat(ImageFormat.JPEG); // 设置图片格式
      parameters.setJpegQuality(100); // 设置照片质量
      parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
      mPoint = new Point();
      display.getSize(mPoint);
      List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
      Camera.Size optimalPreviewSize = getOptimalPreviewSize(sizes, mPoint.x, mPoint.y);
      parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
      parameters.setPictureSize(optimalPreviewSize.width, optimalPreviewSize.height);
      camera.setParameters(parameters);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 查找后置摄像头
   */
  private int findBackCamera() {
    Log.d(TAG, "findBackCamera: ");

    int cameraCount = 0;
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    cameraCount = Camera.getNumberOfCameras(); // get cameras number

    for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
      Camera.getCameraInfo(camIdx, cameraInfo); // get camerainfo
      if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
        // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
        return camIdx;
      }
    }
    return -1;
  }

  @Override public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    Log.d(TAG, "surfaceChanged: ");
    if (!isPause) {
      setParameter();
      camera.startPreview();
    }
  }

  @Override public void surfaceDestroyed(SurfaceHolder holder) {
    Log.d(TAG, "surfaceDestroyed: ");
  }

  @Override public void onClick(View v) {
    // camera.takePicture(this, null, this);
  }

  @Override public void onPictureTaken(byte[] data, Camera camera) {
    Log.d(TAG, "onPictureTaken: execute");
    camera.startPreview();

    if (isFinish && !isPause) {
      isFinish = false;
      mData = data;
      exeTask();
    }
  }

  @Override public void onShutter() {
  }

  private void exeTask() {
    Log.d(TAG, "exeTask: ");

    new AsyncTask<Void, Void, Boolean>() {
      private Bitmap mBitmap;

      @Override protected void onPreExecute() {
        //设置图片可以缓存
        mDrawRect = mIvPhotoRect.getDrawRect();
        mIvResult.setDrawingCacheEnabled(true);
      }

      @Override protected Boolean doInBackground(Void... params) {
        Bitmap bitmap = null;

        Log.d(TAG, "doInBackground: " + mData);
        if (mData != null && mPoint != null && mDrawRect != null) {

          try {
            Camera.Parameters parameters = camera.getParameters();
            int width = parameters.getPreviewSize().width;
            int height = parameters.getPreviewSize().height;

            YuvImage yuv = new YuvImage(mData, parameters.getPreviewFormat(), width, height, null);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);

            byte[] bytes = out.toByteArray();
            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
          } catch (Exception e) {
            e.printStackTrace();
          }
          //Bitmap bitmap = BitmapFactory.decodeByteArray(mData, 0, mData.length);
          Log.d(TAG, "bitmap: " + bitmap);
          if (bitmap == null) return false;
          //截取
          Bitmap rectBitmap = Bitmap.createScaledBitmap(bitmap, mPoint.x, mPoint.y, true);

          mBitmap =
              Bitmap.createBitmap(rectBitmap, mDrawRect.left, mDrawRect.top, mDrawRect.width(),
                  mDrawRect.height());

          // Assume block needs to be inside a Try/Catch block.
          String path = Environment.getExternalStorageDirectory().toString();
          File file = new File(path,
              "pic.jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
          try {
            FileOutputStream fOut = new FileOutputStream(file);

            // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
          } catch (Exception e) {
            e.printStackTrace();
          } finally {
            bitmap.recycle();
            rectBitmap.recycle();
          }

          return true;
        }
        return false;
      }

      @Override protected void onPostExecute(Boolean isSuccess) {
        Log.d(TAG, "onPostExecute: " + isSuccess);
        if (isSuccess) {
          String s = check(exeEnglishOCR(mBitmap));
          //Drawable drawable = new BitmapDrawable(getResources(), mBitmap);
          //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
          //  mIvResult.setBackground(drawable);
          //} else {
          //  mIvResult.setBackgroundDrawable(drawable);
          //}
          mBitmap.recycle();
          if (s != null && (s.length() == 15 || s.length() == 18)) {
            Intent intent = new Intent(CameraActivity.this, ResultActivity.class);
            intent.putExtra("result", s);
            startActivity(intent);
          } else {
            isFinish = true;
            // Toast.makeText(getBaseContext(), "识别有误，请重新拍照", Toast.LENGTH_SHORT).show();
          }
        }
      }
    }.execute();
  }

  private String check(String str) {
    Log.d(TAG, "check: " + str);
    if (str == null) return "";

    return str.replaceAll("\t", "")
        .replace(" ", "")
        .replace("\n", "")
        .replace("V", "")
        .replace("v", "")
        .replace("S", "5")
        .replace("s", "5")
        .replace("I", "1")
        .replace("i", "1")
        .replace("z", "2")
        .replace("Z", "2")
        .replace("o", "0")
        .replace("O", "0");
  }

  String regEx = "[^0-9A-Za-z]";

  public String exeEnglishOCR(Bitmap bitmap) {
    Log.d(TAG, "exeEnglishOCR: ");
    //获取缓存的bitmap
    final TessBaseAPI baseApi = new TessBaseAPI();
    //初始化OCR的训练数据路径与语言

    boolean init =
        baseApi.init(Environment.getExternalStorageDirectory().getAbsolutePath(), DEFAULT_LANGUAGE);
    //设置识别模式
    baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);
    //设置要识别的图片
    baseApi.setImage(bitmap);
    String utf8Text = baseApi.getUTF8Text();
    String[] split = utf8Text.split(" ");
    String result = null;
    Pattern p = Pattern.compile(regEx);
    for (String str : split) {
      if (str.length() >= 15) {
        Matcher m = p.matcher(str);
        result = m.replaceAll("").trim();
      }
    }
    //替换与模式匹配的所有字符（即非数字的字符将被""替换）
    baseApi.clear();
    baseApi.end();
    if (result != null) {
      return result;
    }
    return null;
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    mHandler.removeCallbacksAndMessages(null);
  }

  private void release() {
    if (camera != null) {
      Log.d(TAG, "release: success");
      camera.stopPreview();
      camera.release(); // 释放照相机
      camera = null;
    }
  }
}
