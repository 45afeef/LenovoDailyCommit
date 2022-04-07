package com.automate.lenovo.Service;


import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK;
import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_LONG_CLICK;
import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_DOWN;
import static android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction.ACTION_SCROLL_FORWARD;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.graphics.Rect;
import android.media.MediaActionSound;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

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
                {"com.android.settings","com.google.android","com.google.android.permissioncontroller"};


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



        if (!windowIdList.contains(accessibilityEvent.getWindowId())) {
            // This is a new Activity
            // Add current windowId to windowIdList to track the window (screen) history
            windowId = accessibilityEvent.getWindowId();
            windowIdList.add(windowId);
            // sleep here as it may takes time to load the content in low configured devices

            switch (accessibilityEvent.getClassName().toString()) {
                case "com.oppo.settings.SettingsActivity":
                case "com.android.settings.homepage.SettingsHomepageActivity":

                    SharedPreferences sharedpreferences = getSharedPreferences("LenovoAutomator", Context.MODE_PRIVATE);
                    boolean canIRun = sharedpreferences.getBoolean("CANIRUN", false);

                    if(canIRun) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean("CANIRUN", false);
                        editor.commit();

                        // wait for a while so that the auto app can make sure that settings app is fully loaded
                        sleep(2000);

                        // Completed Points

                        // Point 2
                        // Point 3
                        // Point 4
                        // Point 5
                        appsAndNotifications();

                        // Point 6. Indicator light
                        // Point 7. Sleep Setting
                        displaySettings();

                        // Point 8. Tablet Volume
                        // already done when click on the automate button

                        // Point 9. Disable TalkBack shortcut
                        turnOffAccessibilityVolume();

                        // Point 10
                        //enableDeveloperOptionsInEmulator();
                        enableDeveloperOptionsInTablet();


                        // Point 1
                        swipeDownForNotification();
                    }
                    break;

            }

        }

    }

    private void turnOffAccessibilityVolume() {
        AccessibilityNodeInfo accessibilityNode = scrollAndFindTheNodeWithText("Accessibility");
        if(click(clickableParent(accessibilityNode))) {
            sleep();
            AccessibilityNodeInfo volumeKeyShortcut = findNodeWithText("Volume key shortcut");
            if (performGestureClick(volumeKeyShortcut)) {
                sleep();
                clickOnTheParentsClickable("Use service");
                sleep();
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep();
            }
            performGlobalAction(GLOBAL_ACTION_BACK);
            sleep();
        }
    }

    private void appsAndNotifications() {
        AccessibilityNodeInfo appAndNotifications = scrollAndFindTheNodeWithText("Apps & notifications");
        if (click(clickableParent(appAndNotifications))) {
            sleep(5000);


            // Point 2. Disable apps:
            // TODO change the node text into "SEE ALL 33(32+1)  APPS"
            AccessibilityNodeInfo seeAllAppBtn = findNodeWithText("See all 33 apps");
            if(seeAllAppBtn == null){
                seeAllAppBtn = findNodeWithText("App info");
            }
            // Disable or Uninstall all apps
            if (performGestureClick(seeAllAppBtn)) {
                sleep();

                // Specific apps (8 apps) to disable or uninstall on lenovo tablet
                disableApp("Android Auto");
                // Point 4. Allow Unknown Apps
                allowUnknownAppsFrom("Chrome");
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
            }
            // Disable or Uninstall of apps completed




            // Point 3. Disable Notifications for all apps
            AccessibilityNodeInfo notificationsNode = findNodeWithText("Notifications");
            if (performGestureClick(notificationsNode)) {
                sleep();

                // a. Don't show notifications on lockscreen
                AccessibilityNodeInfo notificationsOnLockScreen = findNodeWithText("Notifications on lockscreen");
                if (performGestureClick(notificationsOnLockScreen)) {
                    sleep();
                    AccessibilityNodeInfo dontShow = findNodeWithText("Don’t show notifications");
                    performGestureClick(dontShow);
                    sleep();
                }

                // Go to advanced
                AccessibilityNodeInfo advancedNode = scrollAndFindTheNodeWithText("Advanced");
                if (click(clickableParent(advancedNode))) {
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




            // Point 5. Turn OFF Smart Launcher
            AccessibilityNodeInfo defaultApps = findNodeWithText("Default apps");
            if (performGestureClick(defaultApps)) {
                sleep();
                AccessibilityNodeInfo imageButton = findNodeByClassName("android.widget.ImageButton",findNodeWithText("Home app").getParent().getParent());
                if(click(clickableParent(imageButton))){
                    sleep();
                    clickOnTheParentsClickable("Add icon to Home screen");
                    clickOnTheParentsClickable("Show Google App");
                    sleep();
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    sleep();
                }
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep();
            }



            // go back to settings home
            performGlobalAction(GLOBAL_ACTION_BACK);
            sleep();
        }
    }



    private void allowUnknownAppsFrom(String nodeText) {
        AccessibilityNodeInfo chromeNode = scrollAndFindTheNodeWithText(nodeText);
        if (performGestureClick(chromeNode)) {
            sleep();
            AccessibilityNodeInfo advancedNode = scrollAndFindTheNodeWithText("Advanced");
            if(click(clickableParent(advancedNode))){
                sleep();
            }
            AccessibilityNodeInfo installUnknownAppsNode = scrollAndFindTheNodeWithText("Install unknown apps");
            if (performGestureClick(installUnknownAppsNode)) {
                sleep();
                AccessibilityNodeInfo allowTextNode = scrollAndFindTheNodeWithText("Allow from this source");
                AccessibilityNodeInfo allowSwitch = findNodeWithAction(allowTextNode.getParent(), ACTION_CLICK);
                sleep();
                click(allowSwitch);
                sleep();
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep();
            }
            performGlobalAction(GLOBAL_ACTION_BACK);
            sleep();
        }
    }


    private  void displaySettings(){
        // now Goto Display -> Indicator light
        AccessibilityNodeInfo displayNode = scrollAndFindTheNodeWithText("Display");
        if (click(clickableParent(displayNode))) {
            sleep();
            // Indicator Light
            AccessibilityNodeInfo indicatorLightNode = scrollAndFindTheNodeWithText("Indicator light");
            if(click(clickableParent(indicatorLightNode))){
                sleep();
                clickOnTheParentsClickable("Indicator light flashes upon receiving notifications");
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep();
            }

            // Sleep
            AccessibilityNodeInfo sleepNode = scrollAndFindTheNodeWithText("Sleep");
            if(click(clickableParent(sleepNode))){
                AccessibilityNodeInfo fiveMinute = scrollAndFindTheNodeWithText("5 minutes");
                click(clickableParent(fiveMinute));
                sleep(100);
            }

            // go back to settings home
            performGlobalAction(GLOBAL_ACTION_BACK);
            sleep();
        }
    }

    private void clickOnTheParentsClickable(String nodeText) {
        AccessibilityNodeInfo node = scrollAndFindTheNodeWithText(nodeText);
        if(node != null){
            AccessibilityNodeInfo clickableSibling = findNodeWithAction(node.getParent(),ACTION_CLICK);
            performGestureClick(clickableSibling);
            sleep();
        }
    }

    private void disableApp(String appName) {
        AccessibilityNodeInfo appNameNode = scrollAndFindTheNodeWithText(appName);
        if(click(clickableParent(appNameNode))){
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
        if(click( clickableParent(appNameNode))){
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
        if(click( clickableParent(aboutTablet))){
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
        if(click(clickableParent(systemNode))){
            // System subSetting menu opened
            // wait a while
            sleep();
            AccessibilityNodeInfo aboutTablet = scrollAndFindTheNodeWithText("About Tablet");
            if(click(clickableParent(aboutTablet))){
                sleep();
                AccessibilityNodeInfo buildNumber = scrollAndFindTheNodeWithText("Build number");
                for (int i = 0; i<8 ; i++){
                    performGestureClick(buildNumber);
                }
                sleep(500);
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep();
            }
            sleep(2000);
            AccessibilityNodeInfo devOptions = scrollAndFindTheNodeWithText("Developer Options");
            if(click(clickableParent(devOptions))){
                sleep();
                clickOnTheParentsClickable("USB debugging");
                sleep(500);
                AccessibilityNodeInfo ok = findNodeWithText("OK");
                click(clickableParent(ok));
                sleep(100);
                performGlobalAction(GLOBAL_ACTION_BACK);
                sleep();
            }

            performGlobalAction(GLOBAL_ACTION_BACK);
            sleep();
        }
    }

    enum ScrollDirection{
        Down,Up,Left,Right
    }
    private AccessibilityNodeInfo clickableParent(AccessibilityNodeInfo node) {
        return getParentNodeWithAction(node, ACTION_CLICK);
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
        if (root == null){return null;}

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
//        performGestureClick(120,250);
//        performGestureClick(280,250);
//        performGestureClick(430,250);
//        performGestureClick(580,250);
                // Specific points for lenovo tablets
                performGestureClick(200, 145);
                performGestureClick(325, 145);
                performGestureClick(450, 145);
                sleep();
            }
        }, null);
        sleep(2000);

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
//2. Disable apps: (Can apps be disable programmatically?
//    Ok if not)
//    a. In Settings → Apps & notifications → See all apps
//    b. Disable or Uninstall→and confirm Disable or Uninstall
//            (Disable the following apps if possible)
//    i. Android Auto
//    ii. Digital Wellbeing
//    iii. Google Play Movies and TV
//    iv. Keep notes
//    v. MusicFX
//    vi. Sound Recorder
//    vii. YouTube
//    viii.YouTube Music
//3. Disable Notifications for all apps
//    a. In Settings → Apps & notifications → Notifications
//    b. Set Notifications to Don’t Show
//    i. Set Don’t show notifications
//    c. Set these Advanced settings as toggle OFF:
//    i. Suggested actions and replies OFF
//    ii. Allow notifications dots OFF
//    iii. Blink lights OFF
//    iv. Display status bar icons OFF
//4. Allow Unknown Apps
//    i. In Settings → Apps & notifications
//    ii. On the apps list select Chrome → Install unknown apps
//→ set ON Allow from this source.
//6. Indicator light
//    a. In Settings → Display → Indicator Light
//    b. Toggle OFF indicator light flashes upon receiving
//            notifications
//7. Sleep Setting
//    a. In Settings → Display → Sleep → Select 5 min
//1. Turn ON Bluetooth, turn OFF auto-rotate, turn OFF
//    notification volume.
//            1. Bluetooth icon ON, auto-rotate icon OFF, notifications
//            OFF
//8. Tablet volume
//    a. Sound → Set Media Volume to about 50%
//    b. Set both Alarm volume & Notification volume down
//    to 0%.
//9. Disable TalkBack shortcut
//    a. In Accessibility -> select Volume key shortcut ->
//    toggle OFF Use Service setting.





















//    Android Settings to Set Programmatically
//5. Turn OFF Smart Launcher
//    i. In Apps & Notifications → Default apps → set these
//    settings
//    ii. Set OFF “Add icon to Home screen”
//    iii. Set OFF “Show Google App”
//10. Set USB debugging ON.
//    Under settings->system->Developer options, turn ON
//    USB debugging (enabled).
//            (If done manually, need to click the build number 7 times in
//    settings->system->About tablet to enable developer option.)
//