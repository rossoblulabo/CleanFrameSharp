
package org.cleanframe.sharp;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.view.accessibility.AccessibilityManager;

/** Own-app controls only. Never writes Android settings or changes another package. */
public final class HomeRedirectController {
    public static final String GOOGLE_PACKAGE="com.google.android.tvlauncher";
    public static final String GOOGLE_ACTIVITY="com.google.android.tvlauncher.MainActivity";
    private HomeRedirectController(){}
    static SharedPreferences prefs(Context c){return c.getSharedPreferences("favorites",Context.MODE_PRIVATE);}
    public static boolean requested(Context c){return prefs(c).getBoolean("homeIntercept",false);}
    public static boolean setRequested(Context c,boolean enabled){
        boolean saved=prefs(c).edit().putBoolean("homeIntercept",enabled).remove("homeRedirectError").commit();
        if(saved)HomeRedirectService.refreshFilter();
        return saved;
    }
    // Persist the pause synchronously before another launcher event can be processed.
    @android.annotation.SuppressLint("ApplySharedPref")
    static void failClosed(Context c,String reason){prefs(c).edit().putBoolean("homeIntercept",false).putString("homeRedirectError",reason).commit();}
    public static String error(Context c){return prefs(c).getString("homeRedirectError","");}
    public static boolean accessibilityEnabled(Context c){
        AccessibilityManager manager=(AccessibilityManager)c.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if(manager==null)return false;
        for(AccessibilityServiceInfo info:manager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)){
            if(info.getResolveInfo()==null||info.getResolveInfo().serviceInfo==null)continue;
            ServiceInfo service=info.getResolveInfo().serviceInfo;
            String name=service.name.startsWith(".")?service.packageName+service.name:service.name;
            if(c.getPackageName().equals(service.packageName)&&HomeRedirectService.class.getName().equals(name))return true;
        }
        return false;
    }
    public static String status(Context c){
        if(!requested(c))return "Off";
        if(!accessibilityEnabled(c))return "Needs Accessibility access";
        return HomeRedirectService.connected()?"Ready · press Home to verify":"Waiting for Android";
    }
    public static void openGoogle(Context c){
        // Pause persistently BEFORE launch, so Google Home remains usable and is an escape route.
        if(!setRequested(c,false))throw new IllegalStateException("Could not pause Home redirect");
        Intent i=new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(GOOGLE_PACKAGE,GOOGLE_ACTIVITY));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        c.startActivity(i);
    }
}

