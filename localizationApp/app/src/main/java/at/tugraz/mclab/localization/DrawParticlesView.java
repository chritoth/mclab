package at.tugraz.mclab.localization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

    class DrawParticlesView extends View {
        Paint mPaint;

        Bitmap mBitmap;
        Canvas mCanvas;
        Path mPath;
        Paint   mBitmapPaint;

        public DrawParticlesView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setColor(0xFFFF0000);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeCap(Paint.Cap.ROUND);
            mPaint.setStrokeWidth(20);

            mPath = new Path();
            mBitmapPaint = new Paint();
            mBitmapPaint.setColor(Color.RED);
        }
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }
        @Override
        public void onDraw(Canvas canvas) {
            // TODO Auto-generated method stub
            super.draw(canvas);
            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath(mPath, mPaint);
        }


    }
