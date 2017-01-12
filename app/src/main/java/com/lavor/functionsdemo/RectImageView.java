package com.lavor.functionsdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Xfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author liufu on 2017/1/3.
 */

public class RectImageView extends ImageView {
	private Paint mPaint;
	private
	@SuppressLint("DrawAllocation")
	Rect mRect;

	{
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mPaint.setColor(Color.BLACK);
		mPaint.setAlpha(0xbb);
		mPaint.setStrokeWidth(2f);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
	}

	public RectImageView(Context context) {
		super(context);
	}

	public RectImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RectImageView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	@SuppressLint("DrawAllocation")
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getContext().getResources().getDimensionPixelSize(R.dimen.d150);
		int height = getContext().getResources().getDimensionPixelSize(R.dimen.d25);
		int halfHeight = getMeasuredHeight() / 2;
		int halfWidth = getMeasuredWidth() / 2;
		mRect = new Rect(halfWidth - width, halfHeight - height, halfWidth + width, halfHeight + height);
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		canvas.drawRect(mRect, mPaint);
		canvas.drawRect(0, 0, getMeasuredWidth(), getMeasuredHeight(), mPaint);
	}

	public Rect getDrawRect() {
		return mRect;
	}
}
