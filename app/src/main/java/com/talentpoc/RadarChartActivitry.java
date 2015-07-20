
package com.talentpoc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.talentpoc.charts.RadarChart;
import com.talentpoc.components.XAxis;
import com.talentpoc.components.YAxis;
import com.talentpoc.data.Entry;
import com.talentpoc.data.RadarData;
import com.talentpoc.data.RadarDataSet;
import com.talentpoc.listener.ChartLabelSelectedListener;
import com.talentpoc.utils.RoundImageView;

import java.util.ArrayList;

public class RadarChartActivitry extends Activity implements AdapterView.OnItemSelectedListener, ChartLabelSelectedListener, View.OnClickListener {

    private RadarChart mChart;
    private int mVertices = 3;
    private RoundImageView mProfileImageView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_radarchart);

        mChart = (RadarChart) findViewById(R.id.chart1);
        mProfileImageView=(RoundImageView)findViewById(R.id.img_user_profile);

        mChart.setWebLineWidth(1.5f);
        mChart.setWebLineWidthInner(0.75f);
        mChart.setWebAlpha(100);
        mChart.setChartLabelSelectedListener(this);

        mProfileImageView.setOnClickListener(this);
        
        setData();

        XAxis xAxis = mChart.getXAxis();
        xAxis.setTextSize(9f);

        YAxis yAxis = mChart.getYAxis();
        yAxis.setLabelCount(mVertices);
        yAxis.setTextSize(9f);
        yAxis.setStartAtZero(false);
        yAxis.setEnabled(false);


    }


    private String[] mParties = new String[]{
            "Party A", "Party B", "Party C", "Party D", "Party E", "Party F", "Party G", "Party H",
            "Party I"
    };

    public void setData() {

        float mult = 200;

        ArrayList<Entry> yVals1 = new ArrayList<Entry>();
        // IMPORTANT: In a PieChart, no values (Entry) should have the same
        // xIndex (even if from different DataSets), since no values can be
        // drawn above each other.
        for (int i = 0; i < mVertices; i++) {
            yVals1.add(new Entry((float) (Math.random() * mult) + mult / 2, i));
        }

        ArrayList<String> xVals = new ArrayList<String>();

        for (int i = 0; i < mVertices; i++)
            xVals.add(mParties[i % mParties.length]);

        RadarDataSet set1 = new RadarDataSet(yVals1, "Set 1");
        set1.setDrawFilled(true);
        set1.setLineWidth(2f);
        set1.setDrawValues(false);

        ArrayList<RadarDataSet> sets = new ArrayList<RadarDataSet>();
        sets.add(set1);

        RadarData data = new RadarData(xVals, sets);
        data.setValueTextSize(8f);
        data.setDrawValues(false);
        mChart.setData(data);
        mChart.invalidate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.vertices_spinner);
        Spinner spinner = (Spinner) menu.findItem(R.id.vertices_spinner).getActionView();

        ArrayList<Integer> verticesArray = new ArrayList<>();
        for (int i = 0; i < getResources().getIntArray(R.array.vertices_range).length; i++) {
            verticesArray.add(getResources().getIntArray(R.array.vertices_range)[i]);
        }
        ArrayAdapter<Integer> dataAdapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_spinner_item, verticesArray);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(this);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mVertices = getResources().getIntArray(R.array.vertices_range)[position];
        mChart.getYAxis().setLabelCount(mVertices);
        mChart.removeAllViews();
        mChart.invalidate();
        setData();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    @Override
    public void onLabelSelected(int xIndex) {
        Toast.makeText(RadarChartActivitry.this, mChart.getXValue(xIndex), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.img_user_profile:{
                startActivity(new Intent(RadarChartActivitry.this, MainActivity.class));
            }
        }
    }
}

