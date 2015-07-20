
package com.talentpoc.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;

import com.talentpoc.data.DataSet;
import com.talentpoc.utils.Utils;
import com.talentpoc.utils.ViewPortHandler;

public abstract class DataRenderer extends Renderer {

    /** main paint object used for rendering */
    protected Paint mRenderPaint;

    protected Paint mDrawPaint;

    /**
     * paint object for drawing values (text representing values of chart
     * entries)
     */
    protected Paint mValuePaint;

    public DataRenderer(ViewPortHandler viewPortHandler) {
        super(viewPortHandler);
        mRenderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRenderPaint.setStyle(Style.FILL);

        mDrawPaint = new Paint(Paint.DITHER_FLAG);

        mValuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mValuePaint.setColor(Color.rgb(63, 63, 63));
        mValuePaint.setTextAlign(Align.CENTER);
        mValuePaint.setTextSize(Utils.convertDpToPixel(9f));

    }

    /**
     * Returns the Paint object this renderer uses for drawing the values
     * (value-text).
     * 
     * @return
     */
    public Paint getPaintValues() {
        return mValuePaint;
    }

    /**
     * Returns the Paint object used for rendering.
     * 
     * @return
     */
    public Paint getPaintRender() {
        return mRenderPaint;
    }

    /**
     * Applies the required styling (provided by the DataSet) to the value-paint
     * object.
     * 
     * @param set
     */
    protected void applyValueTextStyle(DataSet<?> set) {

        mValuePaint.setColor(set.getValueTextColor());
        mValuePaint.setTypeface(set.getValueTypeface());
        mValuePaint.setTextSize(set.getValueTextSize());
    }

    /**
     * Initializes the buffers used for rendering with a new size. Since this
     * method performs memory allocations, it should only be called if
     * necessary.
     */
    public abstract void initBuffers();

    public abstract void drawData(Canvas c);

    public abstract void drawValues(Canvas c);

    public abstract void drawExtras(Canvas c);


}
