
package com.talentpoc.renderer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import com.talentpoc.charts.RadarChart;
import com.talentpoc.data.Entry;
import com.talentpoc.data.RadarData;
import com.talentpoc.data.RadarDataSet;
import com.talentpoc.utils.Utils;
import com.talentpoc.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

;

public class RadarChartRenderer extends DataRenderer {

    protected RadarChart mChart;

    /**
     * paint for drawing the web
     */
    protected Paint mWebPaint;
    ArrayList<PointF> centerPoints = new ArrayList<>();

    public RadarChartRenderer(RadarChart chart,
                              ViewPortHandler viewPortHandler) {
        super(viewPortHandler);
        mChart = chart;
        mWebPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWebPaint.setStyle(Paint.Style.STROKE);
    }

    public Paint getWebPaint() {
        return mWebPaint;
    }

    @Override
    public void initBuffers() {
        // TODO Auto-generated method stub

    }

    @Override
    public void drawData(Canvas c) {

        RadarData radarData = mChart.getData();

        for (RadarDataSet set : radarData.getDataSets()) {

            if (set.isVisible())
                drawDataSet(c, set);
        }
    }

    protected void drawDataSet(Canvas c, RadarDataSet dataSet) {

        float sliceangle = mChart.getSliceAngle();

        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();

        PointF center = mChart.getCenterOffsets();

        List<Entry> entries = dataSet.getYVals();

        Path surface = new Path();

        boolean hasMovedToPoint = false;

        for (int j = 0; j < entries.size(); j++) {

            mRenderPaint.setColor(dataSet.getColor(j));

            Entry e = entries.get(j);

            PointF p = Utils.getPosition(centerPoints.get(j), (e.getVal() - mChart.getYChartMin()) * factor,
                    sliceangle * j + mChart.getRotationAngle());

            if (Float.isNaN(p.x))
                continue;

            if (!hasMovedToPoint) {
                surface.moveTo(p.x, p.y);
                hasMovedToPoint = true;
            } else
                surface.lineTo(p.x, p.y);
        }

        surface.close();

        // draw filled
        if (dataSet.isDrawFilledEnabled()) {
            mRenderPaint.setStyle(Paint.Style.FILL);
            mRenderPaint.setAlpha(dataSet.getFillAlpha());
            c.drawPath(surface, mRenderPaint);
            mRenderPaint.setAlpha(255);
        }

        mRenderPaint.setStrokeWidth(dataSet.getLineWidth());
        mRenderPaint.setStyle(Paint.Style.STROKE);

        // draw the line (only if filled is disabled or alpha is below 255)
        if (!dataSet.isDrawFilledEnabled() || dataSet.getFillAlpha() < 255) {
             c.drawPath(surface, mRenderPaint);
        }
    }

    @Override
    public void drawValues(Canvas c) {

        float sliceangle = mChart.getSliceAngle();

        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();

        PointF center = mChart.getCenterOffsets();

        float yoffset = Utils.convertDpToPixel(5f);

        for (int i = 0; i < mChart.getData().getDataSetCount(); i++) {

            RadarDataSet dataSet = mChart.getData().getDataSetByIndex(i);

            if (!dataSet.isDrawValuesEnabled())
                continue;

            // apply the text-styling defined by the DataSet
            applyValueTextStyle(dataSet);

            List<Entry> entries = dataSet.getYVals();

            for (int j = 0; j < entries.size(); j++) {

                Entry e = entries.get(j);

                PointF p = Utils.getPosition(center, (e.getVal() - mChart.getYChartMin()) * factor,
                        sliceangle * j + mChart.getRotationAngle());

                c.drawText(dataSet.getValueFormatter().getFormattedValue(e.getVal()),
                        p.x, p.y - yoffset, mValuePaint);
            }
        }
    }

    @Override
    public void drawExtras(Canvas c) {
        drawWeb(c);
    }

    protected void drawWeb(Canvas c) {

        float sliceangle = mChart.getSliceAngle();

        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();
        float rotationangle = mChart.getRotationAngle();

        PointF center = mChart.getCenterOffsets();

        // draw the inner-web
        mWebPaint.setStrokeWidth(mChart.getWebLineWidthInner());
        mWebPaint.setColor(mChart.getWebColorInner());
        mWebPaint.setAlpha(mChart.getWebAlpha());

        int labelCount = mChart.getYAxis().mEntryCount;

        for (int j = 0; j < labelCount; j++) {


            for (int i = 0; i < mChart.getData().getXValCount(); i++) {

                float r = (mChart.getYAxis().mEntries[j] - mChart.getYChartMin()) * factor;

                PointF p1 = Utils.getPosition(center, r, sliceangle * i + rotationangle);
                PointF p2 = Utils.getPosition(center, r, sliceangle * (i + 1) + rotationangle);

                if (j == 0) {
                    centerPoints.add(i, p1);
                }
                c.drawLine(p1.x, p1.y, p2.x, p2.y, mWebPaint);
            }
        }

        // draw the web lines that come from the center
        mWebPaint.setStrokeWidth(mChart.getWebLineWidth());
        mWebPaint.setColor(mChart.getWebColor());
        mWebPaint.setAlpha(mChart.getWebAlpha());

        for (int i = 0; i < mChart.getData().getXValCount(); i++) {

            PointF p = Utils.getPosition(centerPoints.get(i), (mChart.getYAxis().mAxisMaximum - mChart.getYAxis().mEntries[0]) * factor, sliceangle * i
                    + rotationangle);

            c.drawLine(centerPoints.get(i).x, centerPoints.get(i).y, p.x, p.y, mWebPaint);
        }


    }

}

