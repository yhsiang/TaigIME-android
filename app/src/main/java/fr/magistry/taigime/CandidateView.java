/*
 * Copyright (C) 2008-2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package fr.magistry.taigime;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class CandidateView extends View {

    private static final int OUT_OF_BOUNDS = -1;

    private Composer mComposer;
    private List<Candidate> mSuggestions;
    private String mUnusedSuffix = "";
    private int mSelectedIndex;
    private int mTouchX = OUT_OF_BOUNDS;
    private Drawable mSelectionHighlight;
    private int mCursor = 0;
    
    private Rect mBgPadding;

    private static final int MAX_SUGGESTIONS = 1320;
    private static final int SCROLL_PIXELS = 30;
    
    private int[] mWordWidth = new int[MAX_SUGGESTIONS];
    private int[] mWordX = new int[MAX_SUGGESTIONS];

    private static final int X_GAP = 10;
    
    private static final List<Candidate> EMPTY_LIST = new ArrayList<Candidate>();

    private int mColorNormal;
    private int mColorRecommended;
    private int mColorOther;
    private int mVerticalPadding;
    private Paint mPaint;
    private boolean mScrolled;
    private int mTargetScrollX;
    
    private int mTotalWidth;
    
    private GestureDetector mGestureDetector;

	private Paint mPaintTRS;
	private boolean mOutputTRS = true;

    /**
     * Construct a CandidateView for showing suggested words for completion.
     * @param context
     * @param attrs
     */
    public CandidateView(Context context) {
        super(context);
        mSelectionHighlight = context.getResources().getDrawable(
                android.R.drawable.list_selector_background);
        mSelectionHighlight.setState(new int[] {
                android.R.attr.state_enabled,
                android.R.attr.state_focused,
                android.R.attr.state_window_focused,
                android.R.attr.state_pressed
        });

        Resources r = context.getResources();
        
        setBackgroundColor(r.getColor(R.color.candidate_background));
        
        mColorNormal = r.getColor(R.color.candidate_normal);
        mColorRecommended = r.getColor(R.color.candidate_recommended);
        mColorOther = r.getColor(R.color.candidate_other);
        mVerticalPadding = r.getDimensionPixelSize(R.dimen.candidate_vertical_padding);
        
        mPaint = new Paint();
        mPaint.setColor(mColorNormal);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_font_height));
        mPaint.setStrokeWidth(0);
        
        mPaintTRS = new Paint();   
        mPaintTRS.set(mPaint);  
        mPaintTRS.setTextSize(r.getDimensionPixelSize(R.dimen.candidate_font_trs_height));
        
        mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2,
                    float distanceX, float distanceY) {
                mScrolled = true;
                int sx = getScrollX();
                sx += distanceX;
                if (sx < 0) {
                    sx = 0;
                }
                if (sx + getWidth() > mTotalWidth) {                    
                    sx -= distanceX;
                }
                mTargetScrollX = sx;
                scrollTo(sx, getScrollY());
                invalidate();
                return true;
            }
        });
        setHorizontalFadingEdgeEnabled(true);
        setWillNotDraw(false);
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
    }
    
    /**
     * A connection back to the service to communicate with the text field
     * @param listener
     */
    public void setService(TaigIMEService listener) {
    }
    
    @Override
    public int computeHorizontalScrollRange() {
        return mTotalWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = resolveSize(50, widthMeasureSpec);
        
        // Get the desired height of the icon menu view (last row of items does
        // not have a divider below)
        Rect padding = new Rect();
        mSelectionHighlight.getPadding(padding);
        final int desiredHeight = ((int)mPaint.getTextSize()) + mVerticalPadding*3
                + padding.top + padding.bottom + ((int)mPaintTRS.getTextSize());
        
        // Maximum possible width and desired height
        setMeasuredDimension(measuredWidth,
                resolveSize(desiredHeight, heightMeasureSpec));
    }

    private float reduceTextSizeFromWidth(Paint p, String str, float maxWidth){
    	float size = p.getTextSize();
    	if(str.length()==0)
    		return size;
    	while(p.measureText(str) > maxWidth) {
			p.setTextSize(-- size);
		}
    	return size;
    }
    /**
     * If the canvas is null, then only touch calculations are performed to pick the target
     * candidate.
     */
    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas != null) {
            super.onDraw(canvas);
        }
        mTotalWidth = 0;
        if (mSuggestions == null) return;
        
        if (mBgPadding == null) {
            mBgPadding = new Rect(0, 0, 0, 0);
            if (getBackground() != null) {
                getBackground().getPadding(mBgPadding);
            }
        }
        int x = 0;
        final int count = mSuggestions.size(); 
        final int height = getHeight();
        final Rect bgPadding = mBgPadding;
        final Paint paint = mPaint;
        final int touchX = mTouchX;
        final int scrollX = getScrollX();
        final boolean scrolled = mScrolled;
        final int y; //= (int) ( - mPaint.ascent() + mVerticalPadding);
        final int y2; //= (int) (height - mPaintTRS.descent()); // (y - mVerticalPadding - mPaint.descent() - mPaintTRS.ascent()) ;
        final float initialTRSsize = mPaintTRS.getTextSize(); 
        if(mOutputTRS){
        	y2 = (int) ( - mPaintTRS.ascent() + mVerticalPadding);
            y = (int) (height - mPaint.descent());
        	
        }
        else {
        	y = (int) ( - mPaint.ascent() + mVerticalPadding);
            y2 = (int) (height - mPaintTRS.descent());
        
        }
        for (int i = 0; i < count; i++) {
            Candidate suggestion = mSuggestions.get(i);
            String hanji = suggestion.getWord().getHanji();
            String trs = suggestion.getWord().getTailuo();
            float textWidth;
            if (i==0){
            	hanji = ""; 
            	trs = "";//hanji;//TODO: conversion?
            	if(mOutputTRS){
            		trs = suggestion.getWord().getBopomo();
            		textWidth = mPaintTRS.measureText(trs);
            	}
            	else {
            		hanji = suggestion.getWord().getBopomo();
            		textWidth = paint.measureText(hanji);
            	}
            }
            else
              textWidth = paint.measureText(hanji);
            final int wordWidth = (int) textWidth + X_GAP * 2;
            mWordX[i] = x;
            mWordWidth[i] = wordWidth;
            mPaintTRS.setTextSize(initialTRSsize);
            mPaintTRS.setTextSize(reduceTextSizeFromWidth(mPaintTRS, trs, textWidth));
            paint.setColor(mColorNormal);
            if (touchX + scrollX >= x && touchX + scrollX < x + wordWidth && !scrolled) {
                if (canvas != null) {
                    canvas.translate(x, 0);
                    mSelectionHighlight.setBounds(0, bgPadding.top, wordWidth, height);
                    mSelectionHighlight.draw(canvas);
                    canvas.translate(-x, 0);
                }
                mSelectedIndex = i;
            }

            if (canvas != null) {
            	Paint activePaint = null;
            	Paint secondPaint = null;
            	if(mOutputTRS) {
            		activePaint = mPaintTRS;
            		secondPaint = paint;
            	}
            	else{
            		activePaint = paint;
            		secondPaint = mPaintTRS;
            	}
            	secondPaint.setAlpha(150);
                if ((i == mCursor)) {
                    activePaint.setFakeBoldText(true);
                    activePaint.setColor(mColorRecommended);
                } else if (i != 0) {
                    activePaint.setColor(mColorOther);
                }
                
                canvas.drawText(hanji, x + X_GAP, y, paint);
                canvas.drawText(trs, x + X_GAP, y2, mPaintTRS);
                activePaint.setColor(mColorOther); 
                canvas.drawLine(x + wordWidth + 0.5f, bgPadding.top, 
                        x + wordWidth + 0.5f, height + 1, paint);
                activePaint.setFakeBoldText(false);
                secondPaint.setAlpha(255);
            }
            x += wordWidth;
            mPaintTRS.setTextSize(initialTRSsize);
        }
        mTotalWidth = x;
        if (mTargetScrollX != getScrollX()) {
            scrollToTarget();
        }
    }
    
    private void scrollToTarget() {
        int sx = getScrollX();
        if (mTargetScrollX > sx) {
            sx += SCROLL_PIXELS;
            if (sx >= mTargetScrollX) {
                sx = mTargetScrollX;
                requestLayout();
            }
        } else {
            sx -= SCROLL_PIXELS;
            if (sx <= mTargetScrollX) {
                sx = mTargetScrollX;
                requestLayout();
            }
        }
        scrollTo(sx, getScrollY());
        invalidate();
    }
    
    //@SuppressLint("WrongCall")
	public void setSuggestions(ArrayList<Candidate> suggestions, boolean completions,
            boolean typedWordValid) {
        clear();
        
        if (suggestions != null) {
                mSuggestions = suggestions;
        }
        scrollTo(0, 0);
        mTargetScrollX = 0;
        // Compute the total width
        //onDraw(null);
        invalidate();
        requestLayout();
    }

    public void clear() {
        mSuggestions = EMPTY_LIST;
        mTouchX = OUT_OF_BOUNDS;
        mSelectedIndex = -1;
        mCursor = 1;
        setUnusedSuffix("");
        invalidate();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent me) {

        if (mGestureDetector.onTouchEvent(me)) {
            return true;
        }

        int action = me.getAction();
        int x = (int) me.getX();
        int y = (int) me.getY();
        mTouchX = x;

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mScrolled = false;
            invalidate();
            break;
        case MotionEvent.ACTION_MOVE:
            if (y <= 0) {
                // Fling up!?
                if (mSelectedIndex >= 0) {
                    mComposer.pickSuggestion(mSelectedIndex);
                    mSelectedIndex = -1;
                }
            }
            invalidate();
            break;
        case MotionEvent.ACTION_UP:
            if (!mScrolled) {
                if (mSelectedIndex >= 0) {
                    mComposer.pickSuggestion(mSelectedIndex);
                }
            }
            mSelectedIndex = -1;
            removeHighlight();
            requestLayout();
            break;
        }
        return true;
    }
    
    /**
     * For flick through from keyboard, call this method with the x coordinate of the flick 
     * gesture.
     * @param x
     */
    public void takeSuggestionAt(float x) {
        mTouchX = (int) x;
        // To detect candidate
        //draw(null);
        if (mSelectedIndex >= 0) {
            mComposer.pickSuggestion(mSelectedIndex);
        }
        invalidate();
    }
    public void setSelectedIndex(int idx){
    	mCursor=idx;
    }
    public int getSelectedIndex(){
    	if (mCursor < mSuggestions.size())
    		return mCursor;
    	if(mSuggestions.size()>0)
    		return 0;
    	return -1;
    		
    }
    public boolean nextSelectedIndex(){
    	
    	if(mCursor < mSuggestions.size()) {
    		mCursor += 1;
    		return true;
    	}
    	
    	return false;
    }
    public boolean prevSelectedIndex(){
    	if(mCursor>0){
    		mCursor -= 1;
    		return true;
    	}
    	return false;
    }
    private void removeHighlight() {
        mTouchX = OUT_OF_BOUNDS;
        invalidate();
    }

	public String getUnusedSuffix() {
		return mUnusedSuffix;
	}

	public void setUnusedSuffix(String mUnusedSuffix) {
		this.mUnusedSuffix = mUnusedSuffix;
	}

	public void setTypeface(Typeface tf){
		mPaint.setTypeface(tf);
	}

	public void setComposer(Composer composer) {
		mComposer = composer;
		
	}

	public boolean isOutputTRS() {
		return mOutputTRS;
	}

	public void setOutputTRS(boolean mOutputTRS) {
		this.mOutputTRS = mOutputTRS;
	}
}
