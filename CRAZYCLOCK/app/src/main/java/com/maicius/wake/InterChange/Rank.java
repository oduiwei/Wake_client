package com.maicius.wake.InterChange;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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

public class Rank extends Activity {
    private String returnData;
    private String username;

    private List<Map<String, Object>> rankListItems;
    private static Handler handler  = new Handler();
    private ProgressDialog dialog;
    private AlertDialog.Builder adviceDialog;
    /**
     * 初始化右上角菜单内容
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.rank_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * 设置右上角菜单item点击事件
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_sleep_advice:
                new Thread(new GetDataThread(WebService.State.GetSleepAdvice)).start();
                return true;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Log.d("Activity", "Enter Activity --> Rank");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rank);

        init();

        new Thread(new GetDataThread(WebService.State.GetFriendsSleepRank)).start();
    }

    /**
     * 初始化设置
     */
    private void init() {
        rankListItems = new ArrayList<Map<String, Object>>();
        username = getIntent().getStringExtra("username");
        dialog = new ProgressDialog(Rank.this);
        dialog.setTitle("提示");
        dialog.setMessage("正在获取排名信息，请稍后...");
        dialog.setCancelable(false);
        dialog.show();
    }

    /**
     * 初始化排名列表：对服务器返回数据进行解析
     */
    private void initRankList() {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(returnData).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("data");

        int rank = 0;
        for(JsonElement element: jsonArray) {
            rank++;
            JsonObject current = element.getAsJsonObject();
            String phone = current.keySet().iterator().next();
            JsonObject sleepObject = current.get(phone).getAsJsonObject();
            String nickname = sleepObject.get("nickname").getAsString();
            int score = sleepObject.get("score").getAsInt();

            Map<String, Object> listItem = new HashMap<String, Object>();
            if (rank < 10) {
                listItem.put("rank", "0" + rank);
            } else {
                listItem.put("rank", rank);
            }
            listItem.put("portrait", R.drawable.ic_portrait);
            listItem.put("nickname", nickname);
            listItem.put("phone", phone);
            listItem.put("score", score + "分");
            rankListItems.add(listItem);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, rankListItems,   // 用来填充的数据列表
                R.layout.item_friend_rank,                                       // 用来填充的Item的界面
                new String[]{"rank", "portrait", "nickname", "phone", "score"},  // 哪些key对应的value来生成列表项
                new int[]{R.id.rankTextView, R.id.rank_image, R.id.nickNameTextView, //决定填充界面中的哪些组件
                R.id.phoneTextView, R.id.scoreTextView});
        ListView rankListView = (ListView) findViewById(R.id.rankListView);
        rankListView.setAdapter(adapter);
        rankListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

    /**
     * 初始化睡眠建议弹框内容
     */
    private void initAdviceDialog() {
        JsonParser parser = new JsonParser();
        JsonObject object = parser.parse(returnData).getAsJsonObject();
        JsonArray array = object.getAsJsonArray("data");

        String message = "";
        for (JsonElement element : array) {
            message += element.getAsString();
        }
        adviceDialog = new AlertDialog.Builder(this);
        adviceDialog.setTitle("建议");
        adviceDialog.setMessage(message);
        adviceDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        adviceDialog.create();
        adviceDialog.show();
    }

    private class GetDataThread implements Runnable {
        private WebService.State state;

        public GetDataThread(WebService.State state) {
            this.state = state;
        }

        @Override
        public void run() {
            returnData = WebService.executeHttpGetWithOneParams(username, state);
            Log.i("ResultData", "返回数据为：" + returnData);

            handler.post(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                    //判断是否连接上服务器
                    if (returnData.equals("404")) {
                        Toast.makeText(Rank.this, "查询失败，请检查网络连接", Toast.LENGTH_SHORT).show();
                        Rank.this.finish();
                        return;
                    }
                    if (state == WebService.State.GetFriendsSleepRank) {
                        initRankList();
                    } else if (state == WebService.State.GetSleepAdvice) {
                        initAdviceDialog();
                    }

                }
            });
        }
    }
}
