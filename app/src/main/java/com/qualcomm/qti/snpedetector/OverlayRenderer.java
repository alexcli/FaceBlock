package com.qualcomm.qti.snpedetector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import java.lang.Math;

import static java.lang.Math.abs;

public class OverlayRenderer extends View {
    public static final String LOGTAG = "SNPEDetector:OverlayRenderer";
    private boolean mFrameOrientation = false; // 0 -> portrait, 1 -> landscape

    private ReentrantLock mLock = new ReentrantLock();
    private ArrayList<Box> mBoxes = new ArrayList<>();

    private boolean mHasResults;
    private Paint mOutlinePaint = new Paint();
    private Paint mFillPaint = new Paint();
    private Paint mEmojiPaint = new Paint();
    private float mBoxScoreThreshold = 0.2f;

    private boolean mEnablePrivacy = true;
    private boolean mBlockAll = true;
    private boolean mCustomCover = true;

    private int mEmojiSize = 300;
    private String mEmoji = "\uD83D\uDE0A";

    private float selectedX;
    private float selectedY;

    public OverlayRenderer(Context context) {
        super(context);
        init();
    }

    public OverlayRenderer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayRenderer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setGeometryOfTheUnderlyingCameraFeed() {
        // TODO...
    }

    public void setOrientation(boolean o) {
        mFrameOrientation = o;
    }

    public void setEnablePrivacy(boolean o){
        mEnablePrivacy = o;
    }

    public void setBlockAll(boolean o){
        mBlockAll = o;
    }

    public void setCustomCover(boolean o){
        mCustomCover = o;
    }
    public void setNextBoxScoreThreshold(float scoreThreshold) {
        mBoxScoreThreshold = scoreThreshold;
    }

    public void setEmojiSize(int size){
        mEmojiSize = size;
    }

    public void setEmoji(String s){
        mEmoji = s;
    }

    public String getEmoji(){
        return mEmoji;
    }
    public float getBoxScoreThreshold() {
        return mBoxScoreThreshold;
    }

    public void setBoxesFromAnotherThread(ArrayList<Box> nextBoxes) {
        mLock.lock();
        if (nextBoxes == null) {
            mHasResults = false;
            for (Box box : mBoxes)
                box.type_score = 0;
        } else {
            mHasResults = true;
            for (int i = 0; i < nextBoxes.size(); i++) {
                final Box otherBox = nextBoxes.get(i);
                if (i >= mBoxes.size())
                    mBoxes.add(new Box());
                otherBox.copyTo(mBoxes.get(i));
            }
        }
        mLock.unlock();
        postInvalidate();
    }

    private void init() {
        mOutlinePaint.setStyle(Paint.Style.STROKE);
        mOutlinePaint.setStrokeWidth(6);
        mFillPaint.setStyle(Paint.Style.FILL);
        mEmojiPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int viewWidth = getWidth();
        final int viewHeight = getHeight();
        mEmojiPaint.setTextSize(mEmojiSize);

        // in case there were no results, just draw an X on screen.. totally optional
        if (!mHasResults) {
            mOutlinePaint.setColor(Color.WHITE);
            canvas.drawLine(viewWidth, 0, 0, viewHeight, mOutlinePaint);
            canvas.drawLine(0, 0, viewWidth, viewHeight, mOutlinePaint);
            return;
        }

        final int virtualSize = Math.max(viewHeight, viewHeight);
        final int virtualDx = (virtualSize - viewWidth) / 2;
        final int virtualDy = (virtualSize - viewHeight) / 2;

        mLock.lock();
        float bl, bt, br, bb;
        for (int i = 0; i < mBoxes.size(); i++) {
            final Box box = mBoxes.get(i);
            if (!box.type_name.equals("person")){
                continue;
            }
            // skip rendering below the threshold
            if (box.type_score < mBoxScoreThreshold) {
                break;
            }

            // compute the final geometry
            if (mFrameOrientation) {
                // LANDSCAPE MODE, FRONT CAMERA COORD
                bt = viewHeight - (virtualSize * box.left - virtualDy);
                bl = viewWidth - (virtualSize * box.top - virtualDx);
                bb = viewHeight - (virtualSize * box.right - virtualDy);
                br = viewWidth - (virtualSize * box.bottom - virtualDx);
            } else {
                // PORTRAIT MODE, FRONT CAMERA COORD
                bl = viewWidth - (virtualSize * box.left - virtualDx);
                bt = virtualSize * box.top - virtualDy;
                br = viewWidth - (virtualSize * box.right - virtualDx);
                bb = virtualSize * box.bottom - virtualDy;
            }
            float bcx = (bl + br) / 2;
            float bcy = (bt + bb) / 2;
            float boxSize = abs((br-bl)*(bt-bb));
            mEmojiPaint.setTextSize((int)(boxSize/220000* mEmojiSize)+180);

            if (mEnablePrivacy) {
                if (!mBlockAll && box.is_tracked) {
                    mOutlinePaint.setColor(Color.argb(80, 0,255,0));
                    canvas.drawRect(bl, bt, br, bb, mOutlinePaint);
                } else if (mCustomCover){
                    if (mFrameOrientation) {
                        canvas.rotate(90, bcx, bcy);
                        canvas.drawText(mEmoji, bcx, bcy+34 + (int)(boxSize/1500), mEmojiPaint);
                        canvas.rotate(-90, bcx, bcy);

                    } else {
                        canvas.drawText(mEmoji, bcx, bcy + 34 + (int) (boxSize / 1500), mEmojiPaint);
                    }
                } else {
                    canvas.drawRect(bl, bt, br, bb, mFillPaint);
                }

            }
        }
        mLock.unlock();
    }

    public void changeSelectedCoords(float X, float Y){
        selectedX = X;
        selectedY = Y;
    }
}
