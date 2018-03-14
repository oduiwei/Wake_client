package com.maicius.wake.web;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import com.maicius.wake.alarmClock.MainActivity;
import com.maicius.wake.web.ConnectionDetector;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

public class WebService extends Activity{
    public enum State {
        LogIn,
        Register,
        GetUpTime,
        GetTimeList,
        GetUserInfo,
        GetFriendsList,
        DeleteFriend,
        AddFriend,
        SleepTime,
        GetSleepTime,
        SearchFriend,
        SetGetUpTip,
        GetGetUpTip,
        GetUpTimeHistory,
        GetSleepTimeHistory,
        GetSleepDurationHistory,
        SetIntimacyRelation,
        CheckIntimacyRelation
    }
    // IP地址
    //private static String IP = "192.168.137.1:8080";
    private static String IP = "172.26.6.1:8080";
    private static String base = "http://" + IP;
    /**
     * DoGet
     */
    private static String doHttpGet(String path){
        Log.i("URL", "请求地址是：" + path);
        HttpURLConnection conn = null;
        InputStream is = null;
        try {
            conn = (HttpURLConnection) new URL(path).openConnection();
            conn.setConnectTimeout(3000); // 设置超时时间
            conn.setReadTimeout(3000);
            conn.setDoInput(true);
            conn.setRequestMethod("GET"); // 设置获取信息方式
            conn.setRequestProperty("Charset", "UTF-8"); // 设置接收数据编码格式
            if (conn.getResponseCode() == 200) {
                is = conn.getInputStream();
                return parseInfo(is);
            }
            return null;

        } catch (SocketTimeoutException ex) {
            Log.e("Error", "服务器连接超时...");
        } catch (Exception e) {
            e.printStackTrace();
            //return "服务器连接超时...";
        } finally {

            // 意外退出时进行连接关闭保护
            if (conn != null) {
                conn.disconnect();
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return "404";
    }

    /**
     * 执行包含两个参数的HTTP GET请求处理
     * @param param1 --> 参数1
     * @param param2 --> 参数2
     * @param state  --> 具体请求类型
     * @return       --> 返回结果字符串
     */
    public static String executeHttpGetWithTwoParams(String param1, String param2, State state) {
        String path = "";
        switch (state) {
            // 添加好友
            // param1 --> username || param2 --> friendName
            case AddFriend:
                path = base + "/AddFriend";
                path += "?username=" + param1 + "&friendName=" + param2;
                break;
            // 删除用户的某个好友
            // param1 --> username || param2 --> friendName
            case DeleteFriend:
                path = base + "/DeleteFriend";
                path += "?username=" + param1 + "&friendName=" + param2;
                break;
            // 设置亲友关系
            // param1 --> username || param2 --> friendName
            case SetIntimacyRelation:
                path = base + "/SetRelation";
                path += "?username=" + param1 + "&friendName=" + param2;
                break;
            // 检查是否为亲友关系
            // param1 --> username || param2 --> friendName
            case CheckIntimacyRelation:
                path = base + "/CheckRelation";
                path += "?username=" + param1 + "&friendName=" + param2;
                break;
        }
        return doHttpGet(path);
    }

    /**
     * 执行包含三个参数的HTTP GET请求处理
     * @param param1 --> 参数1
     * @param param2 --> 参数2
     * @param param3 --> 参数3
     * @param state  --> 具体请求什么数据
     * @return       --> 返回结果字符串
     */
    public static String executeHttpGetWithThreeParams(String param1, String param2, String param3, State state) {
        String path = "";
        switch (state) {
            // 获取用户某段时间起床时间记录
            // param1 --> username || param2 --> start || param3 --> end
            case GetUpTimeHistory:
                path = base + "/GetUpTimeHistory?username=" + param1 + "&start=" + param2 + "&end=" + param3;
                break;
            // 获取用户某段时间睡觉时间记录
            // param1 --> username || param2 --> start || param3 --> end
            case GetSleepTimeHistory:
                path = base + "/GetSleepTimeHistory?username=" + param1 + "&start=" + param2 + "&end=" + param3;
                break;
            // 获取用户某段时间睡眠时长记录
            // param1 --> username || param2 --> start || param3 --> end
            case GetSleepDurationHistory:
                path = base + "/GetSleepDurationHistory?username=" + param1 + "&start=" + param2 + "&end=" + param3;
                break;
        }

        return doHttpGet(path);
    }

    /**
     * 登录
     */
    public static String executeHttpGet(String username, String password, State state) {
        String path;
        path = base + "/LogLet";
        path = path + "?username=" + username + "&password=" + password;
        return doHttpGet(path);
    }

    /**
     * 注册
     */
    public static String executeHttpGet(String username, String password,
                                        String nickname, State state) {
        String path;
        path = base + "/RegLet";
        try {
            path = path + "?username=" + username + "&password=" +
                    password + "&nickname=" + URLEncoder.encode(nickname, "UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return doHttpGet(path);
    }
    /**
     * 上传起床时间
     */
    public static String executeHttpGet(long hour, String date, State state){
        String path="";
        switch (state) {
            case SleepTime:
                path = base + "/SleepTime";
                break;
        }

        path = path + "?username=" + MainActivity.s_userName + "&hour=" + String.valueOf(hour)
                +"&date="+date;
        return doHttpGet(path);

    }
    public static String executeHttpGet(String date) {

        String path;
        path = base + "/GetUpTime";

        path = path + "?username=" + MainActivity.s_userName + "&date=" + date;
        return doHttpGet(path);

    }

    public static String executeHttpGet(String username, State state) {

        // URL 地址
        String path = "";
        switch (state) {
            case GetTimeList:
                path = base+ "/TimeHistory";
                break;
            case GetUserInfo:
                path = base + "/GetUserInfo";
                break;
            case GetFriendsList:
                path = base + "/GetFriendsList";
                break;
            case GetSleepTime:
                path = base + "/GetSleepTime";
                break;
            case GetGetUpTip:
                path = base +"/GetGetUpTip";
        }
        path = path + "?username=" + username;
//        Log.v("sss", path);
//        Log.i("URL", "请求地址是：" + path);
        return doHttpGet(path);
    }

    public static String friendOperation(String userName, String friendName, State state) {
        String path = "";
        switch (state) {
            case DeleteFriend:
                path = base + "/DeleteFriend";
                break;
            case AddFriend:
                path = base + "/AddFriend";
                break;
            case SearchFriend://通过用户输入的昵称和电话查找好友信息
                path = base + "/SearchFriend";
                break;
        }
        //为了重用，如果是查找好友，第一个参数是指输入的昵称，第二个参数是指输入的用户名即电话
        try {
            path += "?username=" + URLEncoder.encode(userName,"UTF-8") + "&friendName=" + URLEncoder.encode(friendName, "UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return doHttpGet(path);
    }

    /**
     * 设置用户信息
     * @param username
     * @param nickname
     * @param brief_intro
     * @return
     */
    public static String executeHttpGet(String username, String nickname, String brief_intro) {
        String path;
        path = base + "/SetUserInfo";
        try {
            path = path + "?username=" + username + "&nickname="
                    + URLEncoder.encode(nickname, "UTF-8") + "&brief_intro=" + URLEncoder.encode(brief_intro, "UTF-8");
//            Log.v("sss", path);
//            Log.i("URL", "请求地址是：" + path);
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return doHttpGet(path);
    }

    public static String setGetUpTip(String username,String friendname, String tip, State state){
        String path = "";
        switch (state) {
            case SetGetUpTip:
                path = base + "/SetGetUpTip";
                break;
        }
        try {
            path += "?username=" + username + "&friendname=" + friendname + "&tip=" + URLEncoder.encode(tip, "UTF-8");
        }catch(UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return doHttpGet(path);
    }

    // 将输入流转化为 String 型
    private static String parseInfo(InputStream inStream) throws Exception {
        byte[] data = read(inStream);
        // 转化为字符串
        return new String(data, "UTF-8");
    }

    // 将输入流转化为byte型
    public static byte[] read(InputStream inStream) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
        }
        inStream.close();
        return outputStream.toByteArray();
    }
}
