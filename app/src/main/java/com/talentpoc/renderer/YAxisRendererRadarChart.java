
package com.talentpoc.renderer;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.talentpoc.charts.RadarChart;
import com.talentpoc.components.YAxis;
import com.talentpoc.utils.Utils;
import com.talentpoc.utils.ViewPortHandler;

public class YAxisRendererRadarChart extends YAxisRenderer {

    private RadarChart mChart;
    public YAxisRendererRadarChart(ViewPortHandler viewPortHandler, YAxis yAxis, RadarChart chart) {
        super(viewPortHandler,yAxis,null);
        this.mChart = chart;
    }


    public void computeAxis(float yMin, float yMax) {
        computeAxisValues(yMin, yMax);
    }


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

            if (first == 0.0) // Fix for IEEE negative zero case (Where value == -0.0, and 0.0 == -0.0)
                first = 0.0;

            double last = Utils.nextUp(Math.floor(yMax / interval) * interval);

            double f;
            int i;
            int n = 0;
            for (f = first; f <= last; f += interval) {
                ++n;
            }

            if (Float.isNaN(mYAxis.getAxisMaxValue()))
                n += 1;

            mYAxis.mEntryCount = mYAxis.getLabelCount();

            if (mYAxis.mEntries.length < mYAxis.mEntryCount) {
                // Ensure stops contains at least numStops elements.
                mYAxis.mEntries = new float[mYAxis.mEntryCount];
            }

            for (f = interval+200, i = 0; i < mYAxis.getLabelCount(); f += interval, ++i) {
                mYAxis.mEntries[i] = (float) f;
            }
        }

        if (interval < 1) {
            mYAxis.mDecimals = (int) Math.ceil(-Math.log10(interval));
        } else {
            mYAxis.mDecimals = 0;
        }


        mYAxis.mAxisMaximum = mYAxis.mEntries[mYAxis.mEntryCount - 1];
        mYAxis.mAxisRange = Math.abs(mYAxis.mAxisMaximum - mYAxis.mAxisMinimum);
    }

    @Override
    public void renderAxisLabels(Canvas c) {

        if (!mYAxis.isEnabled() || !mYAxis.isDrawLabelsEnabled())
            return;

        mAxisLabelPaint.setTypeface(mYAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mYAxis.getTextSize());
        mAxisLabelPaint.setColor(mYAxis.getTextColor());

        PointF center = mChart.getCenterOffsets();
        float factor = mChart.getFactor();

        int labelCount = mYAxis.mEntryCount;

        for (int j = 0; j < labelCount; j++) {

            if (j == labelCount - 1 && mYAxis.isDrawTopYLabelEntryEnabled() == false)
                break;

            float r = (mYAxis.mEntries[j] - mYAxis.mAxisMinimum) * factor;

            PointF p = Utils.getPosition(center, r, mChart.getRotationAngle());

            String label = mYAxis.getFormattedLabel(j);

            c.drawText(label, p.x + 10, p.y, mAxisLabelPaint);
        }
    }




}
