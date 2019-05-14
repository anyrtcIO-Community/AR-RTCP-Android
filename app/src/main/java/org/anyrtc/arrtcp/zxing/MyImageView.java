package org.anyrtc.arrtcp.zxing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;

/**
 * Created by liuxiaozhong on 2016/9/29.
 */

public class MyImageView extends android.support.v7.widget.AppCompatImageView {
    private Context context;
    public MyImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }
    public MyImageView(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();

        Paint paint = new Paint();
        paint.setColor(Color.rgb(211,51,60));
        paint.setAntiAlias(true);
        paint.setStrokeWidth(t(5));

        canvas.drawLine(0, 0, 0, t(18), paint);
        canvas.drawLine(0, 0, t(18), 0, paint);

        canvas.drawLine(0, height - t(18), 0, height, paint);
        canvas.drawLine(0, height, t(18),height,paint);

        canvas.drawLine(width-t(18), 0, width, 0, paint);
        canvas.drawLine(width, 0, width,t(18),paint);

        canvas.drawLine(width, height-t(18), width, height, paint);
        canvas.drawLine(width-t(18), height, width,height,paint);
    }
    public  int dp2px(float dpVal)
    {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    public int t(float dpVal){
        return dp2px(dpVal);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
    }
}
