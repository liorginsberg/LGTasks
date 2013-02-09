package com.liorginsberg.lgtasks;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.EditText;

public class CustomEditText extends EditText {

	private Paint mPaintUnderline;
	private Paint mPaintTriangle;
	int width;
	int height;

	public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		mPaintUnderline = new Paint();
		mPaintUnderline.setStyle(Paint.Style.STROKE);
		mPaintUnderline.setStrokeWidth(1);
		mPaintUnderline.setColor(Color.GRAY);
		
		
		
		mPaintTriangle = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintTriangle.setStyle(Paint.Style.FILL);
		mPaintTriangle.setColor(Color.DKGRAY);
		mPaintTriangle.setAntiAlias(true);

		System.out.println("constructor");
	}

	protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);

		// Measure Width
		if (widthMode == MeasureSpec.EXACTLY) {
			// Must be this size
			width = widthSize;
		} else if (widthMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			width = Math.min(50, widthSize);
		} else {
			// Be whatever you want
			width = 100;
		}

		// Measure Height
		if (heightMode == MeasureSpec.EXACTLY) {
			// Must be this size
			height = heightSize;
		} else if (heightMode == MeasureSpec.AT_MOST) {
			// Can't be bigger than...
			height = Math.min(80, heightSize);
		} else {
			// Be whatever you want
			height = 73;
		}

		// Satisfy contract by calling setMeasuredDimension
		setMeasuredDimension(width, height);

	}

	protected void onDraw(Canvas canvas) {

		canvas.drawLine(8, height - 5, width - 8, height - 5, mPaintUnderline); // draw
																		// underline

		//canvas.drawLine(8, height - 5, 8, height - 10, mPaintUnderline); // draw
																// left
																// corner

		 Path path = new Path();
		 path.setFillType(Path.FillType.EVEN_ODD);
		 path.moveTo(width - 7, height - 4);
		 path.lineTo(width -25, height - 4);
		 path.lineTo(width - 7, height - 25);
		// path.lineTo(width - 8, height -5);
		 path.close();

		 canvas.drawPath(path, mPaintTriangle);

		super.onDraw(canvas);
	}
}
