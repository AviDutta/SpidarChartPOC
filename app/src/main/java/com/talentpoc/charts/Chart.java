
package com.talentpoc.charts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewParent;
import android.widget.RelativeLayout;

import com.talentpoc.data.ChartData;
import com.talentpoc.data.DataSet;
import com.talentpoc.data.Entry;
import com.talentpoc.interfaces.ChartInterface;
import com.talentpoc.renderer.DataRenderer;
import com.talentpoc.utils.DefaultValueFormatter;
import com.talentpoc.utils.Utils;
import com.talentpoc.utils.ValueFormatter;
import com.talentpoc.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Baseclass of all Chart-Views.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("NewApi")
public abstract class Chart<T extends ChartData<? extends DataSet<? extends Entry>>> extends
        RelativeLayout
        implements ChartInterface {

    public static final String LOG_TAG = "TalentPOC";

    /**
     * flag that indicates if logging is enabled or not
     */
    protected boolean mLogEnabled = false;

    /**
     * object that holds all data that was originally set for the chart, before
     * it was modified or any filtering algorithms had been applied
     */
    protected T mData = null;

    /**
     * default value-formatter, number of digits depends on provided chart-data
     */
    protected ValueFormatter mDefaultFormatter;

    /**
     * paint object used for drawing the description text in the bottom right
     * corner of the chart
     */
    protected Paint mDescPaint;

    /**
     * paint object for drawing the information text when there are no values in
     * the chart
     */
    protected Paint mInfoPaint;

    /**
     * flag that indicates if the chart has been fed with data yet
     */
    protected boolean mDataNotSet = true;


    /**
     * the number of x-values the chart displays
     */
    protected float mDeltaX = 1f;

    protected float mXChartMin = 0f;
    protected float mXChartMax = 0f;

    /**
     * if true, touch gestures are enabled on the chart
     */
    protected boolean mTouchEnabled = true;

    /**
     * text that is displayed when the chart is empty
     */
    private String mNoDataText = "No chart data available.";


    /**
     * text that is displayed when the chart is empty that describes why the
     * chart is empty
     */
    private String mNoDataTextDescription;


    /**
     * object responsible for rendering the data
     */
    protected DataRenderer mRenderer;

    /**
     * object that manages the bounds and drawing constraints of the chart
     */
    protected ViewPortHandler mViewPortHandler;


    /**
     * Extra offsets to be appended to the viewport
     */
    private float mExtraTopOffset = 0.f,
            mExtraRightOffset = 0.f,
            mExtraBottomOffset = 0.f,
            mExtraLeftOffset = 0.f;

    /**
     * default constructor for initialization in code
     */
    public Chart(Context context) {
        super(context);
        init();
    }

    /**
     * constructor for initialization in xml
     */
    public Chart(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * even more awesome constructor
     */
    public Chart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    /**
     * initialize all paints and stuff
     */
    protected void init() {

        setWillNotDraw(false);
        // setLayerType(View.LAYER_TYPE_HARDWARE, null);
        // initialize the utils
        Utils.init(getContext());

        mDefaultFormatter = new DefaultValueFormatter(1);

        mViewPortHandler = new ViewPortHandler();

        mDescPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDescPaint.setColor(Color.BLACK);
        mDescPaint.setTextAlign(Align.RIGHT);
        mDescPaint.setTextSize(Utils.convertDpToPixel(9f));

        mInfoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInfoPaint.setColor(Color.rgb(247, 189, 51)); // orange
        mInfoPaint.setTextAlign(Align.CENTER);
        mInfoPaint.setTextSize(Utils.convertDpToPixel(12f));

        mDrawPaint = new Paint(Paint.DITHER_FLAG);

        if (mLogEnabled)
            Log.i("", "Chart.init()");
    }


    /**
     * Sets a new data object for the chart. The data object contains all values
     * and information needed for displaying.
     *
     * @param data
     */
    public void setData(T data) {

        if (data == null) {
            Log.e(LOG_TAG,
                    "Cannot set data for chart. Provided data object is null.");
            return;
        }

        // LET THE CHART KNOW THERE IS DATA
        mDataNotSet = false;
        mOffsetsCalculated = false;
        mData = data;

        // calculate how many digits are needed
        calculateFormatter(data.getYMin(), data.getYMax());

        for (DataSet<?> set : mData.getDataSets()) {
            if (set.needsDefaultFormatter())
                set.setValueFormatter(mDefaultFormatter);
        }

        // let the chart know there is new data
        notifyDataSetChanged();

        if (mLogEnabled)
            Log.i(LOG_TAG, "Data is set.");
    }

    /**
     * Lets the chart know its underlying data has changed and performs all
     * necessary recalculations.
     */
    public abstract void notifyDataSetChanged();

    /**
     * calculates the offsets of the chart to the border depending on the
     * position of an eventual legend or depending on the length of the y-axis
     * and x-axis labels and their position
     */
    protected abstract void calculateOffsets();

    /**
     * calcualtes the y-min and y-max value and the y-delta and x-delta value
     */
    protected abstract void calcMinMax();

    /**
     * calculates the required number of digits for the values that might be
     * drawn in the chart (if enabled), and creates the default-value-formatter
     */
    protected void calculateFormatter(float min, float max) {

        float reference = 0f;

        if (mData == null || mData.getXValCount() < 2) {

            reference = Math.max(Math.abs(min), Math.abs(max));
        } else {
            reference = Math.abs(max - min);
        }

        int digits = Utils.getDecimals(reference);
        mDefaultFormatter = new DefaultValueFormatter(digits);
    }

    /**
     * flag that indicates if offsets calculation has already been done or not
     */
    private boolean mOffsetsCalculated = false;

    /**
     * Bitmap object used for drawing. This is necessary because hardware
     * acceleration uses OpenGL which only allows a specific texture size to be
     * drawn on the canvas directly.
     */
    protected Bitmap mDrawBitmap;

    /**
     * paint object used for drawing the bitmap
     */
    protected Paint mDrawPaint;

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);

        if (mDataNotSet || mData == null || mData.getYValCount() <= 0) { // check
            // if
            // there
            // is
            // data

            // if no data, inform the user
            canvas.drawText(mNoDataText, getWidth() / 2, getHeight() / 2, mInfoPaint);

            if (!TextUtils.isEmpty(mNoDataTextDescription)) {
                float textOffset = -mInfoPaint.ascent() + mInfoPaint.descent();
                canvas.drawText(mNoDataTextDescription, getWidth() / 2, (getHeight() / 2)
                        + textOffset, mInfoPaint);
            }
            return;
        }

        if (!mOffsetsCalculated) {

            calculateOffsets();
            mOffsetsCalculated = true;
        }


    }


    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS ONLY GETTERS AND SETTERS */

    /**
     * Returns the default ValueFormatter that has been determined by the chart
     * considering the provided minimum and maximum values.
     *
     * @return
     */
    public ValueFormatter getDefaultValueFormatter() {
        return mDefaultFormatter;
    }

    @Override
    public float getXChartMax() {
        return mXChartMax;
    }

    @Override
    public float getXChartMin() {
        return mXChartMin;
    }

    @Override
    public int getXValCount() {
        return mData.getXValCount();
    }


    /**
     * Returns the center point of the chart (the whole View) in pixels.
     *
     * @return
     */
    public PointF getCenter() {
        return new PointF(getWidth() / 2f, getHeight() / 2f);
    }

    /**
     * Returns the center of the chart taking offsets under consideration.
     * (returns the center of the content rectangle)
     *
     * @return
     */
    @Override
    public PointF getCenterOffsets() {
        return mViewPortHandler.getContentCenter();
    }


    /**
     * Sets extra offsets (around the chart view) to be appended to the
     * auto-calculated offsets.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    public void setExtraOffsets(float left, float top, float right, float bottom) {
        setExtraLeftOffset(left);
        setExtraTopOffset(top);
        setExtraRightOffset(right);
        setExtraBottomOffset(bottom);
    }

    /**
     * Set an extra offset to be appended to the viewport's top
     */
    public void setExtraTopOffset(float offset) {
        mExtraTopOffset = Utils.convertDpToPixel(offset);
    }

    /**
     * @return the extra offset to be appended to the viewport's top
     */
    public float getExtraTopOffset() {
        return mExtraTopOffset;
    }

    /**
     * Set an extra offset to be appended to the viewport's right
     */
    public void setExtraRightOffset(float offset) {
        mExtraRightOffset = Utils.convertDpToPixel(offset);
    }

    /**
     * @return the extra offset to be appended to the viewport's right
     */
    public float getExtraRightOffset() {
        return mExtraRightOffset;
    }

    /**
     * Set an extra offset to be appended to the viewport's bottom
     */
    public void setExtraBottomOffset(float offset) {
        mExtraBottomOffset = Utils.convertDpToPixel(offset);
    }

    /**
     * @return the extra offset to be appended to the viewport's bottom
     */
    public float getExtraBottomOffset() {
        return mExtraBottomOffset;
    }

    /**
     * Set an extra offset to be appended to the viewport's left
     */
    public void setExtraLeftOffset(float offset) {
        mExtraLeftOffset = Utils.convertDpToPixel(offset);
    }

    /**
     * @return the extra offset to be appended to the viewport's left
     */
    public float getExtraLeftOffset() {
        return mExtraLeftOffset;
    }

    /**
     * Set this to true to enable logcat outputs for the chart. Default:
     * disabled
     *
     * @param enabled
     */
    public void setLogEnabled(boolean enabled) {
        mLogEnabled = enabled;
    }

    /**
     * Returns true if log-output is enabled for the chart, fals if not.
     *
     * @return
     */
    public boolean isLogEnabled() {
        return mLogEnabled;
    }

    /**
     * Sets the text that informs the user that there is no data available with
     * which to draw the chart.
     *
     * @param text
     */
    public void setNoDataText(String text) {
        mNoDataText = text;
    }

    /**
     * Sets descriptive text to explain to the user why there is no chart
     * available Defaults to empty if not set
     *
     * @param text
     */
    public void setNoDataTextDescription(String text) {
        mNoDataTextDescription = text;
    }

    /**
     * Set this to false to disable all gestures and touches on the chart,
     * default: true
     *
     * @param enabled
     */
    public void setTouchEnabled(boolean enabled) {
        this.mTouchEnabled = enabled;
    }


    /**
     * Returns the rectangle that defines the borders of the chart-value surface
     * (into which the actual values are drawn).
     *
     * @return
     */
    @Override
    public RectF getContentRect() {
        return mViewPortHandler.getContentRect();
    }

    /**
     * disables intercept touchevents
     */
    public void disableScroll() {
        ViewParent parent = getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(true);
    }

    /**
     * enables intercept touchevents
     */
    public void enableScroll() {
        ViewParent parent = getParent();
        if (parent != null)
            parent.requestDisallowInterceptTouchEvent(false);
    }

    /**
     * paint for the grid background (only line and barchart)
     */
    public static final int PAINT_GRID_BACKGROUND = 4;

    /**
     * paint for the info text that is displayed when there are no values in the
     * chart
     */
    public static final int PAINT_INFO = 7;

    /**
     * paint for the description text in the bottom right corner
     */
    public static final int PAINT_DESCRIPTION = 11;

    /**
     * paint for the hole in the middle of the pie chart
     */
    public static final int PAINT_HOLE = 13;

    /**
     * paint for the text in the middle of the pie chart
     */
    public static final int PAINT_CENTER_TEXT = 14;

    /**
     * paint used for the legend
     */
    public static final int PAINT_LEGEND_LABEL = 18;

    /**
     * set a new paint object for the specified parameter in the chart e.g.
     * Chart.PAINT_VALUES
     *
     * @param p     the new paint object
     * @param which Chart.PAINT_VALUES, Chart.PAINT_GRID, Chart.PAINT_VALUES,
     *              ...
     */
    public void setPaint(Paint p, int which) {

        switch (which) {
            case PAINT_INFO:
                mInfoPaint = p;
                break;
            case PAINT_DESCRIPTION:
                mDescPaint = p;
                break;
        }
    }

    /**
     * Returns the paint object associated with the provided constant.
     *
     * @param which e.g. Chart.PAINT_LEGEND_LABEL
     * @return
     */
    public Paint getPaint(int which) {
        switch (which) {
            case PAINT_INFO:
                return mInfoPaint;
            case PAINT_DESCRIPTION:
                return mDescPaint;
        }

        return null;
    }

    /**
     * returns the x-value at the given index
     *
     * @param index
     * @return
     */
    public String getXValue(int index) {
        if (mData == null || mData.getXValCount() <= index)
            return null;
        else
            return mData.getXVals().get(index);
    }

    /**
     * Get all Entry objects at the given index across all DataSets.
     * INFORMATION: This method does calculations at runtime. Do not over-use in
     * performance critical situations.
     *
     * @param xIndex
     * @return
     */
    public List<Entry> getEntriesAtIndex(int xIndex) {

        List<Entry> vals = new ArrayList<Entry>();

        for (int i = 0; i < mData.getDataSetCount(); i++) {

            DataSet<? extends Entry> set = mData.getDataSetByIndex(i);

            Entry e = set.getEntryForXIndex(xIndex);

            if (e != null) {
                vals.add(e);
            }
        }

        return vals;
    }

    /**
     * Returns the ChartData object that has been set for the chart.
     *
     * @return
     */
    public T getData() {
        return mData;
    }

    /**
     * Returns the ViewPortHandler of the chart that is responsible for the
     * content area of the chart and its offsets and dimensions.
     *
     * @return
     */
    public ViewPortHandler getViewPortHandler() {
        return mViewPortHandler;
    }

    /**
     * Returns the Renderer object the chart uses for drawing data.
     *
     * @return
     */
    public DataRenderer getRenderer() {
        return mRenderer;
    }

    /**
     * Sets a new DataRenderer object for the chart.
     *
     * @param renderer
     */
    public void setRenderer(DataRenderer renderer) {

        if (renderer != null)
            mRenderer = renderer;
    }

    @Override
    public PointF getCenterOfView() {
        return getCenter();
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = (int) Utils.convertDpToPixel(50f);
        setMeasuredDimension(
                Math.max(getSuggestedMinimumWidth(),
                        resolveSize(size,
                                widthMeasureSpec)),
                Math.max(getSuggestedMinimumHeight(),
                        resolveSize(size,
                                heightMeasureSpec)));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mLogEnabled)
            Log.i(LOG_TAG, "OnSizeChanged()");

        if (w > 0 && h > 0 && w < 10000 && h < 10000) {
            // create a new bitmap with the new dimensions

            if (mDrawBitmap != null)
                mDrawBitmap.recycle();

            mDrawBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_4444);
            mViewPortHandler.setChartDimens(w, h);

            if (mLogEnabled)
                Log.i(LOG_TAG, "Setting chart dimens, width: " + w + ", height: " + h);

        }

        notifyDataSetChanged();

        super.onSizeChanged(w, h, oldw, oldh);
    }
}
