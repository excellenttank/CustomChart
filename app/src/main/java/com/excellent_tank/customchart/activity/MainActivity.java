package com.excellent_tank.customchart.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.excellent_tank.customchart.R;
import com.excellent_tank.customchart.chart.HorizontalChart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private Random random = new Random();
    private ArrayList<HashMap<String, Object>> pileNoDateList = new ArrayList<>();
    private ArrayList<Float> monthCountList = new ArrayList<Float>();
    private LinearLayout histogram_text_whole_ll;
    private LinearLayout histogram_text_ll;
    private com.excellent_tank.customchart.chart.HistogramChart histogram_chart_view;
    private HorizontalChart horizontalChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
        initView();
    }

    private void bindViews() {
        histogram_text_whole_ll = (LinearLayout) findViewById(R.id.histogram_text_whole_ll);
        histogram_text_ll = (LinearLayout) findViewById(R.id.histogram_text_ll);
        histogram_chart_view = (com.excellent_tank.customchart.chart.HistogramChart) findViewById(R.id.histogram_chart_view);
        horizontalChart = (HorizontalChart) findViewById(R.id.horizontal_chart);
    }

    private void initView() {
        initData();
        histogram_chart_view.setRefresh(true);
        histogram_chart_view.setData(pileNoDateList, histogram_text_ll, histogram_text_whole_ll);
        histogram_chart_view.invalidate();
        histogram_chart_view.requestLayout();

        horizontalChart.setRefresh(true);
        horizontalChart.SetDate(monthCountList);
        horizontalChart.invalidate();
        horizontalChart.requestLayout();
    }

    private void initData() {
        pileNoDateList.clear();
        for (int i = 0; i < 50; i++) {
            HashMap<String, Object> data = new HashMap<>();
            data.put("startPileNo", i);
            data.put("upCount", random.nextInt(200));
            data.put("downCount", random.nextInt(200));
            pileNoDateList.add(data);
        }

        monthCountList.clear();
        monthCountList.add(10f);
        monthCountList.add(30f);
        monthCountList.add(20f);
        monthCountList.add(60f);
        monthCountList.add(45f);

    }


}
