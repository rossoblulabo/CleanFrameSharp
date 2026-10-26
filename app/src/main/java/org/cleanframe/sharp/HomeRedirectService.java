package org.cleanframe.sharp;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

/** Optional Home-only input filter. No window watching, delayed launches or key history. */
public class HomeRedirectService extends AccessibilityService {
    private static HomeRedirectService active;
    private long homeDownTime=Long.MIN_VALUE;
    private boolean consumeHome;
    private String lastResult="No Home press received in this service session";

    static boolean connected(){return active!=null;}
    static String lastResult(){return active==null?"Service disconnected":active.lastResult;}
    static void refreshFilter(){if(active!=null)active.configureFilter();}
    private void configureFilter(){
        AccessibilityServiceInfo info=getServiceInfo();
        if(info==null)return;
        info.eventTypes=0;
        if(HomeRedirectController.requested(this)||consumeHome)info.flags|=AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        else info.flags&=~AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        setServiceInfo(info);
    }
    @Override protected void onServiceConnected(){
        super.onServiceConnected();active=this;homeDownTime=Long.MIN_VALUE;consumeHome=false;configureFilter();
    }
    @Override public boolean onUnbind(Intent intent){if(active==this)active=null;return super.onUnbind(intent);}
    @Override public void onDestroy(){if(active==this)active=null;super.onDestroy();}
    @Override public void onInterrupt(){}
    @Override public void onAccessibilityEvent(AccessibilityEvent event){/* No window-based fallback. */}

    @Override protected boolean onKeyEvent(KeyEvent event){
        // Pass every other key untouched, without recording its value, text or timing.
        if(event==null||event.getKeyCode()!=KeyEvent.KEYCODE_HOME)return false;
        if(event.getAction()==KeyEvent.ACTION_UP){
            boolean handled=homeDownTime==event.getDownTime()&&consumeHome;
            homeDownTime=Long.MIN_VALUE;consumeHome=false;
            if(handled&&!event.isCanceled()&&HomeRedirectController.requested(this)){
                try{
                    // Open on release, as normal Home does. No UI work is scheduled by DOWN,
                    // so cold-start rendering cannot delay consumption of the matching UP.
                    startActivity(new Intent(Intent.ACTION_MAIN).setClass(this,MainActivity.class)
                        .addCategory(Intent.CATEGORY_HOME)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));
                    lastResult="Home received and consumed; launch requested";
                }catch(RuntimeException error){
                    lastResult="Home launch failed; interception paused";
                    HomeRedirectController.failClosed(this,"Android refused the Home launch. Interception is paused; the next Home press follows the original system path.");
                }
            }
            if(!HomeRedirectController.requested(this))configureFilter();
            return handled;
        }
        if(event.getAction()!=KeyEvent.ACTION_DOWN)return false;
        // Repeated DOWNs can retain repeatCount=0 after an accessibility input filter.
        if(homeDownTime==event.getDownTime())return consumeHome;
        homeDownTime=event.getDownTime();consumeHome=false;
        if(event.getRepeatCount()!=0||event.isCanceled()||!HomeRedirectController.requested(this))return false;
        PowerManager power=(PowerManager)getSystemService(Context.POWER_SERVICE);
        KeyguardManager keyguard=(KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        if((power!=null&&!power.isInteractive())||(keyguard!=null&&keyguard.isKeyguardLocked()))return false;
        consumeHome=true;
        lastResult="Home DOWN received and consumed; waiting for release";
        return true;
    }
}
