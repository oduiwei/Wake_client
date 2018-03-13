package com.maicius.wake.InterChange;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maicius.wake.Utils.DateUtils;
import com.maicius.wake.alarmClock.R;
import com.maicius.wake.chart.IChart;
import com.maicius.wake.chart.MBarChart;
import com.maicius.wake.chart.MLineChart;
import com.maicius.wake.chart.MPieChart;
import com.maicius.wake.web.WebService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class GetUpHistory extends Activity {

    static public ArrayList<String> times = new ArrayList<String>();
    static public LinkedHashMap<String, String> sleeptimes = new LinkedHashMap<>(); //睡眠数据缓存,将会被用来渲染图表
    static public LinkedHashMap<String, LinkedHashMap<String, String>> durationData = new LinkedHashMap<>(); //睡眠时长缓存，将被用来渲染图表

    private static Handler m_handler = new Handler();
    private SimpleAdapter m_simpleAdapter;
    private ListView m_listView;             //界面中用来显示数据的列表控件
    private Spinner m_spinner;               //选择时间段的下拉菜单控件
    private Spinner m_kindSpinner;            //选择数据类型的下拉菜单控件
    private ProgressDialog m_proDialog;
    private ArrayList<HashMap<String, Object>> m_listViewStrings = new ArrayList<HashMap<String, Object>>();//睡眠数据与界面列表对象缓存
    private ArrayList<String> m_spinnerListStrings = new ArrayList<String>();  //选择时间段下拉菜单列表内容
    private ArrayList<String> m_kindSpinnerListStrings = new ArrayList<>();    //选择数据类型下拉菜单列表内容
    private String m_responseInfo = "";      //从服务器获取的数据
    private String m_username;
    private IChart m_timeChart = new MLineChart();  //折线图
    private IChart m_barChart = new MBarChart();    //柱状图
    private IChart m_pieChart = new MPieChart();    //饼状图
    private TimeFilter m_timeFilterID;       //用户选择的时间段
    public static KindFilter m_kindFilterID;       //用户选择的数据类型

    private enum TimeFilter {
        NO_LIMIT, LAST_WEEK, LAST_MONTH, LAST_YEAR, USER_DEFINED
    }

    public enum KindFilter {
        GET_UP, SLEEP_TIME, SLEEP_DURATION
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Activity", "Enter Activity --> GetUpHistory");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_up_history);

        mInit();
    }

    /**
     * 功能：长按起床记录时弹出菜单，此方法设置菜单选项
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        menu.add(0, Menu.FIRST, 0, R.string.getupHistory_share);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    /**
     * 功能：长按起床记录时弹出菜单，此方法设置长按菜单内容后动作
     * @param item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        String strTime = m_listViewStrings.get(info.position).get("ItemTitle").toString();
        String strUserName = m_username;
        String kind = "";
        switch(m_kindFilterID) {
            case GET_UP:
                kind = "起床时间";
                break;
            case SLEEP_TIME:
                kind = "睡觉时间";
                break;
            case SLEEP_DURATION:
                kind = "睡眠时长";
                break;
            default:
                kind = "起床时间";
                break;
        }
        String strShared = String.format(getResources().getString(R.string.getupHistory_shareContent), strUserName, strUserName, kind, strTime);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, strShared);
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.getupHistory_shareTitle)));

        return super.onContextItemSelected(item);
    }

    private void mInit() {
        // 初始化弹框
        m_proDialog = new ProgressDialog(GetUpHistory.this);
        m_proDialog.setTitle("提示");
        m_proDialog.setMessage("正在获取历史信息，请稍后...");
        m_proDialog.setCancelable(false);
        m_proDialog.show();

        m_username = this.getIntent().getStringExtra("username");
        m_timeFilterID = TimeFilter.NO_LIMIT;
        m_kindFilterID = KindFilter.GET_UP;
        m_listView = (ListView) findViewById(R.id.timeList);

        // 初始化下拉菜单内容
        Resources res = getResources();
        m_spinner = (Spinner) findViewById(R.id.timeSpinner);
        m_kindSpinner = (Spinner) findViewById(R.id.kindSpinner);
        m_spinnerListStrings.add(res.getString(R.string.getupHistory_spinner_str1));
        m_spinnerListStrings.add(res.getString(R.string.getupHistory_spinner_str2));
        m_spinnerListStrings.add(res.getString(R.string.getupHistory_spinner_str3));
        m_spinnerListStrings.add(res.getString(R.string.getupHistory_spinner_str4));
        m_spinnerListStrings.add(res.getString(R.string.getupHistory_spinner_str5));
        m_kindSpinnerListStrings.add("起床时间");
        m_kindSpinnerListStrings.add("睡觉时间");
        m_kindSpinnerListStrings.add("睡眠时长");

        ArrayAdapter<String> kindAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m_kindSpinnerListStrings);
        kindAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_kindSpinner.setAdapter(kindAdapter);
        m_kindSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        m_kindFilterID = KindFilter.GET_UP;
                        break;
                    case 1:
                        m_kindFilterID = KindFilter.SLEEP_TIME;
                        break;
                    case 2:
                        m_kindFilterID = KindFilter.SLEEP_DURATION;
                        break;
                    default:
                        m_kindFilterID = KindFilter.GET_UP;
                        break;
                }
                //每次更新过滤选项，重新向服务器获取数据
                mUpdateList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, m_spinnerListStrings);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinner.setAdapter(adapter);
        m_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        m_timeFilterID = TimeFilter.NO_LIMIT;
                        break;
                    case 1:
                        m_timeFilterID = TimeFilter.LAST_WEEK;
                        break;
                    case 2:
                        m_timeFilterID = TimeFilter.LAST_MONTH;
                        break;
                    case 3:
                        m_timeFilterID = TimeFilter.LAST_YEAR;
                        break;
                    case 4:
                        m_timeFilterID = TimeFilter.USER_DEFINED;
                        break;
                    default:
                        m_timeFilterID = TimeFilter.NO_LIMIT;
                        break;
                }
                //每次更新过滤选项，重新向服务器获取数据
                mUpdateList();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //折线图
        ImageView image_curve = (ImageView) findViewById(R.id.curve);
        image_curve.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = null;
                try {
                    intent = m_timeChart.execute(GetUpHistory.this);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        //饼状图
        ImageView image_curve_1 = (ImageView) findViewById(R.id.curve_1);
        image_curve_1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = null;
                try {
                    intent = m_pieChart.execute(GetUpHistory.this);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        //条形图
        ImageView image_curve_2 = (ImageView) findViewById(R.id.curve_2);
        image_curve_2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = null;
                try {
                    intent = m_barChart.execute(GetUpHistory.this);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                startActivity(intent);
            }
        });

        registerForContextMenu(m_listView);

        //创建子线程
//        new Thread(new ThreadGetHistory()).start();
    }

    private void mInitList() {
        sleeptimes.clear();                         //清空睡眠数据缓存
        durationData.clear();
        m_listViewStrings.clear();             //清空界面列表项数据缓存
        //解析服务器返回数据
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(m_responseInfo).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("data");
        if (m_kindFilterID == KindFilter.GET_UP) {
            for (JsonElement element : jsonArray) {
                String current = element.getAsString();
                String strWords[] = current.split(" ");
                String strDates[] = strWords[0].split("-");
                String strTimes[] = strWords[1].split(":");

                if (!current.equals("")) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("ItemImage", R.drawable.ic_dialog_time);
                    map.put("ItemTitle", "\n日期：" + strDates[0] + "年" + strDates[1] + "月" + strDates[2] + "日" +
                            "\n时间：" + strTimes[0] + "时" + strTimes[1] + "分" + strTimes[2] + "秒\n");
                    map.put("ItemID", strWords[0]);
                    m_listViewStrings.add(map);

                    sleeptimes.put(strWords[0], strWords[1]);
                }
            }
        } else if (m_kindFilterID == KindFilter.SLEEP_TIME) {
            for (JsonElement element : jsonArray) {
                JsonObject current = element.getAsJsonObject();
                String date = current.keySet().iterator().next();
                String time = current.get(date).getAsString();
                String strWords[] = time.split(" ");
                String strDates[] = strWords[0].split("-");
                String strTimes[] = strWords[1].split(":");

                Log.d("Variables", "date: " + date + " || time: " + time);
                if (!current.equals("")) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("ItemImage", R.drawable.ic_dialog_time);
                    map.put("ItemTitle", "\n日期：" + strDates[0] + "年" + strDates[1] + "月" + strDates[2] + "日" +
                            "\n时间：" + strTimes[0] + "时" + strTimes[1] + "分" + strTimes[2] + "秒\n");
                    map.put("ItemID", date);
                    m_listViewStrings.add(map);

                    sleeptimes.put(date, strWords[1]);
                }
            }
        } else if (m_kindFilterID == KindFilter.SLEEP_DURATION) {
            for (JsonElement element : jsonArray) {
                JsonObject current = element.getAsJsonObject();
                String date = current.keySet().iterator().next();
                JsonObject duration = current.get(date).getAsJsonObject();
                String totalSleep = duration.get("totalsleep").getAsString();
                String deepSleep = duration.get("deepsleep").getAsString();
                String lightSleep = duration.get("lightsleep").getAsString();
                Log.d("Variables", "totalsleep: " + totalSleep +
                        " || deepsleep: " + deepSleep + " || lightsleep: " + lightSleep);
                if (!current.equals("")) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("ItemImage", R.drawable.ic_dialog_time);
                    map.put("ItemTitle", "\n睡眠时长：" + totalSleep + "h" +
                            "\n浅睡时长：" + lightSleep + "h" +
                            "\n深睡时长：" + deepSleep + "h");
                    map.put("ItemID", date);
                    m_listViewStrings.add(map);

                    LinkedHashMap<String, String> durationMap = new LinkedHashMap<>();
                    durationMap.put("totalsleep", totalSleep);
                    durationMap.put("deepsleep", deepSleep);
                    durationMap.put("lightsleep", lightSleep);
                    durationData.put(date, durationMap);
                }
            }
        }
        m_simpleAdapter = new SimpleAdapter(this, m_listViewStrings,
                R.layout.time_item,//每一行的布局//动态数组中的数据源的键对应到定义布局的View中
                new String[]{"ItemImage", "ItemTitle", "ItemID"},
                new int[]{R.id.imageItem, R.id.textItem, R.id.idItem}
        );
        m_listView.setAdapter(m_simpleAdapter);
    }

    private void mUpdateList() {
        //启动新的线程向服务器获取数据
        new Thread(new ThreadGetHistory()).start();
    }

    private Boolean mCheckTime(String strTime) {
        if (strTime.equals("")) {
            return false;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        Date getupDate;
        try {
            getupDate = formatter.parse(strTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        long diffMillis = curDate.getTime() - getupDate.getTime();
        double diffDays = diffMillis / (1000.0 * 3600 * 24);
        double longDays = 0;
        switch (m_timeFilterID) {
            case NO_LIMIT:
                longDays = Double.MAX_VALUE;
                break;
            case LAST_WEEK:
                longDays = 7;
                break;
            case LAST_MONTH:
                longDays = 30;
                break;
            case LAST_YEAR:
                longDays = 365;
                break;
            case USER_DEFINED:
                longDays = Double.MAX_VALUE;
                break;
        }
        if (diffDays > longDays) {
            return false;
        }
        return true;
    }

    public class ThreadGetHistory implements Runnable {
        @Override
        public void run() {
            //获取今天日期
            String end = DateUtils.getTodayDate();
            String start = "";
            if (m_timeFilterID == TimeFilter.NO_LIMIT) {
                start = "";
            } else if (m_timeFilterID == TimeFilter.LAST_WEEK) {
                //获取本周一日期
                start = DateUtils.getMondayOfWeek();
            } else if (m_timeFilterID == TimeFilter.LAST_MONTH) {
                //获取本月一号日期
                start = DateUtils.getFirstDayOfMonth();
            } else if (m_timeFilterID == TimeFilter.LAST_YEAR) {
                //获取今年第一天日期
                start = DateUtils.getFirstDayOfYear();
            } else {
                start = "";
                end = "";
            }

            WebService.State state = WebService.State.GetUpTimeHistory;
            if (m_kindFilterID == KindFilter.GET_UP) {
                state = WebService.State.GetUpTimeHistory;
            } else if (m_kindFilterID == KindFilter.SLEEP_TIME) {
                state = WebService.State.GetSleepTimeHistory;
            } else if (m_kindFilterID == KindFilter.SLEEP_DURATION) {
                state = WebService.State.GetSleepDurationHistory;
            }
            m_responseInfo = WebService.executeHttpGetWithThreeParams(m_username, start, end, state);
            Log.i("ResultData", "返回数据为：" + m_responseInfo);
            m_handler.post(new Runnable() {
                @Override
                public void run() {

                    m_proDialog.dismiss();

                    //判断是否连接上服务器
                    if (m_responseInfo.equals("404")) {
                        Toast.makeText(GetUpHistory.this, "查询失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                        GetUpHistory.this.finish();
                        return;
                    }

                    if (m_responseInfo.equals("failed")) {

                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(GetUpHistory.this);
                        alertDialog.setTitle("提示").setMessage("获取历史信息失败！");
                        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.create().show();

                    } else {
                        mInitList();
                    }
                }
            });
        }
    }
}

