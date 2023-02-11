package com.example.animationproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PaintView extends View {

    private  int colorEraser = Color.WHITE;
    private int defaultBgColor = Color.WHITE;
    public static final float TOUCH_TOLERANCE = 10;
    private int BrushSize = 10;
    public int colorPen = Color.BLACK;
    private int whatTool = 1;
    private float mX,mY;
    private Path mPath;
    private Paint mPaint;
    private int currentColor;
    private static final ArrayList<FingerPath> paths = new ArrayList<>();
    private final ArrayList<FingerPath> bufPaths = new ArrayList<>();
    private Bitmap mBitmap;
    private Bitmap oldFrame = BitmapFactory.decodeResource(getResources(),R.drawable.empty_pic);
    private Canvas mCanvas;
    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    public PaintView(Context context) {
        super(context);
    }

    public PaintView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        // настройка пера
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(colorPen);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setXfermode(null);
        mPaint.setAlpha(0xff);
    }
    // рисование
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();

        mCanvas.drawColor(defaultBgColor);
        mCanvas.drawBitmap(oldFrame,0,0,mBitmapPaint);
        for(FingerPath fp : paths){
            mPaint.setColor(fp.getColor());
            mPaint.setStrokeWidth(fp.getStrokeWidth());
            mPaint.setMaskFilter(null);
            mCanvas.drawPath(fp.getPath(),mPaint);
        }
        canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
        canvas.restore();
    }
   // инициализация холста
    public void init( DisplayMetrics displayMetrics){
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        mBitmap = Bitmap.createBitmap(width, height,Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        currentColor = colorPen;
    }
    // описание инструментов
    public void pen(){
        currentColor = colorPen;
        whatTool = 1;
    }
    public void eraser(){
        currentColor = colorEraser;
        whatTool = 2;
    }
    public void clear(){
        paths.clear();
        setOldFrame(BitmapFactory.decodeResource(getResources(),R.drawable.empty_pic));
        pen();
        invalidate();
    }
    public void back(){
        if(paths.size()>=1){
            bufPaths.add(paths.get(paths.size()-1));
            paths.remove(paths.size()-1);
            switch (whatTool){
                case 1:
                    pen();
                    break;
                case 2:
                    eraser();
                    break;
            }
            invalidate();
        }
    }
    public void forward(){
        if(bufPaths.size()>=1) {
            paths.add(bufPaths.get(bufPaths.size() - 1));
            bufPaths.remove(bufPaths.size() - 1);
            switch (whatTool){
                case 1:
                    pen();
                    break;
                case 2:
                    eraser();
                    break;
            }
            invalidate();
        }
    }

    // обработка прикоснавений
    private void touchStart(float x,float y){
        mPath = new Path();
        FingerPath fp = new FingerPath(currentColor, BrushSize, mPath);
        paths.add(fp);

        mPath.reset();
        mPath.moveTo(x,y);
        mX = x;
        mY = y;
    }
    private void touchMove (float x, float y){
        float dx = Math.abs(x-mX);
        float dy = Math.abs(y-mY);

        if(dx>=TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE){
            mPath.quadTo(mX,mY, (x+mX)/2, (y+mY)/2);
            mX = x;
            mY = y;
        }
    }
    private void touchUp(){
        mPath.lineTo(mX,mY);
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        bufPaths.clear();
        float x = event.getX();
        float y = event.getY();
        Bitmap b = DrawPlace.viewToBitmap(this);
        ImageView i = DrawPlace.getCanvasPreview();
        i.setImageBitmap(Bitmap.createScaledBitmap(b,b.getWidth()/5,b.getHeight()/5,false));
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }
        return true;
    }
    // get-set
    public void setBrushSize(int brushSize) {
        BrushSize = brushSize;
    }
    public void setColorEraser(int colorEraser) {
        this.colorEraser = colorEraser;
    }
    public int getDefaultBgColor() {
        return defaultBgColor;
    }
    public void setDefaultBgColor(int defaultBgColor) {
        this.defaultBgColor = defaultBgColor;
    }
    public void setOldFrame(Bitmap oldFrame) {
        this.oldFrame = oldFrame;
    }

}
