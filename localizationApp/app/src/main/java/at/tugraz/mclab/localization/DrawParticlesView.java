package at.tugraz.mclab.localization;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.media.Image;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;

class DrawParticlesView extends View{
        Paint mPaint;
        Bitmap mBitmap;
        Canvas mCanvas;

        Bitmap workingBitmap;
        Bitmap mutableBitmap;


        public DrawParticlesView(Context context) {
            super(context);
            BitmapFactory.Options myOptions = new BitmapFactory.Options();
            myOptions.inDither = true;
            myOptions.inScaled = false;
            myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
            myOptions.inPurgeable = true;

            mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan,myOptions);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.BLUE);


            workingBitmap = Bitmap.createBitmap(mBitmap);
            mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

            mCanvas = new Canvas(mutableBitmap);

        }

        /*public void drawParticles (ImageView iV, Particle [] p)
        {

         //Image size: x:129 y:429
            for (Particle particle : p)
            {
                mCanvas.drawCircle((int)particle.getX(), (int)particle.getY(), 5, mPaint);

                iV.setAdjustViewBounds(true);
                iV.setImageBitmap(mutableBitmap);

            }


        }*/
        public void drawParticles (ImageView floorPlanImageView)
        {
            for (int i = 0; i<=100; i+=20)
            {
                mCanvas.drawCircle((int)i+50, i+50, 5, mPaint);

                floorPlanImageView.setAdjustViewBounds(true);
                floorPlanImageView.setImageBitmap(mutableBitmap);

            }
        }

        public void clearPanel(ImageView floorPlanImageView)
        {
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
            mCanvas = new Canvas(mutableBitmap);
            floorPlanImageView.setAdjustViewBounds(true);
            floorPlanImageView.setImageBitmap(mutableBitmap);
        }


    }
