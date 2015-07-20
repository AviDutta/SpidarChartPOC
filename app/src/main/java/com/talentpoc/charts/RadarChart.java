
package com.talentpoc.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.talentpoc.components.XAxis;
import com.talentpoc.components.YAxis;
import com.talentpoc.data.RadarData;
import com.talentpoc.listener.ChartLabelSelectedListener;
import com.talentpoc.renderer.RadarChartRenderer;
import com.talentpoc.renderer.XAxisRendererRadarChart;
import com.talentpoc.renderer.YAxisRendererRadarChart;
import com.talentpoc.utils.Utils;

/**
 * Implementation of the RadarChart, a "spidernet"-like chart. It works best
 * when displaying 5-10 entries per DataSet.
 *
 * @author Philipp Jahoda
 */
public class RadarChart extends PieRadarChartBase<RadarData> implements View.OnTouchListener {

    private static final int MAX_TOUCH_DISTANCE=50;

    /**
     * width of the main web lines
     */
    private float mWebLineWidth = 2.5f;

    /**
     * width of the inner web lines
     */
    private float mInnerWebLineWidth = 1.5f;

    /**
     * color for the main web lines
     */
    private int mWebColor = Color.rgb(122, 122, 122);

    /**
     * color for the inner web
     */
    private int mWebColorInner = Color.rgb(122, 122, 122);

    /**
     * transparency the grid is drawn with (0-255)
     */
    private int mWebAlpha = 150;

    /**
     * flag indicating if the web lines should be drawn or not
     */
    private boolean mDrawWeb = true;

    /**
     * the object reprsenting the y-axis labels
     */
    private YAxis mYAxis;

    /**
     * the object representing the x-axis labels
     */
    private XAxis mXAxis;

    private ChartLabelSelectedListener mChartLabelSelectedListener;

    protected YAxisRendererRadarChart mYAxisRenderer;
    protected XAxisRendererRadarChart mXAxisRenderer;

    public RadarChart(Context context) {
        super(context);
        setOnTouchListener(this);
    }

    public RadarChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnTouchListener(this);
    }

    public RadarChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnTouchListener(this);
    }


    public ChartLabelSelectedListener getChartLabelSelectedListener() {
        return mChartLabelSelectedListener;
    }

    public void setChartLabelSelectedListener(ChartLabelSelectedListener mChartLabelSelectedListener) {
        this.mChartLabelSelectedListener = mChartLabelSelectedListener;
    }

    @Override
    protected void init() {
        super.init();

        mYAxis = new YAxis(YAxis.AxisDependency.LEFT);
        mXAxis = new XAxis();
        mXAxis.setSpaceBetweenLabels(0);

        mWebLineWidth = Utils.convertDpToPixel(1.5f);
        mInnerWebLineWidth = Utils.convertDpToPixel(0.75f);

        mRenderer = new RadarChartRenderer(this, mViewPortHandler);
        mYAxisRenderer = new YAxisRendererRadarChart(mViewPortHandler, mYAxis, this);
        mXAxisRenderer = new XAxisRendererRadarChart(mViewPortHandler, mXAxis, this);
    }

    @Override
    protected void calcMinMax() {
        super.calcMinMax();

        float minLeft = mData.getYMin(YAxis.AxisDependency.LEFT);
        float maxLeft = mData.getYMax(YAxis.AxisDependency.LEFT);

        mXChartMax = mData.getXVals().size() - 1;
        mDeltaX = Math.abs(mXChartMax - mXChartMin);

        float leftRange = Math.abs(maxLeft - (mYAxis.isStartAtZeroEnabled() ? 0 : minLeft));

        float topSpaceLeft = leftRange / 100f * mYAxis.getSpaceTop();
        float bottomSpaceLeft = leftRange / 100f * mYAxis.getSpaceBottom();

        mXChartMax = mData.getXVals().size() - 1;
        mDeltaX = Math.abs(mXChartMax - mXChartMin);

        mYAxis.mAxisMaximum = !Float.isNaN(mYAxis.getAxisMaxValue()) ? mYAxis
                .getAxisMaxValue() : maxLeft + topSpaceLeft;
        mYAxis.mAxisMinimum = !Float.isNaN(mYAxis.getAxisMinValue()) ? mYAxis
                .getAxisMinValue() : minLeft - bottomSpaceLeft;

        // consider starting at zero (0)
        if (mYAxis.isStartAtZeroEnabled())
            mYAxis.mAxisMinimum = 0f;

        mYAxis.mAxisRange = Math.abs(mYAxis.mAxisMaximum - mYAxis.mAxisMinimum);
    }


    @Override
    public void notifyDataSetChanged() {
        if (mDataNotSet)
            return;

        calcMinMax();

        if (mYAxis.needsDefaultFormatter()) {
            mYAxis.setValueFormatter(mDefaultFormatter);
        }

        mYAxisRenderer.computeAxis(mYAxis.mAxisMinimum, mYAxis.mAxisMaximum);
        mXAxisRenderer.computeAxis(mData.getXValAverageLength(), mData.getXVals());

        calculateOffsets();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mDataNotSet)
            return;

        mXAxisRenderer.renderAxisLabels(canvas);

        if (mDrawWeb)
            mRenderer.drawExtras(canvas);

        mRenderer.drawData(canvas);

        mYAxisRenderer.renderAxisLabels(canvas);

      //  mRenderer.drawValues(canvas);

    }

    /**
     * Returns the factor that is needed to transform values into pixels.
     *
     * @return
     */
    public float getFactor() {
        RectF content = mViewPortHandler.getContentRect();
        return (float) Math.min(content.width() / 2f, content.height() / 2f)
                / mYAxis.mAxisRange;
    }

    /**
     * Returns the angle that each slice in the radar chart occupies.
     *
     * @return
     */
    public float getSliceAngle() {
        return 360f / (float) mData.getXValCount();
    }

    @Override
    public int getIndexForAngle(float angle) {

        // take the current angle of the chart into consideration
        float a = Utils.getNormalizedAngle(angle - getRotationAngle());

        float sliceangle = getSliceAngle();

        for (int i = 0; i < mData.getXValCount(); i++) {
            if (sliceangle * (i + 1) - sliceangle / 2f > a)
                return i;
        }

        return 0;
    }

    /**
     * Returns the object that represents all y-labels of the RadarChart.
     *
     * @return
     */
    public YAxis getYAxis() {
        return mYAxis;
    }

    /**
     * Returns the object that represents all x-labels that are placed around
     * the RadarChart.
     *
     * @return
     */
    public XAxis getXAxis() {
        return mXAxis;
    }

    /**
     * Sets the width of the web lines that come from the center.
     *
     * @param width
     */
    public void setWebLineWidth(float width) {
        mWebLineWidth = Utils.convertDpToPixel(width);
    }

    public float getWebLineWidth() {
        return mWebLineWidth;
    }

    /**
     * Sets the width of the web lines that are in between the lines coming from
     * the center.
     *
     * @param width
     */
    public void setWebLineWidthInner(float width) {
        mInnerWebLineWidth = Utils.convertDpToPixel(width);
    }

    public float getWebLineWidthInner() {
        return mInnerWebLineWidth;
    }

    /**
     * Sets the transparency (alpha) value for all web lines, default: 150, 255
     * = 100% opaque, 0 = 100% transparent
     *
     * @param alpha
     */
    public void setWebAlpha(int alpha) {
        mWebAlpha = alpha;
    }

    /**
     * Returns the alpha value for all web lines.
     *
     * @return
     */
    public int getWebAlpha() {
        return mWebAlpha;
    }

    /**
     * Sets the color for the web lines that come from the center. Don't forget
     * to use getResources().getColor(...) when loading a color from the
     * resources. Default: Color.rgb(122, 122, 122)
     *
     * @param color
     */
    public void setWebColor(int color) {
        mWebColor = color;
    }

    public int getWebColor() {
        return mWebColor;
    }

    /**
     * Sets the color for the web lines in between the lines that come from the
     * center. Don't forget to use getResources().getColor(...) when loading a
     * color from the resources. Default: Color.rgb(122, 122, 122)
     *
     * @param color
     */
    public void setWebColorInner(int color) {
        mWebColorInner = color;
    }

    public int getWebColorInner() {
        return mWebColorInner;
    }

    /**
     * If set to true, drawing the web is enabled, if set to false, drawing the
     * whole web is disabled. Default: true
     *
     * @param enabled
     */
    public void setDrawWeb(boolean enabled) {
        mDrawWeb = enabled;
    }


    @Override
    protected float getRequiredBaseOffset() {
        return mXAxis.isEnabled() ? mXAxis.mLabelWidth : Utils.convertDpToPixel(10f);
    }

    @Override
    public float getRadius() {
        RectF content = mViewPortHandler.getContentRect();
        return Math.min(content.width() / 2f, content.height() / 2f);
    }

    /**
     * Returns the maximum value this chart can display on it's y-axis.
     */
    public float getYChartMax() {
        return mYAxis.mAxisMaximum;
    }

    /**
     * Returns the minimum value this chart can display on it's y-axis.
     */
    public float getYChartMin() {
        return mYAxis.mAxisMinimum;
    }

    /**
     * Returns the range of y-values this chart can display.
     *
     * @return
     */
    public float getYRange() {
        return mYAxis.mAxisRange;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                for (int i = 0; i < mXAxis.getLabelPosition().size(); i++) {
                    PointF startPoint = mXAxis.getLabelPosition().get(i);
                    float dx = Utils.distance(event.getX(), startPoint.x, event.getY(), startPoint.y);
                    if (dx <MAX_TOUCH_DISTANCE ) {
                        mChartLabelSelectedListener.onLabelSelected(i);
                        return true;
                    }

                }
            }
        }
        return false;
    }
}
