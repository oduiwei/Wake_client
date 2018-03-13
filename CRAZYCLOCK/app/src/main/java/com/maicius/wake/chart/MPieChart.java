
package com.maicius.wake.chart;

import org.achartengine.ChartFactory;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

import com.maicius.wake.InterChange.GetUpHistory;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;

/**
 * Budget demo pie chart.
 */
public class MPieChart extends AbstractChart {
    /**
     * Returns the chart name.
     *
     * @return the chart name
     */
    public String getName() {
        return "Budget chart";
    }

    /**
     * Returns the chart description.
     *
     * @return the chart description
     */
    public String getDesc() {
        return "The budget per project for this year (pie chart)";
    }

    /**
     * Executes the chart demo.
     *
     * @param context the context
     * @return the built intent
     */
    public Intent execute(Context context) {
        String activityTitle = "";
        String chartTitle = "";
        switch (GetUpHistory.m_kindFilterID) {
            case GET_UP:
                activityTitle = "起床时间统计图";
                chartTitle = "起床时间分布";
                break;
            case SLEEP_TIME:
                activityTitle = "睡觉时间统计图";
                chartTitle = "睡觉时间分布";
                break;
            case SLEEP_DURATION:
                activityTitle = "睡眠时长统计图";;
                chartTitle = "睡眠时长比率";
                break;
        }

        DefaultRenderer mRenderer = new DefaultRenderer();
        mRenderer.setChartTitle(chartTitle);
        mRenderer.setZoomButtonsVisible(true);// 显示放大缩小功能按钮
        mRenderer.setStartAngle(180);// 设置为水平开始
        mRenderer.setDisplayValues(true);// 显示数据
        mRenderer.setFitLegend(true);// 设置是否显示图例
        mRenderer.setLegendTextSize(32);// 设置图例字体大小
        mRenderer.setLegendHeight(10);// 设置图例高度

        CategorySeries series = new CategorySeries("");
        int[] colors = new int[]{Color.BLUE, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.CYAN};
        double[] values;
        String[] strs;
        if (GetUpHistory.m_kindFilterID == GetUpHistory.KindFilter.SLEEP_DURATION) {
            strs = new String[]{"深度睡眠", "浅度睡眠"};
            float total = 0, deep = 0, light = 0;
            LinkedHashMap<String, LinkedHashMap<String, String>> durationData = GetUpHistory.durationData;
            try {
                Iterator iter = durationData.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String date = (String) entry.getKey();
                    LinkedHashMap<String, String> durationMap = (LinkedHashMap<String, String>) entry.getValue();
                    total += Float.parseFloat(durationMap.get("totalsleep"));
                    deep += Float.parseFloat(durationMap.get("deepsleep"));
                    light += Float.parseFloat(durationMap.get("lightsleep"));
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Log.e("Error", "字符串解析失败！--> MLineChart.mGetDateDataset");
            }

            values = new double[] { (double)deep / total, (double)light / total };
        } else  {
            LinkedHashMap<String, String> data = GetUpHistory.sleeptimes;
            int size = data.size();
            int[] hourArray = new int[size];
            int index = 0;
            try {
                Iterator iter = data.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String time = (String) entry.getValue();

                    StringTokenizer tokenizer = new StringTokenizer(time, ":");
                    int hour = Integer.parseInt(tokenizer.nextToken());
                    hourArray[index] = hour;
                    index++;
                }
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                Log.e("Error", "字符串解析失败！--> MPieChart.execute");
            }
            int period01 = 0, period02 = 0, period03 = 0, period04 = 0, period05 = 0;
            if (GetUpHistory.m_kindFilterID == GetUpHistory.KindFilter.GET_UP) {
                strs = new String[]{"6点之前", "6点-8点", "8点-10点", "10点-12点", "12点之后"};
                for (int i = 0; i < size; i++) {
                    if (hourArray[i] < 6) {
                        period01++;
                    } else if (hourArray[i] < 8 && hourArray[i] >= 6) {
                        period02++;
                    } else if (hourArray[i] >= 8 && hourArray[i] < 10) {
                        period03++;
                    } else if (hourArray[i] >= 10 && hourArray[i] < 12) {
                        period04++;
                    } else if (hourArray[i] >= 12) {
                        period05++;
                    }
                }
            } else {
                strs = new String[]{"9点之前", "9点-11点", "11点-12点", "12点-1点", "1点之后"};
                for (int i = 0; i < size; i++) {
                    if (hourArray[i] < 21 && hourArray[i] > 18) {
                        period01++;
                    } else if (hourArray[i] < 23 && hourArray[i] >= 21) {
                        period02++;
                    } else if (hourArray[i] >= 23 && hourArray[i] < 24) {
                        period03++;
                    } else if (hourArray[i] >= 0 && hourArray[i] < 1) {
                        period04++;
                    } else if (hourArray[i] >= 1 && hourArray[i] < 6) {
                        period05++;
                    }
                }
            }

            values = new double[]{ (double)period01 / size, (double)period02 / size,
                    (double)period03 / size, (double)period04 / size, (double)period05 / size };
        }
        double totalValues = 0;
        for (int i = 0; i < values.length; i++)
            totalValues += values[i];
        for (int i = 0; i < values.length; i++) {
            series.add(strs[i], values[i] / totalValues);// 设置种类名称和对应的数值，前面是（key,value）键值对
            SimpleSeriesRenderer renderer = new SimpleSeriesRenderer();
            if (i < colors.length) {
                renderer.setColor(colors[i]);// 设置描绘器的颜色
            } else {
                renderer.setColor(getRandomColor());// 设置描绘器的颜色
            }
            renderer.setChartValuesFormat(NumberFormat.getPercentInstance());// 设置百分比
            mRenderer.setChartTitleTextSize(48);// 设置饼图标题大小
            mRenderer.addSeriesRenderer(renderer);// 将最新的描绘器添加到DefaultRenderer中
        }
        Intent intent = ChartFactory.getPieChartIntent(context,
                series, mRenderer, activityTitle);
        return intent;
    }

    private int getRandomColor() {// 分别产生RBG数值
        Random random = new Random();
        int R = random.nextInt(255);
        int G = random.nextInt(255);
        int B = random.nextInt(255);
        return Color.rgb(R, G, B);
    }
}
