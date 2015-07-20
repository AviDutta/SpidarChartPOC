
package com.talentpoc.renderer;


import com.talentpoc.utils.ViewPortHandler;

/**
 * Abstract baseclass of all Renderers.
 * 
 * @author Philipp Jahoda
 */
public abstract class Renderer {

    /**
     * the component that handles the drawing area of the chart and it's offsets
     */
    protected ViewPortHandler mViewPortHandler;

    /** the minimum value on the x-axis that should be plotted */
    protected int mMinX = 0;

    /** the maximum value on the x-axis that should be plotted */
    protected int mMaxX = 0;

    public Renderer(ViewPortHandler viewPortHandler) {
        this.mViewPortHandler = viewPortHandler;
    }

    /**
     * Returns true if the specified value fits in between the provided min
     * and max bounds, false if not.
     * 
     * @param val
     * @param min
     * @param max
     * @return
     */
    protected boolean fitsBounds(float val, float min, float max) {

        if (val < min || val > max)
            return false;
        else
            return true;
    }


}
