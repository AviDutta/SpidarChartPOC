
package com.talentpoc.renderer;

import android.graphics.Canvas;
import android.graphics.PointF;

import com.talentpoc.charts.RadarChart;
import com.talentpoc.components.XAxis;
import com.talentpoc.utils.Utils;
import com.talentpoc.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

public class XAxisRendererRadarChart extends XAxisRenderer {

    private RadarChart mChart;

    public XAxisRendererRadarChart(ViewPortHandler viewPortHandler, XAxis xAxis, RadarChart chart) {
        super(viewPortHandler, xAxis, null);
        this.mXAxis = xAxis;
        mChart = chart;
    }

    @Override
    public void renderAxisLabels(Canvas c) {

        if (!mXAxis.isEnabled() || !mXAxis.isDrawLabelsEnabled())
            return;

        mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mXAxis.getTextSize());
        mAxisLabelPaint.setColor(mXAxis.getTextColor());

        float sliceangle = mChart.getSliceAngle();

        // calculate the factor that is needed for transforming the value to
        // pixels
        float factor = mChart.getFactor();

        PointF center = mChart.getCenterOffsets();
        List<PointF> labelPositions = new ArrayList<>();

        for (int i = 0; i < mXAxis.getValues().size(); i++) {

            String text = mXAxis.getValues().get(i);

            float angle = (sliceangle * i + mChart.getRotationAngle()) % 360f;

            PointF p = Utils.getPosition(center, mChart.getYRange() * factor
                    + mXAxis.mLabelWidth / 2f, angle);

            c.drawText(text, p.x, p.y + mXAxis.mLabelHeight / 2f, mAxisLabelPaint);
            labelPositions.add(i, p);
        }
        mXAxis.setLabelPosition(labelPositions);
    }


    public void computeAxis(float xValAverageLength, List<String> xValues) {

        mAxisLabelPaint.setTypeface(mXAxis.getTypeface());
        mAxisLabelPaint.setTextSize(mXAxis.getTextSize());

        StringBuffer a = new StringBuffer();

        int max = (int) Math.round(xValAverageLength
                + mXAxis.getSpaceBetweenLabels());

        for (int i = 0; i < max; i++) {
            a.append("h");
        }

       /* TextView textView= new TextView(mChart.getContext());
        textView.setTypeface(mXAxis.getTypeface());
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX,mXAxis.getTextSize());
        textView.setText(a.toString());
        mXAxis.mLabelWidth= textView.getMeasuredWidth();
        mXAxis.mLabelHeight= textView.getMeasuredHeight();*/
        mXAxis.mLabelWidth = Utils.calcTextWidth(mAxisLabelPaint, a.toString());
        mXAxis.mLabelHeight = Utils.calcTextHeight(mAxisLabelPaint, "Q");
        mXAxis.setValues(xValues);
    }


}
