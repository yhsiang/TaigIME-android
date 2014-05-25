/**
 * 
 */
package fr.magistry.taigime;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.inputmethodservice.Keyboard.Key;
import android.util.Log;
import android.graphics.Typeface;
/**
 * @author pierre
 *
 */
public class CustomKeyIcon extends Drawable {
	
	
	private final String text;
    private final Paint paint;
    private final Key key;
    private Typeface mfont;
    

    public CustomKeyIcon(Key k, Typeface font ) {
    	this.mfont= font;
        this.text = String.valueOf(k.label);
        this.key = k;
        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(k.height/2);
        paint.setAntiAlias(true);
        //paint.setFakeBoldText(true);
        //paint.setShadowLayer(6f, 0, 0, Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        
        
    }
 
    public void setTypeface(Typeface tf){
    	paint.setTypeface(tf);
    	mfont = tf;
    }
    @Override
    public void draw(Canvas canvas) {
    	Rect bounds = new Rect();
    	paint.getTextBounds("ㄒ", 0, 1, bounds);
    	int h = key.height;
    	
    	float factor = 0.45f * h / bounds.height();
    	//paint.setTextSize(25);//paint.getTextSize() * factor);
    	paint.setTypeface(mfont);
    	
        canvas.drawText(text,0, 5 , paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

}
