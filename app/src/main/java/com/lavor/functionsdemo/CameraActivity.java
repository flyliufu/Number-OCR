package com.lavor.functionsdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.util.List;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener, Camera.PictureCallback, Camera.ShutterCallback, Camera.PreviewCallback {
	
	private static final String TAG = "CameraActivity";
	private SurfaceView mRvCamera;
	private SurfaceHolder holder;
	private Camera camera;
	private Display display;
	private RectImageView mIvPhotoRect;
	private int flag;
	private byte[] mData;
	private Bitmap mBitmap;
	private Point mPoint;
	private ImageView mIvResult;
	private DisplayMetrics metrics;
	private float density;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		initViews();
	}
	
	private void initViews() {

		// 获取屏幕信息
		WindowManager mManger = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		density = metrics.density;
		display = mManger.getDefaultDisplay();
		mIvResult = (ImageView) findViewById(R.id.iv_result);
		mRvCamera = (SurfaceView) findViewById(R.id.sv_camera);
		mIvPhotoRect = (RectImageView) findViewById(R.id.iv_photo_rect);

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
	 *
	 * @param holder
	 */
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			int cameraIndex = findBackCamera();
			if (cameraIndex != -1) {
				// 打开摄像头
				camera = Camera.open(cameraIndex);
				camera.setPreviewCallback(this);
				camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
				camera.setDisplayOrientation(0); // 相机自然的角度
				camera.startPreview(); // 开始预览
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 获取预览图像大小
	 *
	 * @param sizes
	 * @param w
	 * @param h
	 * @return
	 */
	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
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
	
	/**
	 * 设置照片格式
	 */
	private void setParameter() {
		Camera.Parameters parameters = camera.getParameters(); // 获取各项参数
		parameters.setPictureFormat(ImageFormat.JPEG); // 设置图片格式
		parameters.setJpegQuality(100); // 设置照片质量
		parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		mPoint = new Point();
		display.getSize(mPoint);
		List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
		Camera.Size optimalPreviewSize = getOptimalPreviewSize(sizes, mPoint.x, mPoint.y);
		parameters.setPreviewSize(optimalPreviewSize.width, optimalPreviewSize.height);
		camera.setParameters(parameters);
	}
	
	/**
	 * 查找后置摄像头
	 *
	 * @return
	 */
	private int findBackCamera() {
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
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		setParameter();
		camera.startPreview();
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
	}
	
	@Override
	public void onClick(View v) {
		camera.takePicture(this, null, this);
	}
	
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d(TAG, "onPictureTaken: execute");
		camera.startPreview();
		this.mData = data;
		TakePhotoThread thread = new TakePhotoThread();
		thread.start();
	}
	
	@Override
	public void onShutter() {
	}
	
	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.d(TAG, "onPreviewFrame: execute");
	}

	public class TakePhotoThread extends Thread {
		@Override
		public void run() {
			try {
				execute();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	Handler mHandler = new Handler();

	private synchronized void execute() {
		flag++;
		Rect drawRect = mIvPhotoRect.getDrawRect();
		if (mData != null && mPoint != null && drawRect != null) {

			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = 1;
			mBitmap = BitmapFactory.decodeByteArray(mData, 0, mData.length,opts);
			if (mBitmap != null) {
				//截取
				Bitmap bitmap = Bitmap.createScaledBitmap(mBitmap, mPoint.x, mPoint.y, true);
				Bitmap rectBitmap = Bitmap.createBitmap(bitmap, drawRect.top, drawRect.left, drawRect.width(), drawRect.height());
				if (!mBitmap.isRecycled()) {
					mBitmap.recycle();
				}
				final Drawable drawable = new BitmapDrawable(getResources(), bitmap);
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
							mIvResult.setBackground(drawable);
						} else {
							mIvResult.setBackgroundDrawable(drawable);
						}
					}
				});
			}
		}
		flag--;
	}

	public Bitmap resizeImage(Bitmap bitmap, int w, int h) {
		Bitmap BitmapOrg = bitmap;
		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		Matrix matrix = new Matrix();
		matrix.postScale(scaleWidth, scaleHeight);
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width, height, matrix, true);
		return resizedBitmap;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (camera != null) {
			camera.stopPreview();
			camera.release(); // 释放照相机
			camera = null;
		}
	}
}
