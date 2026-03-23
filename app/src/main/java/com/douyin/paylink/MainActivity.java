package com.douyin.paylink;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.content.Intent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    
    private static TextView logView;
    private static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ScrollView scrollView = new ScrollView(this);
        logView = new TextView(this);
        logView.setPadding(30, 30, 30, 30);
        logView.setTextSize(14);
        logView.setText("支付链接监控\n\n等待拦截...\n");
        
        scrollView.addView(logView);
        setContentView(scrollView);
        
        // 启动监控服务
        startService(new Intent(this, LogcatMonitor.class));
    }
    
    public static void addLog(String link) {
        if (logView != null) {
            String time = sdf.format(new Date());
            logView.post(() -> {
                logView.append("\n[" + time + "]\n" + link + "\n");
            });
        }
    }
}
