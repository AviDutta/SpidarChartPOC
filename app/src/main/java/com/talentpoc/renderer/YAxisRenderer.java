
package com.talentpoc.renderer;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint.Align;

import com.talentpoc.components.YAxis;
import com.talentpoc.utils.PointD;
import com.talentpoc.utils.Transformer;
import com.talentpoc.utils.Utils;
import com.talentpoc.utils.ViewPortHandler;

public class YAxisRenderer extends AxisRenderer {

	protected YAxis mYAxis;

    public YAxisRenderer(ViewPortHandler viewPortHandler, YAxis yAxis, Transformer trans) {
        super(viewPortHandler, trans);

        this.mYAxis = yAxis;

        mAxisLabelPaint.setColor(Color.BLACK);
        mAxisLabelPaint.setTextSize(Utils.convertDpToPixel(10f));
    }

    /**
     * Computes the axis values.
     * 
     * @param yMin - the minimum y-value in the data object for this axis
     * @param yMax - the maximum y-value in the data object for this axis
     */
    public void computeAxis(float yMin, float yMax) {

        // calculate the starting and entry point of the y-labels (depending on
        // zoom / contentrect bounds)
        if (mViewPortHandler.contentWidth() > 10 && !mViewPortHandler.isFullyZoomedOutY()) {

            PointD p1 = mTrans.getValuesByTouchPoint(mViewPortHandler.contentLeft(),
                    mViewPortHandler.contentTop());
            PointD p2 = mTrans.getValuesByTouchPoint(mViewPortHandler.contentLeft(),
                    mViewPortHandler.contentBottom());

            if (!mYAxis.isInverted()) {
                yMin = (float) p2.y;
                yMax = (float) p1.y;
            } else {

                yMin = (float) p1.y;
                yMax = (float) p2.y;
            }
        }

        computeAxisValues(yMin, yMax);
    }

    /**
     * Sets up the y-axis labels. Computes the desired number of labels between
     * the two given extremes. Unlike the papareXLabels() method, this method
     * needs to be called upon every refresh of the view.
     * 
     * @return
     */
    protected void computeAxisValues(float min, float max) {

        float yMin = min;
        float yMax = max;

        int labelCount = mYAxis.getLabelCount();
        double range = Math.abs(yMax - yMin);

        if (labelCount == 0 || range <= 0) {
            mYAxis.mEntries = new float[] {};
            mYAxis.mEntryCount = 0;
            return;
        }

        double rawInterval = range / labelCount;
        double interval = Utils.roundToNextSignificant(rawInterval);
        double intervalMagnitude = Math.pow(10, (int) Math.log10(interval));
        int intervalSigDigit = (int) (interval / intervalMagnitude);
        if (intervalSigDigit > 5) {
            // Use one order of magnitude higher, to avoid intervals like 0.9 or
            // 90
            interval = Math.floor(10 * intervalMagnitude);
        }

        // if the labels should only show min and max
        if (mYAxis.isShowOnlyMinMaxEnabled()) {

            mYAxis.mEntryCount = 2;
            mYAxis.mEntries = new float[2];
            mYAxis.mEntries[0] = yMin;
            mYAxis.mEntries[1] = yMax;

        } else {

            double first = Math.ceil(yMin / interval) * interval;
            double last = Utils.nextUp(Math.floor(yMax / interval) * interval);

            double f;
            int i;
            int n = 0;
            for (f = first; f <= last; f += interval) {
                ++n;
            }

            mYAxis.mEntryCount = n;

            if (mYAxis.mEntries.length < n) {
                // Ensure stops contains at least numStops elements.
                mYAxis.mEntries = new float[n];
            }

            for (f = first, i = 0; i < n; f += interval, ++i) {
                mYAxis.mEntries[i] = (float) f;
            }
        }

        if (interval < 1) {
            mYAxis.mDecimals = (int) Math.ceil(-Math.log10(interval));
        } else {
            mYAxis.mDecimals = 0;
        }
    }

    /**
     * draws the y-axis labels to the screen
     */
    @Override
    public void renderAxisLabels(Canvas c) {

        if (!mYAxis.isEnabled() || !mYAxis.isDrawLabelsEnabled())
            return;

        float[] positions = new float[mYAxis.mEntryCount * 2];

        for (int i = 0; i < positions.length; i += 2) {
            // only fill y values, x values are not needed since the y-labels
            // are
            // static on the x-axis
            positions[i + 1] = mYAxis.mEntries[i / 2];
        }

        mTrans.pointValuesToPixel(positions);

        mAxisLabelPaint.setTypeface(mYAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mYAxis.getTextSize());
        mAxisLabelPaint.setColor(mYAxis.getTextColor());

        float xoffset = mYAxis.getXOffset();
        float yoffset = Utils.calcTextHeight(mAxisLabelPaint, "A") / 2.5f + mYAxis.getYOffset();

        YAxis.AxisDependency dependency = mYAxis.getAxisDependency();
        YAxis.YAxisLabelPosition labelPosition = mYAxis.getLabelPosition();

        float xPos = 0f;

        if (dependency == YAxis.AxisDependency.LEFT) {

            if (labelPosition == YAxis.YAxisLabelPosition.OUTSIDE_CHART) {
                mAxisLabelPaint.setTextAlign(Align.RIGHT);
                xPos = mViewPortHandler.offsetLeft() - xoffset;
            } else {
                mAxisLabelPaint.setTextAlign(Align.LEFT);
                xPos = mViewPortHandler.offsetLeft() + xoffset;
            }

        } else {

            if (labelPosition == YAxis.YAxisLabelPosition.OUTSIDE_CHART) {
                mAxisLabelPaint.setTextAlign(Align.LEFT);
                xPos = mViewPortHandler.contentRight() + xoffset;
            } else {
                mAxisLabelPaint.setTextAlign(Align.RIGHT);
                xPos = mViewPortHandler.contentRight() - xoffset;
            }
        }

        drawYLabels(c, xPos, positions, yoffset);
    }


    public void renderAxisLine(Canvas c) {

        if (!mYAxis.isEnabled() || !mYAxis.isDrawAxisLineEnabled())
            return;

        mAxisLinePaint.setColor(mYAxis.getAxisLineColor());
        mAxisLinePaint.setStrokeWidth(mYAxis.getAxisLineWidth());

        if (mYAxis.getAxisDependency() == YAxis.AxisDependency.LEFT) {
            c.drawLine(mViewPortHandler.contentLeft(),
                    mViewPortHandler.contentTop(), mViewPortHandler.contentLeft(),
                    mViewPortHandler.contentBottom(), mAxisLinePaint);
        } else {
            c.drawLine(mViewPortHandler.contentRight(),
                    mViewPortHandler.contentTop(), mViewPortHandler.contentRight(),
                    mViewPortHandler.contentBottom(), mAxisLinePaint);
        }
    }

    /**
     * draws the y-labels on the specified x-position
     * 
     * @param fixedPosition
     * @param positions
     */
    protected void drawYLabels(Canvas c, float fixedPosition, float[] positions, float offset) {

        // draw
        for (int i = 0; i < mYAxis.mEntryCount; i++) {

            String text = mYAxis.getFormattedLabel(i);

            if (!mYAxis.isDrawTopYLabelEntryEnabled() && i >= mYAxis.mEntryCount - 1)
                return;

            c.drawText(text, fixedPosition, positions[i * 2 + 1] + offset, mAxisLabelPaint);
        }
    }

}
