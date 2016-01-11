package org.stream.split.voicenotification.Controls;

import android.content.Context;
import android.content.pm.PackageManager;
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
import android.util.AttributeSet;
import android.util.Log;
import android.util.StateSet;
import android.widget.CheckBox;

import org.stream.split.voicenotification.Enities.AppInfoEntity;
import org.stream.split.voicenotification.R;

/**
 * Created by split on 2016-01-05.
 */
public class ImageCheckBox extends CheckBox {

    public static String TAG = ImageCheckBox.class.getSimpleName();
    static int width = 70;
    static int height = 70;
    static int enterFade = 200;
    static int exitFade = 200;
    static int tickColor = Color.LTGRAY;
    static int tickID = R.drawable.ic_apply_applications;
    static Drawable tick;

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
        StateListDrawable sld = createStateListDrawable1(getContext(),icon,width,height);
        this.setButtonDrawable(sld);
    }

    private Drawable getCheckedDrawable(Context context, int drawableID, int backgroundColor, int tickWidth, int tickHeight) {
        Resources res = context.getResources();
        Drawable tick = res.getDrawable(drawableID);

        Bitmap tickBitmap = ((BitmapDrawable)tick).getBitmap();
        Drawable tickScaled = new BitmapDrawable(res,Bitmap.createScaledBitmap(tickBitmap,tickWidth,tickHeight,true));

        RoundRectShape rrs = new RoundRectShape(new float[] { 45, 45, 45, 45, 45, 45, 45, 45 }, null,null);
        ShapeDrawable sd = new ShapeDrawable(rrs);
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

        if(tick == null) {
            tick = getCheckedDrawable(context,tickID, tickColor,iconWidth,iconHeight);
        }
        sld.addState(new int[]{android.R.attr.state_checked}, tick);
        sld.addState(StateSet.WILD_CARD, t);

        sld.setEnterFadeDuration(enterFade);
        sld.setExitFadeDuration(exitFade);

        return sld;
    }
    private StateListDrawable createStateListDrawable(Context context, Drawable icon,int iconWidth, int iconHeight)
    {
        StateListDrawable sld = new StateListDrawable();

        Resources res = context.getResources();

        if(icon instanceof VectorDrawable) {
            icon.setBounds(0, 0, iconWidth, iconHeight);
            Bitmap bm = Bitmap.createBitmap(iconWidth,iconHeight, Bitmap.Config.ARGB_8888);
        }
        Bitmap iconBitmap = ((BitmapDrawable)icon).getBitmap();
        Drawable iconScaled = new BitmapDrawable(res,Bitmap.createScaledBitmap(iconBitmap,iconWidth,iconHeight,true));

        if(tick == null) {
            tick = getCheckedDrawable(context,tickID, tickColor,iconWidth,iconHeight);
        }
        sld.addState(new int[]{android.R.attr.state_checked}, tick);
        sld.addState(StateSet.WILD_CARD, iconScaled);

        sld.setEnterFadeDuration(enterFade);
        sld.setExitFadeDuration(exitFade);

        return sld;
    }
}

