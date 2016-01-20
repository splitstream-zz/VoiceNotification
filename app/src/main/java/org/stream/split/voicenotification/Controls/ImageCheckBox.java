package org.stream.split.voicenotification.Controls;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.VectorDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.CheckBox;

import org.stream.split.voicenotification.R;

/**
 * Created by split on 2016-01-05.
 */
public class ImageCheckBox extends CheckBox {

    public static String TAG = ImageCheckBox.class.getSimpleName();
    int mWidth = 70;
     int mHeight = 70;
     int mEnterFade = 200;
     int mExitFade = 200;
     int mTickColor = Color.LTGRAY;
     int mTickID = R.drawable.ic_apply_applications;
    RoundRectShape mBackgroundShape = new RoundRectShape(new float[] { 45, 45, 45, 45, 45, 45, 45, 45 }, null,null);
     Drawable mTick;

    public ImageCheckBox(Context context)
    {
        super(context);
    }

    public ImageCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ImageCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initialize(Drawable icon) {
        StateListDrawable sld = createStateListDrawable1(getContext(),icon, mWidth, mHeight);
        this.setButtonDrawable(sld);
    }
    public void initialize(Drawable icon,int width,int height, int color, RoundRectShape backgroundShape) {
        mWidth = width;
        mHeight = height;
        mTickColor = color;
        mBackgroundShape = backgroundShape;
        StateListDrawable sld = createStateListDrawable1(getContext(),icon,width,height);
        this.setButtonDrawable(sld);
    }

    private Drawable getCheckedDrawable(Context context, int drawableID, int backgroundColor, int tickWidth, int tickHeight, RoundRectShape backgroundShape) {
        Resources res = context.getResources();
        Drawable tick = res.getDrawable(drawableID);

        Bitmap tickBitmap = ((BitmapDrawable)tick).getBitmap();
        Drawable tickScaled = new BitmapDrawable(res,Bitmap.createScaledBitmap(tickBitmap,tickWidth,tickHeight,true));


        ShapeDrawable sd = new ShapeDrawable(backgroundShape);
        sd.getPaint().setColor(backgroundColor);
        LayerDrawable ld = new LayerDrawable(new Drawable[]{sd,tickScaled});
        return  ld;
    }

    private StateListDrawable createStateListDrawable1(Context context, Drawable icon,int iconWidth, int iconHeight)
    {
        StateListDrawable sld = new StateListDrawable();
        Resources res = context.getResources();

        icon.setBounds(0, 0, iconWidth, iconHeight);
        Bitmap bm = Bitmap.createBitmap(iconWidth,iconHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        icon.draw(canvas);
        Drawable t = new BitmapDrawable(res,bm);

        if(mTick == null) {
            mTick = getCheckedDrawable(context, mTickID, mTickColor,iconWidth,iconHeight, mBackgroundShape);
        }
        sld.addState(new int[]{android.R.attr.state_checked}, mTick);
        sld.addState(StateSet.WILD_CARD, t);

        sld.setEnterFadeDuration(mEnterFade);
        sld.setExitFadeDuration(mExitFade);

        return sld;
    }
//    private StateListDrawable createStateListDrawable(Context context, Drawable icon,int iconWidth, int iconHeight)
//    {
//        StateListDrawable sld = new StateListDrawable();
//
//        Resources res = context.getResources();
//
//        if(icon instanceof VectorDrawable) {
//            icon.setBounds(0, 0, iconWidth, iconHeight);
//            Bitmap bm = Bitmap.createBitmap(iconWidth,iconHeight, Bitmap.Config.ARGB_8888);
//        }
//        Bitmap iconBitmap = ((BitmapDrawable)icon).getBitmap();
//        Drawable iconScaled = new BitmapDrawable(res,Bitmap.createScaledBitmap(iconBitmap,iconWidth,iconHeight,true));
//
//        if(mTick == null) {
//            mTick = getCheckedDrawable(context, mTickID, mTickColor,iconWidth,iconHeight);
//        }
//        sld.addState(new int[]{android.R.attr.state_checked}, mTick);
//        sld.addState(StateSet.WILD_CARD, iconScaled);
//
//        sld.setEnterFadeDuration(mEnterFade);
//        sld.setExitFadeDuration(mExitFade);
//
//        return sld;
//    }
}

