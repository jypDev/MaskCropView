package com.jypdev.maskcroplibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by JY-park on 16. 6. 17..
 */
public class MaskCropView extends View {

    private static final int FLAG_ALL_CLEAR = 100;
    private static final int FLAG_RESET_DRAW = 200;

    private boolean isVertical = true;

    //현재 터치한위치
    private int currentX;
    private int currentY;

    //preview size
    private int preViewSize;
    private int preViewPosition;
    private int preViewPointSize;

    private int lbDstLeft;
    private int lbDstTop;
    private int lbDstRight;
    private int lbDstBottom;

    private int rtDstLeft;
    private int rtDstTop;
    private int rtDstRight;
    private int rtDstBottom;


    //터치 시작한위치
    private float mX;
    private float mY;
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    private Paint paint = new Paint();
    private Bitmap resizeBitmap;
    private boolean drawFlag = false;
    private boolean preViewFlag = false;
    private boolean firstDraw = true;

    private boolean leftFlag = false;
    private boolean topFlag = false;

    private int displayWidth;
    private int displayHeight;

    private Bitmap preViewBitmap;
    private Bitmap maskBitmap;
    private Bitmap outBitmap;

    private Canvas maskCanvas;
    private Canvas outCanvas;

    private Path maskPath;
    private Path viewPath;

    private Paint fillPaint;
    private Paint drawPaint;
    private Paint preViewPaint;
    private Paint preViewRectLine;


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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public MaskCropView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    //public

    /**
     * CropImage
     *
     * @return Must need null Check!
     */
    public Bitmap getPicture() {
        Bitmap cropBitmap = null;
        if (drawFlag) {
            if (Math.abs(endX - startX) > 10 && Math.abs(endY - startY) > 10) {
                cropBitmap = Bitmap.createBitmap(outBitmap, startX, startY, endX - startX, endY - startY);
            }
        } else {
            outCanvas.drawBitmap(resizeBitmap, 0, 0, null);
            cropBitmap = Bitmap.createBitmap(outBitmap, 0, 0, displayWidth, displayHeight);
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

        setDrawingCacheEnabled(true);
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

        preViewRectLine = new Paint();
        preViewRectLine.setColor(0xFFFFFF00);
        preViewPaint = new Paint();
        preViewPaint.setColor(0xFFFF0000);
        preViewSize = displayWidth / 5;
        preViewPosition = displayWidth / 5;
        preViewPointSize = 4;
        Log.v("@@@", "프리뷰사이즈: " + preViewSize);
        //preView Setting
//        setDrawingCacheEnabled(true);
    }


    protected void onDraw(Canvas canvas) {
        if (resizeBitmap != null) {
            canvas.drawBitmap(resizeBitmap, 0, 0, paint);  //Original Image (Background)

            if (firstDraw) {
                preViewBitmap = getDrawingCache();
                firstDraw = false;
            }
        }

        if (drawFlag) {
            canvas.drawColor(0x77000000);               //Dim
            canvas.drawBitmap(outBitmap, 0, 0, null);   //Masking Image
        }
        canvas.drawPath(viewPath, drawPaint);           //Masking line

        if (preViewFlag) {
            //원본
            int srcLeft = (currentX - preViewSize);
            int srcTop = (currentY - preViewSize);
            int srcRight = (currentX + preViewSize);
            int srcBottom = (currentY + preViewSize);

            int minusLeft = 0;
            int minusTop = 0;
            int minusRight = 0;
            int minusBottom = 0;

            if (srcLeft < 0) {
                minusLeft = Math.abs(srcLeft);
            }
            if (srcTop < 0) {
                minusTop = Math.abs(srcTop);
            }
            if (srcRight > getWidth()) {
                minusRight = srcRight - getWidth();
            }
            if (srcBottom > getHeight()) {
                minusBottom = srcBottom - getHeight();
            }

            srcLeft = srcLeft + minusLeft;
            srcTop = srcTop + minusTop;
            srcRight = srcRight - minusRight;
            srcBottom = srcBottom - minusBottom;

            int dstLeft = (int) 2 + minusLeft;
            int dstTop = (int) 2 + minusTop;
            int dstRight = (int) 2 + (preViewSize * 2) - minusRight;
            int dstBottom = (int) 2 + (preViewSize * 2) - minusBottom;

            if (currentX < dstRight && currentY < dstBottom) {
                leftFlag = true;
                dstLeft = getWidth() - 2 - preViewSize * 2 + minusLeft;
                dstRight = getWidth() - 2 - minusRight;

            } else {
                leftFlag = false;
            }


            int pointLeft = 2 + preViewSize - preViewPointSize;
            int pointTop = 2 + preViewSize - preViewPointSize;
            int pointRight = 2 + preViewSize + preViewPointSize;
            int pointBottom = 2 + preViewSize + preViewPointSize;

            if (!leftFlag) {
                canvas.drawRect(0, 0, preViewSize * 2 + 4, preViewSize * 2 + 4, preViewRectLine);
                canvas.drawRect(2, 2, preViewSize * 2, preViewSize * 2, paint);
                canvas.drawBitmap(preViewBitmap, new Rect(srcLeft, srcTop, srcRight, srcBottom), new Rect(dstLeft, dstTop, dstRight, dstBottom), null);
                canvas.drawRect(pointLeft, pointTop, pointRight, pointBottom, preViewPaint);
            } else if (leftFlag) {
                canvas.drawRect(getWidth() - preViewSize * 2 - 4, 0, getWidth(), preViewSize * 2 + 4, preViewRectLine);
                canvas.drawRect(getWidth() - preViewSize * 2 - 2, 2, getWidth() - 2, preViewSize * 2 + 2, paint);
                canvas.drawBitmap(preViewBitmap, new Rect(srcLeft, srcTop, srcRight, srcBottom), new Rect(dstLeft, dstTop, dstRight, dstBottom), null);
                canvas.drawRect(getWidth()-2-preViewSize-preViewPointSize,
                        2+preViewSize-preViewPointSize,
                        getWidth()-2-preViewSize+preViewPointSize,
                        2+preViewSize+preViewPointSize,
                        preViewPaint);
            }

            //손가락위치에 따라바뀜 수정필요함..
//            int srcLeft = (currentX - preViewSize);
//            int srcTop = (currentY - preViewSize);
//            int srcRight = (currentX + preViewSize);
//            int srcBottom = (currentY + preViewSize);
//
//            int minusLeft=0;
//            int minusTop=0;
//            int minusRight=0;
//            int minusBottom=0;
//            if(srcLeft<0){
//                minusLeft = Math.abs(srcLeft);
//            }
//            if(srcTop<0){
//                minusTop = Math.abs(srcTop);
//            }
//            if(srcRight<0){
//                minusRight = Math.abs(srcRight);
//            }
//            if(srcBottom<0){
//                minusBottom = Math.abs(srcBottom);
//            }
//
//
//            int dstLeft = (int) (currentX-preViewSize-preViewPosition)+minusLeft;
//            int dstTop = (int) (currentY-preViewSize-preViewPosition)+minusTop;
//            int dstRight = (int) (currentX-preViewPosition)-minusRight-minusLeft;
//            int dstBottom = (int) (currentY-preViewPosition)-minusBottom-minusTop;
//
//            if(dstLeft<0){
//                leftFlag = true;
//                lbDstLeft = (int) (currentX+preViewPosition)+minusLeft;
//                lbDstTop = (int) (currentY-preViewSize-preViewPosition)+minusTop;
//                lbDstRight = (int) (currentX+preViewSize+preViewPosition)-minusRight-minusLeft;
//                lbDstBottom = (int) (currentY-preViewPosition)-minusBottom-minusTop;
//            }else{
//                leftFlag = false;
//            }
//
//            if(dstTop<0){
//                topFlag = true;
//                rtDstLeft = (int) (currentX-preViewSize-preViewPosition)+minusLeft;
//                rtDstTop = (int) (currentY+preViewPosition)+minusTop;
//                rtDstRight = (int) (currentX-preViewPosition)-minusRight-minusLeft;
//                rtDstBottom = (int) (currentY+preViewSize+preViewPosition)-minusBottom-minusTop;
//            }else{
//                topFlag = false;
//            }
//
//            if(dstLeft<0 && dstTop<0){
//                dstLeft = (int) (currentX+preViewPosition)+minusLeft;
//                dstTop = (int) (currentY-preViewSize+preViewPosition)+minusTop;
//                dstRight = (int) (currentX+preViewSize+preViewPosition)-minusRight-minusLeft;
//                dstBottom = (int) (currentY+preViewPosition)-minusBottom-minusTop;
//            }
//
//            int pointLeft = (dstLeft+(dstRight - dstLeft)/2)-preViewPointSize;
//            int pointTop = (dstTop+(dstBottom - dstTop)/2)-preViewPointSize;
//            int pointRight = (dstLeft+(dstRight - dstLeft)/2)+preViewPointSize;
//            int pointBottom = (dstTop+(dstBottom - dstTop)/2)+preViewPointSize;
//
//            if((!leftFlag&&!topFlag) || (leftFlag&&topFlag)) {
//                canvas.drawRect(dstLeft-2,dstTop-2,dstRight+2,dstBottom+2,preViewRectLine);
//                canvas.drawRect(dstLeft, dstTop, dstRight, dstBottom,paint);
//                canvas.drawBitmap(preViewBitmap, new Rect(srcLeft, srcTop, srcRight, srcBottom), new Rect(dstLeft, dstTop, dstRight, dstBottom), null);
//            }else if(leftFlag){
//                canvas.drawBitmap(preViewBitmap, new Rect(srcLeft, srcTop, srcRight, srcBottom), new Rect(lbDstLeft, lbDstTop, lbDstRight, lbDstBottom), null);
//            }else if(topFlag){
//                canvas.drawBitmap(preViewBitmap, new Rect(srcLeft, srcTop, srcRight, srcBottom), new Rect(rtDstLeft, rtDstTop, rtDstRight, rtDstBottom), null);
//            }
//
//            canvas.drawRect(pointLeft, pointTop, pointRight, pointBottom, preViewPaint);
        }
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

                    currentX = (int) x;
                    currentY = (int) y;
                    preViewFlag = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    maskPath.lineTo(x, y);
                    viewPath.lineTo(x, y);
                    startX = startX > x ? (int) x : startX;
                    startY = startY > y ? (int) y : startY;
                    endX = endX < x ? (int) x : endX;
                    endY = endY < y ? (int) y : endY;

                    currentX = (int) x;
                    currentY = (int) y;
                    preViewFlag = true;
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    preViewFlag = false;
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
