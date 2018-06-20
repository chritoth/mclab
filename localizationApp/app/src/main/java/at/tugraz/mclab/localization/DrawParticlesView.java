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

class DrawParticlesView extends View {
    Paint mPaint;
    Bitmap mBitmap;
    Canvas mCanvas;

    Bitmap workingBitmap;
    Bitmap mutableBitmap;

    //Image size: x:123 y:429
    private final static int PX_XMAX = 123;
    private final static int PX_YMAX = 429;
    private final static double xScaling = PX_XMAX / 14.33; // gives x pixels per meter
    private final static double yScaling = PX_YMAX / 47.50; // gives y pixels per meter

    public DrawParticlesView(Context context) {
        super(context);
        BitmapFactory.Options myOptions = new BitmapFactory.Options();
        myOptions.inDither = true;
        myOptions.inScaled = false;
        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
        myOptions.inPurgeable = true;

        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan, myOptions);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLUE);

        workingBitmap = Bitmap.createBitmap(mBitmap);
        mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);

        mCanvas = new Canvas(mutableBitmap);

    }

    public void drawParticles(ImageView iV, Particle[] p) {

        for (Particle particle : p) {

            // x-y axis switched
            int x = (int) (particle.getY() * xScaling);
            int y = (int) (particle.getX() * yScaling);

            mCanvas.drawCircle(x, y, 1, mPaint);

            iV.setAdjustViewBounds(true);
            iV.setImageBitmap(mutableBitmap);

        }
    }

    public void drawParticlesTest(ImageView floorPlanImageView) {
        for (int i = 0; i <= 100; i += 20) {
            mCanvas.drawCircle((int) i + 50, i + 50, 5, mPaint);

            floorPlanImageView.setAdjustViewBounds(true);
            floorPlanImageView.setImageBitmap(mutableBitmap);

        }

        mCanvas.drawCircle(0, 0, 5, mPaint);
        floorPlanImageView.setAdjustViewBounds(true);
        floorPlanImageView.setImageBitmap(mutableBitmap);

        mCanvas.drawCircle((int) (4.33 * xScaling), (int) (18.5 * yScaling), 5, mPaint);
        floorPlanImageView.setAdjustViewBounds(true);
        floorPlanImageView.setImageBitmap(mutableBitmap);
    }

    public void clearPanel(ImageView floorPlanImageView) {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
        mCanvas = new Canvas(mutableBitmap);
        floorPlanImageView.setAdjustViewBounds(true);
        floorPlanImageView.setImageBitmap(mutableBitmap);
    }

}
