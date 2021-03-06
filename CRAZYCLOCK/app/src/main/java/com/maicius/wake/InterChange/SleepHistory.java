package com.maicius.wake.InterChange;
/**
 * 记录睡眠长度
 * 监测屏幕的广播信息，记录每次屏幕关闭的时间并存储在本地数据库里，
 * 每当屏幕解锁后刷新该时间
 * 并在有网络的情况下将该时间上传到云端数据库
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.maicius.wake.alarmClock.R;
import com.maicius.wake.web.WebService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SleepHistory extends Activity {

    private String returnInfo;
    private static Handler handler = new Handler();
    private ProgressDialog dialog;
    private ListView m_list;
    private String username;
    private List<Map<String, Object>> listItems;
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Activity", "Enter Activity --> SleepHistory");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sleep_history);
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        dialog = new ProgressDialog(this);
        dialog.setTitle("提示");
        dialog.setMessage("正在获取历史信息，请稍后...");
        dialog.setCancelable(false);
        dialog.show();
        m_list = (ListView) findViewById(R.id.timeList);
        listItems = new ArrayList<Map<String, Object>>();
        new Thread(new MyThread()).start();
    }
    public class MyThread implements Runnable {
        @Override
        public void run() {
//            returnInfo = WebService.executeHttpGet(username, WebService.State.GetSleepTime);
            returnInfo = WebService.executeHttpGetWithThreeParams(username, "", "", WebService.State.GetSleepTimeHistory);
            Log.i("ResultData", "返回数据为：" + returnInfo);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();//关闭进度框

                    //判断是否连接上服务器
                    if (returnInfo.equals("404")) {
                        Toast.makeText(SleepHistory.this, "查询失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                        SleepHistory.this.finish();
                        return;
                    }

                    if (returnInfo.equals("failed")) {         //返回错误信息
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(SleepHistory.this);
                        alertDialog.setTitle("提示").setMessage("未知错误，请稍后重试");
                        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        alertDialog.create().show();
                    } else {
                        initList();
                    }
                }
            });
        }
    }
    private void initList() {
        JsonParser parser = new JsonParser();
        JsonObject jsonObject = parser.parse(returnInfo).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("data");

        int id = 0;
        for (JsonElement element : jsonArray) {
            id++;
            JsonObject tmp = element.getAsJsonObject();
            String key = tmp.keySet().iterator().next();
            String value = tmp.get(key).getAsString();

            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("ItemImage", R.drawable.ic_dialog_time);
            map.put("ItemTitle", value);
            map.put("ItemID", "时间" + id + ": ");
            listItems.add(map);
        }

        SimpleAdapter mSimpleAdapter = new SimpleAdapter(this, listItems,
                R.layout.time_item,//每一行的布局//动态数组中的数据源的键对应到定义布局的View中
                new String[]{"ItemImage", "ItemTitle", "ItemID"},
                new int[]{R.id.imageItem, R.id.textItem, R.id.idItem}
        );

        m_list.setAdapter(mSimpleAdapter);
    }

}
