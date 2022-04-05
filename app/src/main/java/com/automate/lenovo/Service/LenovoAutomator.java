package com.automate.lenovo.Service;


import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
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
    private boolean isFirstRun = true;

    enum SettingWindow{
        Home,
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
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ;
        // AccessibilityEvent.TYPES_ALL_MASK |
        // AccessibilityEvent.TYPE_VIEW_CLICKED |
        // AccessibilityEvent.TYPE_VIEW_FOCUSED |
        // AccessibilityEvent.TYPE_VIEW_SCROLLED |
        // AccessibilityEvent.TYPE_VIEW_LONG_CLICKED |
        // AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;

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
        Log.d(TAG,"the entire event" + accessibilityEvent);


        // AccessibilityEvent TYPE_WINDOW_STATE_CHANGED trigger when a new activity is loaded
        // we assume every TYPE_WINDOW_STATE_CHANGED is a new screen

        // check if the currentWindow is new or old one
        // "windowIdList not contains" will help us to confirm every screen is considered only once
        if(!windowIdList.contains(accessibilityEvent.getWindowId())){
            // This is a new Activity
            // Add current windowId to windowIdList to track the window (screen) history
            windowId = accessibilityEvent.getWindowId();
            windowIdList.add(windowId);
            // sleep here as it may takes time to load the content in low configured devices

            switch (accessibilityEvent.getClassName().toString()){
                case "com.oppo.settings.SettingsActivity":
                case "com.android.settings.homepage.SettingsHomepageActivity":
//                    sleep(6000);
                    // TODO Create utility function to scroll down until find a node with the required text
                    AccessibilityNodeInfo soundNode = findNodeWithText("Sound");
                    performGestureClick(soundNode);

//                    AccessibilityNodeInfo devicePrivacy =
//                            scrollAndFindTheNodeWithText("Sound");

//                    Log.d("DeviceandPrivacy","yeah"+devicePrivacy);

                    // 200 145
                    // 325 145
                    // 450 145
                    break;
                case "com.android.settings.Settings$ManageExternalSourcesActivity":
                    sleep(6000);
                    Log.d(TAG,"inside Settings$ManageExternalSourcesActivity");
                    settingWindow = SettingWindow.UnknownSourceInstallation;
                    switchOn("Chrome");

                    sleep(100);
                    swipeDownForNotification();
                    sleep(1200);
                    performGestureClick(120,250);
                    performGestureClick(280,250);
                    performGestureClick(430,250);
                    performGestureClick(580,250);

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

    enum ScrollDirection{
        Down,Up,Left,Right
    }
    private AccessibilityNodeInfo scrollAndFindTheNodeWithText(String text) {
        AccessibilityNodeInfo node = findNodeWithText(text);

        if(node != null){
            return  node;
        }else{
            Log.d("Scrolling", "down as we can't find the node with text " + text);
            swipe(SwipeDirection.Up);
            return scrollAndFindTheNodeWithText(text);
//            AccessibilityNodeInfo scroll = findScrollableNode(
//                    getRootInActiveWindow(),
//                    ACTION_SCROLL_DOWN
//            );
//            if (scroll != null) {
//                scroll.performAction(ACTION_SCROLL_DOWN.getId());
//                return scrollAndFindTheNodeWithText(text);
//            }else {
//                return  null;
//            }
        }
    }


    private AccessibilityNodeInfo findScrollableNode(
            AccessibilityNodeInfo root,
            AccessibilityNodeInfo.AccessibilityAction action
    ) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            if (node.getActionList().contains(action)) {
                return node;
            }
            for (int i = 0; i < node.getChildCount(); i++) {
                deque.addLast(node.getChild(i));
            }
        }
        return null;
    }


    private void switchOn(String nodeText) {
        AccessibilityNodeInfo nodeGroup = findNodeWithText(nodeText);
        AccessibilityNodeInfo switchNode = findFirstNodeByClassName("android.widget.Switch", nodeGroup);
        if(click(switchNode)){
            AccessibilityNodeInfo allowButton = findNodeWithText("Allow");
            click(allowButton);
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



    enum SwipeDirection{
        Down,
        Up,
        Left,
        Right
    }

    private void swipe(int mX,int mY, int lX,int lY){
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        path.moveTo(mX,mY);
        path.lineTo(lX,lY);

        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 50));
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
        sleep();
    }

    private void swipe(SwipeDirection swipeDirection){

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        final int screenWidth = displayMetrics.widthPixels;
        final int x2 = screenWidth/2;
        final int x1 = x2/2;
        final int x3 = x1 + x2;

        final int screenHeight = displayMetrics.heightPixels;
        final int y2 = screenHeight/2;
        final int y1 = y2/2;
        final int y3 = y1 + y2;

        switch (swipeDirection){
            case Down:
                // Swipe down
                swipe(x2,y1,x2,y3);
                break;
            case Up:
                // Swipe Up
                swipe(x2,y3,x2,y1);
                break;
            case Left:
                // Swipe Left
                swipe(x3,y2,x1,y2);
                break;
            case Right:
                // Swipe Up
                swipe(x1,y2,x3,y2);
                break;
        }
    }

    private void swipeDownForNotification() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        final int leftSideOfScreen = displayMetrics.widthPixels / 4;
        final int middleXValue = leftSideOfScreen * 2;

        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        Path path = new Path();

        // Swipe down
        path.moveTo(middleXValue,0);
        path.lineTo(middleXValue,displayMetrics.heightPixels);

        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 1000));
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
        sleep();
    }



    private void performGestureClick(AccessibilityNodeInfo node) {
        if (node == null) return;

        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        performGestureClick(rect.centerX(),rect.centerY());
    }
    private void performGestureClick(int x, int y) {
        Path clickPath = new Path();
        clickPath.moveTo(x,y);
        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(clickPath, 0, 1);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        dispatchGesture(clickBuilder.build(), null, null);
        sleep(100);
    }

    // Some Utility methods

    private void sleep(long mills){
        try{
            Thread.sleep(mills);
        }catch (InterruptedException e ){
            e.printStackTrace();
        }
    }
    private void sleep(){sleep(1000);}

    private boolean performAction(AccessibilityNodeInfo node,int AccessibilityAction){
        if (node != null ){
            node.performAction(AccessibilityAction);
            sleep();
            return true;
        }
        return false;
    }

    private boolean click(AccessibilityNodeInfo node){
        return  performAction(node,AccessibilityNodeInfo.ACTION_CLICK);
    }

    private boolean longClick(AccessibilityNodeInfo node){
        return  performAction(node,AccessibilityNodeInfo.ACTION_LONG_CLICK);
    }
    private void printNode(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            Log.d("afi", "node is null");
            return;
        }
        Log.d("afinodeText", nodeInfo.getText() + "");
        Log.d("afiIdResName", nodeInfo.getViewIdResourceName() + "");
        Log.d("afiClassName", nodeInfo.getClassName() + "");
        Log.d("afiNodeInfo",nodeInfo.toString());


        printNode(nodeInfo.getParent());
    }
}
