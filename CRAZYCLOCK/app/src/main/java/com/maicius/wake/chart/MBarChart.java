package com.maicius.wake.chart;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.achartengine.ChartFactory;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import com.maicius.wake.InterChange.GetUpHistory;

/**
 * Sales demo bar chart.
 */
public class MBarChart extends AbstractChart {

    /**
     * Returns the chart name.
     *
     * @return the chart name
     */
    public String getName() {
        return "Sales horizontal bar chart";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "The monthly sales for the last 2 years (horizontal bar chart)";
    }

    /**
     * Executes the chart demo.
     *
     * @param context the context
     * @return the built intent
     */
    public Intent execute(Context context) {
        String activityTitle = "";
        switch (GetUpHistory.m_kindFilterID) {
            case GET_UP:
                activityTitle = "起床时间统计图";
                break;
            case SLEEP_TIME:
                activityTitle = "睡觉时间统计图";
                break;
            case SLEEP_DURATION:
                activityTitle = "睡眠时长统计图";;
                break;
    }
        Intent intent = ChartFactory.getBarChartIntent(context, mGetDateDataset(), mGetRenderer(), Type.DEFAULT, activityTitle);
        return intent;
    }

    private XYMultipleSeriesRenderer mGetRenderer() {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        renderer.setAxisTitleTextSize(24);
        renderer.setChartTitleTextSize(32);
        renderer.setLabelsTextSize(24);
        renderer.setLegendTextSize(24);
        renderer.setPointSize(12f);

        //in this order: top, left, bottom, right
        renderer.setMargins(new int[]{40, 40, 40, 20});

        XYSeriesRenderer green = new XYSeriesRenderer();

        green.setPointStyle(PointStyle.CIRCLE);
        green.setColor(Color.GREEN);
        green.setFillPoints(true);

        XYSeriesRenderer blue, yellow;
        blue = new XYSeriesRenderer();
        blue.setPointStyle(PointStyle.CIRCLE);
        blue.setColor(Color.BLUE);
        blue.setFillPoints(true);

        yellow = new XYSeriesRenderer();
        yellow.setPointStyle(PointStyle.CIRCLE);
        yellow.setColor(Color.YELLOW);
        yellow.setFillPoints(true);

        String chartTitle = "";
        String yTitle = "时间";
        switch (GetUpHistory.m_kindFilterID) {
            case GET_UP:
                chartTitle = "起床时间折线图";
                break;
            case SLEEP_TIME:
                chartTitle = "睡觉时间折线图";
                break;
            case SLEEP_DURATION:
                chartTitle = "睡眠时长折线图";
                yTitle = "小时";
                break;
            default:
                chartTitle = "起床时间折线图";
                break;
        }
        renderer.addSeriesRenderer(green);
        if (GetUpHistory.m_kindFilterID == GetUpHistory.KindFilter.SLEEP_DURATION) {
            renderer.addSeriesRenderer(blue);
            renderer.addSeriesRenderer(yellow);
        }
        renderer.setAxesColor(Color.DKGRAY);
        renderer.setLabelsColor(Color.LTGRAY);
        renderer.setChartTitle(chartTitle);
        renderer.setXTitle("日期");
        renderer.setYTitle(yTitle);
        renderer.setZoomButtonsVisible(true);
        renderer.setYAxisMin(0);
        renderer.setYAxisMax(12.0);
        renderer.setXLabels(5);
        renderer.setYLabels(8);
        renderer.setShowGrid(true);
        renderer.setBarSpacing(1f);

        int length = renderer.getSeriesRendererCount();
        for (int i = 0; i < length; i++) {
            SimpleSeriesRenderer seriesRenderer = renderer.getSeriesRendererAt(i);
            seriesRenderer.setDisplayChartValues(false);
        }
        return renderer;
    }

    private XYMultipleSeriesDataset mGetDateDataset() {
        XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
        if (GetUpHistory.m_kindFilterID == GetUpHistory.KindFilter.SLEEP_DURATION) {
            TimeSeries totalSeries = new TimeSeries("睡眠时长");
            TimeSeries deepSeries = new TimeSeries("深睡时长");
            TimeSeries lightSeries = new TimeSeries("浅睡时长");

            LinkedHashMap<String, LinkedHashMap<String, String>> durationData = GetUpHistory.durationData;
            try {
                Iterator iter = durationData.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String date = (String) entry.getKey();
                    LinkedHashMap<String, String> durationMap = (LinkedHashMap<String, String>) entry.getValue();
                    float totalSleep = Float.parseFloat(durationMap.get("totalsleep"));
                    float deepSleep = Float.parseFloat(durationMap.get("deepsleep"));
                    float lightSleep = Float.parseFloat(durationMap.get("lightsleep"));

                    totalSeries.add(fmt.parse(date), totalSleep);
                    deepSeries.add(fmt.parse(date), deepSleep);
                    lightSeries.add(fmt.parse(date), lightSleep);
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Log.e("Error", "字符串解析失败！--> MLineChart.mGetDateDataset");
            } catch (ParseException e) {
                e.printStackTrace();
                Log.e("Error", "日期解析失败！--> MLineChart.mGetDateDataset");
            }

            dataset.addSeries(totalSeries);
            dataset.addSeries(deepSeries);
            dataset.addSeries(lightSeries);
        } else {
            String seriesTitle = "";
            switch (GetUpHistory.m_kindFilterID) {
                case GET_UP:
                    seriesTitle = "起床时间";
                    break;
                case SLEEP_TIME:
                    seriesTitle = "睡觉时间";
                    break;
            }
            TimeSeries series = new TimeSeries(seriesTitle);

            LinkedHashMap<String, String> mTimes = GetUpHistory.sleeptimes;
            try {
                Iterator iter = mTimes.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String date = (String) entry.getKey();
                    String time = (String) entry.getValue();

                    StringTokenizer tokenizer = new StringTokenizer(time, ":");
                    int hour = Integer.parseInt(tokenizer.nextToken());
                    int minute = Integer.parseInt(tokenizer.nextToken());
                    int second = (int) Float.parseFloat(tokenizer.nextToken());
                    double tm = hour + minute / 60.0 + second / 3600.0;

                    //描点
                    series.add(fmt.parse(date), tm);
                }
            } catch (ParseException e) {
                Log.e("Error", "日期解析失败！--> MLineChart.mGetDateDataset");
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Log.e("Error", "字符串解析失败！--> MLineChart.mGetDateDataset");
            }
            dataset.addSeries(series);
        }
        return dataset;
    }
}
