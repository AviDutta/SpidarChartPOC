
package com.talentpoc.components;

import android.graphics.Color;

import com.talentpoc.utils.Utils;

/**
 * Baseclass of all labels.
 * 
 * @author Philipp Jahoda
 */
public abstract class AxisBase extends ComponentBase {

    private int mAxisLineColor = Color.GRAY;

    private float mAxisLineWidth = 1f;

    /** flag indicating if the grid lines for this axis should be drawn */
    protected boolean mDrawGridLines = true;

    /** flag that indicates if the line alongside the axis is drawn or not */
    protected boolean mDrawAxisLine = true;

    /** flag that indicates of the labels of this axis should be drawn or not */
    protected boolean mDrawLabels = true;
    /** default constructor */
    public AxisBase() {
        this.mTextSize = Utils.convertDpToPixel(10f);
        this.mXOffset = Utils.convertDpToPixel(5f);
        this.mYOffset = Utils.convertDpToPixel(5f);
	}

    /**
     * Set this to true if the line alongside the axis should be drawn or not.
     * 
     * @param enabled
     */
    public void setDrawAxisLine(boolean enabled) {
        mDrawAxisLine = enabled;
    }

    /**
     * Returns true if the line alongside the axis should be drawn.
     * 
     * @return
     */
    public boolean isDrawAxisLineEnabled() {
        return mDrawAxisLine;
    }

    /**
     * Sets the width of the border surrounding the chart in dp.
     * 
     * @param width
     */
    public void setAxisLineWidth(float width) {
        mAxisLineWidth = Utils.convertDpToPixel(width);
    }

    /**
     * Returns the width of the axis line (line alongside the axis).
     * 
     * @return
     */
    public float getAxisLineWidth() {
        return mAxisLineWidth;
    }

    /**
     * Sets the color of the border surrounding the chart.
     * 
     * @param color
     */
    public void setAxisLineColor(int color) {
        mAxisLineColor = color;
    }

    /**
     * Returns the color of the axis line (line alongside the axis).
     * 
     * @return
     */
    public int getAxisLineColor() {
        return mAxisLineColor;
    }

    /**
     * Set this to true to enable drawing the labels of this axis (this will not
     * affect drawing the grid lines or axis lines).
     * 
     * @param enabled
     */
    public void setDrawLabels(boolean enabled) {
        mDrawLabels = enabled;
    }

    /**
     * Returns true if drawing the labels is enabled for this axis.
     * 
     * @return
     */
    public boolean isDrawLabelsEnabled() {
        return mDrawLabels;
    }

	/**
     * Returns the longest formatted label (in terms of characters), this axis
     * contains.
     * 
     * @return
     */
    public abstract String getLongestLabel();


}
