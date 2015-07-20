
package com.talentpoc.data;

import java.util.List;

/**
 * Data container for the RadarChart.
 * 
 * @author Philipp Jahoda
 */
public class RadarData extends ChartData<RadarDataSet> {


    public RadarData(List<String> xVals, List<RadarDataSet> dataSets) {
        super(xVals,dataSets);
    }

}
