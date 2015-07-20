
package com.talentpoc.charts;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;

import com.talentpoc.components.XAxis;
import com.talentpoc.data.ChartData;
import com.talentpoc.data.DataSet;
import com.talentpoc.data.Entry;
import com.talentpoc.utils.Utils;

/**
 * Baseclass of PieChart and RadarChart.
 * 
 * @author Philipp Jahoda
 */
public abstract class PieRadarChartBase<T extends ChartData<? extends DataSet<? extends Entry>>>
        extends Chart<T> {

    /** holds the normalized version of the current rotation angle of the chart */
    private float mRotationAngle = 270f;

    /** holds the raw version of the current rotation angle of the chart */
    private float mRawRotationAngle = 270f;


    public PieRadarChartBase(Context context) {
        super(context);
    }

    public PieRadarChartBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PieRadarChartBase(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void calcMinMax() {
        mDeltaX = mData.getXVals().size() - 1;
    }


    @Override
    public void notifyDataSetChanged() {
        if (mDataNotSet)
            return;

        calcMinMax();
        calculateOffsets();
    }

    @Override
    public void calculateOffsets() {


        float min = Utils.convertDpToPixel(10f);

        if (this instanceof RadarChart) {
            XAxis x = ((RadarChart) this).getXAxis();

            if (x.isEnabled()) {
                min = Math.max(Utils.convertDpToPixel(10f), x.mLabelWidth);
            }
        }

        float offsetBottom = Math.max(min, getRequiredBaseOffset());

        mViewPortHandler.restrainViewPort(min, min, min, offsetBottom);

    }

    /**
     * returns the angle relative to the chart center for the given point on the
     * chart in degrees. The angle is always between 0 and 360°, 0° is NORTH,
     * 90° is EAST, ...
     * 
     * @param x
     * @param y
     * @return
     */
    public float getAngleForPoint(float x, float y) {

        PointF c = getCenterOffsets();

        double tx = x - c.x, ty = y - c.y;
        double length = Math.sqrt(tx * tx + ty * ty);
        double r = Math.acos(ty / length);

        float angle = (float) Math.toDegrees(r);

        if (x > c.x)
            angle = 360f - angle;

        // add 90° because chart starts EAST
        angle = angle + 90f;

        // neutralize overflow
        if (angle > 360f)
            angle = angle - 360f;

        return angle;
    }

    /**
     * Calculates the position around a center point, depending on the distance
     * from the center, and the angle of the position around the center.
     * 
     * @param center
     * @param dist
     * @param angle in degrees, converted to radians internally
     * @return
     */
    protected PointF getPosition(PointF center, float dist, float angle) {

        PointF p = new PointF((float) (center.x + dist * Math.cos(Math.toRadians(angle))),
                (float) (center.y + dist * Math.sin(Math.toRadians(angle))));
        return p;
    }

    /**
     * Returns the distance of a certain point on the chart to the center of the
     * chart.
     *
     * @param x
     * @param y
     * @return
     */
    public float distanceToCenter(float x, float y) {

        PointF c = getCenterOffsets();

        float dist = 0f;

        float xDist = 0f;
        float yDist = 0f;

        if (x > c.x) {
            xDist = x - c.x;
        } else {
            xDist = c.x - x;
        }

        if (y > c.y) {
            yDist = y - c.y;
        } else {
            yDist = c.y - y;
        }

        // pythagoras
        dist = (float) Math.sqrt(Math.pow(xDist, 2.0) + Math.pow(yDist, 2.0));

        return dist;
    }

    /**
     * Returns the xIndex for the given angle around the center of the chart.
     * Returns -1 if not found / outofbounds.
     * 
     * @param angle
     * @return
     */
    public abstract int getIndexForAngle(float angle);

    /**
     * Set an offset for the rotation of the RadarChart in degrees. Default 270f
     * --> top (NORTH)
     * 
     * @param angle
     */
    public void setRotationAngle(float angle) {
        mRawRotationAngle = angle;
        mRotationAngle = Utils.getNormalizedAngle(mRawRotationAngle);
    }

    /**
     * gets the raw version of the current rotation angle of the pie chart the
     * returned value could be any value, negative or positive, outside of the
     * 360 degrees. this is used when working with rotation direction, mainly by
     * gestures and animations.
     *
     * @return
     */
    public float getRawRotationAngle() {
        return mRawRotationAngle;
    }

    /**
     * gets a normalized version of the current rotation angle of the pie chart,
     * which will always be between 0.0 < 360.0
     *
     * @return
     */
    public float getRotationAngle() {
        return mRotationAngle;
    }


    /**
     * returns the diameter of the pie- or radar-chart
     * 
     * @return
     */
    public float getDiameter() {
        RectF content = mViewPortHandler.getContentRect();
        return Math.min(content.width(), content.height());
    }

    /**
     * Returns the radius of the chart in pixels.
     * 
     * @return
     */
    public abstract float getRadius();

    /**
     * Returns the base offset needed for the chart without calculating the
     * legend size.
     * 
     * @return
     */
    protected abstract float getRequiredBaseOffset();

    @Override
    public float getYChartMax() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getYChartMin() {
        // TODO Auto-generated method stub
        return 0;
    }

}
