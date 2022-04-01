package com.automate.lenovo.Service;


import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;


public class LenovoAutomator extends AccessibilityService {

    final String TAG = "Lenovo-Auto-Log";


    final ArrayList<Integer> windowIdList = new ArrayList<>();
    int windowId = 0;

    boolean isMiniJobRunning = false;

    enum SettingWindow{
        Default,
        UnknownSourceInstallation,
        AlertDialog,
        Accessibility,
    }
    SettingWindow settingWindow = SettingWindow.Default;


    List<AccessibilityNodeInfo> nodeList = new ArrayList<>();

    @Override
    public void onServiceConnected() {
        // super.onServiceConnected();
        Log.d(TAG,"onServiceConnected: done by Afeef");

        // create and set the accessibility service info
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // Set the type of events that this service wants to listen to. Others
        // won't be passed to this service.
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK |
                AccessibilityEvent.TYPE_VIEW_CLICKED |
                AccessibilityEvent.TYPE_VIEW_FOCUSED |
                AccessibilityEvent.TYPE_VIEW_SCROLLED |
                AccessibilityEvent.TYPE_VIEW_LONG_CLICKED |
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED |
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

        // If you only want this service to work with specific applications, set their
        // package names here. Otherwise, when the service is activated, it will listen
        // to events from all applications.
        info.packageNames = new String[]
                {"com.android.settings"};

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated. This service *is*
        // application-specific, so the flag isn't necessary. If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        info.flags = AccessibilityServiceInfo.DEFAULT |
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS |
                AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS |
                AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS |
                AccessibilityServiceInfo.FLAG_ENABLE_ACCESSIBILITY_VOLUME |
                AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;

        info.notificationTimeout = 100;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            info.setInteractiveUiTimeoutMillis(1000);
        }

        this.setServiceInfo(info);

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {




        Log.d(TAG, "onAccessibilityEvent: done by afeef"+accessibilityEvent.toString());
        Log.d(TAG,"what is the classname" + accessibilityEvent.getClassName());



        getAllViewInWindow(getRootInActiveWindow());

        // AccessibilityEvent TYPE_WINDOW_STATE_CHANGED trigger when a new activity is loaded
        // we assume every TYPE_WINDOW_STATE_CHANGED is a screen
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
            // check if the currentWindow is new or old one
            if(windowIdList.contains(accessibilityEvent.getWindowId())){
                // This is a old Activity
            }else{
                // This is a new Activity
                // Add current windowId to windowIdList to track the window (screen) history
                windowId = accessibilityEvent.getWindowId();
                windowIdList.add(windowId);

                switch (accessibilityEvent.getClassName().toString()){
                    case "com.android.settings.Settings$ManageExternalSourcesActivity":
                        Log.d(TAG,"inside Settings$ManageExternalSourcesActivity");
                        settingWindow = SettingWindow.UnknownSourceInstallation;
                        break;
                    case "com.android.settings.Settings$AccessibilitySettingsActivity":
                        Log.d(TAG,"inside Settings$AccessibilitySettingsActivity");
                        settingWindow = SettingWindow.Accessibility;
                        break;
                    case "android.app.AlertDialog":
                        Log.d(TAG,"inside android.app.AlertDialog");
                        settingWindow = SettingWindow.AlertDialog;
                        break;
                    default:
                        Log.e("Unexpected value: " , accessibilityEvent.getClassName().toString());
                        settingWindow = SettingWindow.Default;
                        break;
                }
            }
        }

        Log.d(TAG,"what is the settingWindow"+settingWindow);

        // perform gestures only in TYPE_WINDOW_CONTENT_CHANGED and when isMiniJobRunning is FALSE
        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
            switch (settingWindow){
                case UnknownSourceInstallation:
                    if(!isMiniJobRunning) {
                        isMiniJobRunning = true;
                        AccessibilityNodeInfo chromeGroup = findNodeWithText("Chrome");
                        AccessibilityNodeInfo chromeSwitch = findFirstNodeByClassName("android.widget.Switch", chromeGroup);
                        if (chromeSwitch != null) {
                            chromeSwitch.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            AccessibilityNodeInfo allowButton = findNodeWithText("Allow");
                            if (allowButton != null ){
                                allowButton.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            }
                        }
                        isMiniJobRunning = false;
                    }
                    break;
                case AlertDialog:

                    break;
            }
            // find the requiredNode based
        }

    }

    @Override
    public void onInterrupt() {

    }


    private AccessibilityNodeInfo findNodeWithText(String text) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(getRootInActiveWindow());
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (getAllTextFromNode(node).equals(text)) {
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                try {
                    deque.addLast(node.getChild(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void getAllViewInWindow(AccessibilityNodeInfo root) {
        if (root == null) return;
        nodeList.clear();
        nodeList.add(root);

        //Log.d("NodeInfo", root.toString());
        //Log.d("childCount", " = " + root.getChildCount());

        if (root != null || root.getChildCount() > 0) {
            for (int i = 0; i < root.getChildCount(); i++) {
                AccessibilityNodeInfo nodeInfo = root.getChild(i);
                getAllViewInWindow(nodeInfo);
            }
        }
    }

    private String getAllTextFromNode(AccessibilityNodeInfo nodeInfo) {
        String nodeString = "";

        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(nodeInfo);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node.getText() != null && !node.getText().toString().isEmpty()) {
                nodeString += node.getText().toString(); //.replace(" ","");
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                try {
                    deque.addLast(node.getChild(i));
                } catch (Exception e) {
                    Log.e("getchild error", e.toString());
                    e.printStackTrace();
                }
            }
        }

        Log.d("getAllTextFromNode", nodeString);
        return nodeString;
    }

    private AccessibilityNodeInfo findFirstNodeByClassName(String className,AccessibilityNodeInfo group){
        if( className == null){
            return null;
        }

        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(group);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();


            if(node.getClassName().toString().equals(className)){
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                try {
                    deque.addLast(node.getChild(i));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return  null;
    }
    private AccessibilityNodeInfo findNodeByTextAndId(
            String text,
            String viewId,
            AccessibilityNodeInfo root
    ) {
        if (
                root == null || viewId.isEmpty() || text == null || text.isEmpty()
        ) return null;

        List<AccessibilityNodeInfo> infos = root.findAccessibilityNodeInfosByViewId(
                viewId
        );

        for (AccessibilityNodeInfo info : infos) {
            if (info.getText() != null && info.getText().toString().equals(text)) {
                return info;
            }
        }

        return null;
    }



}
