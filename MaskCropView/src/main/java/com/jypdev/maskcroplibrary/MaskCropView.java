package com.jypdev.maskcroplibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by JY-park on 16. 6. 17..
 */
public class MaskCropView extends View {

    private static final int FLAG_ALL_CLEAR = 100;
    private static final int FLAG_RESET_DRAW = 200;

    private boolean isVertical = true;

    private float mX;
    private float mY;

    private int startX;
    private int startY;
    private int endX;
    private int endY;

    private Paint paint = new Paint();
    private Bitmap resizeBitmap;
    private boolean drawFlag = false;

    private int displayWidth;
    private int displayHeight;

    private Bitmap maskBitmap;
    private Bitmap outBitmap;

    private Canvas maskCanvas;
    private Canvas outCanvas;

    private Path maskPath;
    private Path viewPath;

    private Paint fillPaint;
    private Paint drawPaint;


    public MaskCropView(Context context) {
        super(context);
        init();
    }

    public MaskCropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaskCropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    //public
    /**
     * CropImage
     * @return Must need null Check!
     */
    public Bitmap getPicture() {
        Bitmap cropBitmap = null;
        if (drawFlag) {
            if(Math.abs(endX-startX) > 10 && Math.abs(endY-startY) > 10) {
                cropBitmap = Bitmap.createBitmap(outBitmap, startX, startY, endX - startX, endY - startY);
            }
        }else{
            outCanvas.drawBitmap(resizeBitmap, 0, 0, null);
            cropBitmap = Bitmap.createBitmap(outBitmap,0,0,displayWidth,displayHeight);
        }
        return cropBitmap;
    }


    public void setOriginalBitmap(Bitmap originalBitmap) {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (width > height) {
            width = metrics.heightPixels;
            height = metrics.widthPixels;
        }

        viewClear(FLAG_ALL_CLEAR);
        resizeBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, false);
        if (originalBitmap != resizeBitmap) {
            originalBitmap.recycle();
        }

        invalidate();
    }

    public void setOrientation(boolean isVertical) {
        this.isVertical = isVertical;
    }

    private void init() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        displayWidth = metrics.widthPixels;
        displayHeight = metrics.heightPixels;

        //mask 세팅 ================================
        fillPaint = new Paint();
        fillPaint.setColor(0xFFFFFFFF); //흰색
        fillPaint.setStyle(Paint.Style.FILL); //채우기 옵션
        maskPath = new Path();
        maskBitmap = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888);
        maskCanvas = new Canvas(maskBitmap);

        //output 세팅 ================================
        outBitmap = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888);
        outCanvas = new Canvas(outBitmap);

        //draw 세팅 ================================
        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);  //모서리 부드럽게
        drawPaint.setDither(true); //단말의 색표현력이 떨어질때 낮게 표현
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setColor(0xFFFFFFFF);
        drawPaint.setPathEffect(new DashPathEffect(new float[]{7, 4}, 2));
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setStrokeWidth(2);
        viewPath = new Path();
    }


    protected void onDraw(Canvas canvas) {
        if (resizeBitmap != null)
            canvas.drawBitmap(resizeBitmap, 0, 0, paint);  //Original Image (Background)

        if (drawFlag) {
            canvas.drawColor(0x77000000);               //Dim
            canvas.drawBitmap(outBitmap, 0, 0, null);   //Masking Image
        }
        canvas.drawPath(viewPath, drawPaint);            //Masking line
    }

    private void touch_start(float x, float y) {
        viewClear(FLAG_RESET_DRAW);
        drawFlag = false;
        maskPath.reset();
        viewPath.reset();
        maskPath.moveTo(x, y);
        viewPath.moveTo(x, y);
        mX = x;
        mY = y;
        startX = (int) mX;
        startY = (int) mY;
        endX = (int) mX;
        endY = (int) mY;
    }

    private void touch_up() {
        drawFlag = true;
        maskPath.lineTo(mX, mY);
        viewPath.lineTo(mX, mY);

        maskCanvas.drawPath(maskPath, fillPaint);
        outCanvas.drawBitmap(resizeBitmap, 0, 0, null);

        Paint paint = new Paint();
        paint.setFilterBitmap(false);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        outCanvas.drawBitmap(maskBitmap, 0, 0, paint);

        paint.setXfermode(null);

    }

    private void viewClear(int mode) {
        if (drawFlag == true) {
            maskBitmap.recycle();
            maskBitmap = null;
            maskBitmap = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888);
            maskCanvas = new Canvas(maskBitmap);

            outBitmap.recycle();
            outBitmap = null;
            outBitmap = Bitmap.createBitmap(displayWidth, displayHeight, Bitmap.Config.ARGB_8888);
            outCanvas = new Canvas(outBitmap);

        }

        if (mode == FLAG_ALL_CLEAR) {
            if (resizeBitmap != null) {
                resizeBitmap.recycle();
                resizeBitmap = null;
            }

            maskPath.reset();
            viewPath.reset();

            drawFlag = false;
        } else if (mode == FLAG_RESET_DRAW) {

        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();

        float x = event.getX();
        float y = event.getY();

        if (resizeBitmap != null) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    maskPath.lineTo(x, y);
                    viewPath.lineTo(x, y);
                    startX = startX > x ? (int) x : startX;
                    startY = startY > y ? (int) y : startY;
                    endX = endX < x ? (int) x : endX;
                    endY = endY < y ? (int) y : endY;
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    break;
            }
            invalidate();
        }
        return true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (resizeBitmap != null) {
            resizeBitmap.recycle();
            resizeBitmap = null;
        }
        if (maskBitmap != null) {
            maskBitmap.recycle();
            maskBitmap = null;
        }
        if (outBitmap != null) {
            outBitmap.recycle();
            outBitmap = null;
        }
    }
}
