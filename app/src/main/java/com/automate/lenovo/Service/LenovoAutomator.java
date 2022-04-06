package com.automate.lenovo.Service;


import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK;
import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK;
import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN;
import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD;

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
                    // wait for a while so that the auto app can make sure that settings app is fully loaded
//                    sleep(6000);


                    // Point 10
//                    enableDeveloperOptionsInEmulator();
                    // enableDeveloperOptionsInTablet();
                    //
                    // Still Need to add script to enable usb debugging


                    // Point 2
                    // Apps and notifications
                    appsAndNotifications();
                    // Disable Apps Completed


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

    private void appsAndNotifications() {
        AccessibilityNodeInfo appAndNotifications = scrollAndFindTheNodeWithText("Apps & notifications");
        AccessibilityNodeInfo clickable = getParentNodeWithAction(appAndNotifications,ACTION_CLICK);
        if(click(clickable)){
            sleep(5000);

            // Point 2. Disable apps:
            // TODO change the node text into "SEE ALL 32 APPS"
            AccessibilityNodeInfo seeAllAppBtn = findNodeWithText("See all 29 apps");
            // Disable or Uninstall all apps
            if(true){}else if(performGestureClick(seeAllAppBtn)){
                sleep();
                disableApp("Android Auto");
                // Point 4. Allow Unknown Apps
                AccessibilityNodeInfo chromeNode = findNodeWithText("Chrome");
                if(performGestureClick(chromeNode)){
                    sleep();
                    AccessibilityNodeInfo installUnknownAppsNode = scrollAndFindTheNodeWithText("Install unknown apps");
                    if(performGestureClick(installUnknownAppsNode)){
                        sleep();
                        AccessibilityNodeInfo allowTextNode = scrollAndFindTheNodeWithText("Allow from this source");
                        AccessibilityNodeInfo allowSwitch = findNodeWithAction(allowTextNode.getParent(),ACTION_CLICK);
                        sleep();
                        click(allowSwitch);
                        sleep();
                        performGlobalAction(GLOBAL_ACTION_BACK);
                        sleep();
                    }
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    sleep();
                }
                // Point 4 completed
                disableApp("Digital Wellbeing");
                disableApp("Google Play Movies & TV");
                disableApp("Keep notes");
                unInstallApp("MusicFX");
                unInstallApp("Sound Recorder");
                disableApp("YouTube");
                disableApp("YouTube Music");

                sleep();
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep();











//                disableApp("Calendar");
//                disableApp("chrome");
//                disableApp("clock");
//                disableApp("drive");
//                disableApp("duo");
            }
            // Disable or Uninstall of apps completed
            // Play with notifications


            // Now
            // Point 3. Disable Notifications for all apps
            AccessibilityNodeInfo notificationsNode = findNodeWithText("Notifications");
            if(performGestureClick(notificationsNode)){
                sleep();

                // Don't show notifications on lockscreen
                AccessibilityNodeInfo notificationsOnLockScreen = findNodeWithText("Notifications on lockscreen");
                if(performGestureClick(notificationsOnLockScreen)){
                    sleep();
                    AccessibilityNodeInfo dontShow = findNodeWithText("Don’t show notifications");
                    performGestureClick(dontShow);
                    sleep();
                }

                // Go to advanced
                AccessibilityNodeInfo advancedNode = scrollAndFindTheNodeWithText("Advanced");
                if(performGestureClick(advancedNode)){
                    sleep();

                    clickOnTheParentsClickable("Suggested actions and replies");
                    clickOnTheParentsClickable("Allow notification dots");
                    clickOnTheParentsClickable("Blink light");
                    clickOnTheParentsClickable("Display status bar icons");
                    sleep();
                }
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep();
            }
            performGlobalAction(GLOBAL_ACTION_BACK);
            sleep();

            // TODO reached here
            // Now play with default apps
        }
    }

    private void clickOnTheParentsClickable(String nodeText) {
        AccessibilityNodeInfo node = findNodeWithText(nodeText);
        AccessibilityNodeInfo clickableSibling = findNodeWithAction(node.getParent(),ACTION_CLICK);
        performGestureClick(clickableSibling);
        sleep();
    }

    private void disableApp(String appName) {
        AccessibilityNodeInfo appNameNode = scrollAndFindTheNodeWithText(appName);
        AccessibilityNodeInfo clickable = getParentNodeWithAction(appNameNode,ACTION_CLICK);
        if(click(clickable)){
            sleep();
            AccessibilityNodeInfo disableBtn = findNodeWithText("Disable");
            if(performGestureClick(disableBtn)){
                sleep();
                AccessibilityNodeInfo disableAppBtn = findNodeWithText("Disable app");
                performGestureClick(disableAppBtn);
                sleep();
            }
            performGlobalAction(GLOBAL_ACTION_BACK);
            sleep();
        }
        sleep();
    }

    private void unInstallApp(String appName) {
        AccessibilityNodeInfo appNameNode = scrollAndFindTheNodeWithText(appName);
        AccessibilityNodeInfo clickable = getParentNodeWithAction(appNameNode,ACTION_CLICK);
        if(click(clickable)){
            sleep();
            AccessibilityNodeInfo disableBtn = findNodeWithText("UNINSTALL");
            if(performGestureClick(disableBtn)){
                sleep();
                AccessibilityNodeInfo disableAppBtn = findNodeWithText("OK ");
                performGestureClick(disableAppBtn);
                sleep();
            }
            sleep();
        }
        sleep();
    }

    private void enableDeveloperOptionsInEmulator() {
        AccessibilityNodeInfo aboutTablet = scrollAndFindTheNodeWithText("About emulated device");
        AccessibilityNodeInfo clickableTablet =  getParentNodeWithAction(aboutTablet,ACTION_CLICK);
        if(click(clickableTablet)){
            sleep();
            AccessibilityNodeInfo buildNumber = scrollAndFindTheNodeWithText("Build number");
            for (int i = 0; i<7 ; i++){
                performGestureClick(buildNumber);
            }
        }
    }

    private void enableDeveloperOptionsInTablet() {

        // Go to System > About Tablet > Build Number
        // Go to System > Developer Options > Enable USB Debugging
        AccessibilityNodeInfo systemNode = scrollAndFindTheNodeWithText("System");
        AccessibilityNodeInfo clickableNode = getParentNodeWithAction(systemNode,ACTION_CLICK);
        if(click(clickableNode)){
            // System subSetting menu opened
            // wait a while
            sleep();
            AccessibilityNodeInfo aboutTablet = scrollAndFindTheNodeWithText("About Tablet");
            AccessibilityNodeInfo clickableTablet =  getParentNodeWithAction(aboutTablet,ACTION_CLICK);
            if(click(clickableTablet)){
                sleep();
                AccessibilityNodeInfo buildNumber = scrollAndFindTheNodeWithText("Build number");
                for (int i = 0; i<8 ; i++){
                    performGestureClick(buildNumber);
                }
                sleep();

            }else{
                // Todo Error on clicking the about tablet
            }
        }else{
            // TODO error while clicking the SYSTEM node
        }
    }

    enum ScrollDirection{
        Down,Up,Left,Right
    }
    private AccessibilityNodeInfo getParentNodeWithAction(AccessibilityNodeInfo node, AccessibilityNodeInfo.AccessibilityAction action) {
        if(node == null) return null;

        if(node.getActionList().contains(action)){
            return  node;
        }else{
            return  getParentNodeWithAction(node.getParent(),action);
        }

    }
    private AccessibilityNodeInfo scrollAndFindTheNodeWithText(String text) {
        AccessibilityNodeInfo node = findNodeWithText(text);

        if(node != null){
            return  node;
        }else{
            Log.d("Scrolling", "down as we can't find the node with text " + text);
            AccessibilityNodeInfo scrollNode = findNodeWithAction(getRootInActiveWindow(),ACTION_SCROLL_FORWARD);
            if(performAction(scrollNode, ACTION_SCROLL_FORWARD.getId())){
                return scrollAndFindTheNodeWithText(text);
            }else {
                return null;
            }
        }
    }


    private AccessibilityNodeInfo findNodeWithAction(
            AccessibilityNodeInfo root,
            AccessibilityNodeInfo.AccessibilityAction action
    ) {
        Deque<AccessibilityNodeInfo> deque = new ArrayDeque<>();
        deque.add(root);
        while (!deque.isEmpty()) {
            AccessibilityNodeInfo node = deque.removeFirst();
            Log.d(TAG, "findNodeWithAction: afeefafeef "+node.getActionList());
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
        AccessibilityNodeInfo switchNode = findNodeByClassName("android.widget.Switch", nodeGroup);
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
            Log.d(TAG, "findNodeWithText: afeefafeefafeef " + getAllTextFromNode(node));
            if (getAllTextFromNode(node).replace("'","").equalsIgnoreCase(text)) {
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

    private AccessibilityNodeInfo findNodeByClassName(String className,AccessibilityNodeInfo group){
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

        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 1000));
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
                swipe(x2,y1,x3,y3);
                break;
            case Up:
                // Swipe Up
                swipe(x2,y3,x3,y1);
                break;
            case Left:
                // Swipe Left
                swipe(x3,y2,x1,y1);
                break;
            case Right:
                // Swipe Up
                swipe(x1,y2,x3,y1);
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

        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 100));
        dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
        sleep();
    }



    private boolean performGestureClick(AccessibilityNodeInfo node) {
        if (node == null) return false;

        Rect rect = new Rect();
        node.getBoundsInScreen(rect);
        return performGestureClick(rect.centerX(),rect.centerY());
    }
    private boolean performGestureClick(int x, int y) {
        Path clickPath = new Path();
        clickPath.moveTo(x,y);
        GestureDescription.StrokeDescription clickStroke = new GestureDescription.StrokeDescription(clickPath, 0, 10);
        GestureDescription.Builder clickBuilder = new GestureDescription.Builder();
        clickBuilder.addStroke(clickStroke);
        dispatchGesture(clickBuilder.build(), null, null);
        sleep(100);
        return true;
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
        return  performAction(node,ACTION_CLICK.getId());
    }

    private boolean longClick(AccessibilityNodeInfo node){
        return  performAction(node, ACTION_LONG_CLICK.getId());
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
