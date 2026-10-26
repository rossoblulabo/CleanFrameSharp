package org.cleanframe.sharp;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ServiceController;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.KeyguardManager;
import android.content.*;
import android.content.pm.*;
import android.content.res.XmlResourceParser;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29,shadows=HomeRedirectServiceTest.ConnectedServiceShadow.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class HomeRedirectServiceTest {
    // Model the service-info roundtrip normally provided by a bound system connection.
    @org.robolectric.annotation.Implements(android.accessibilityservice.AccessibilityService.class)
    public static class ConnectedServiceShadow extends org.robolectric.shadows.ShadowAccessibilityService {
        private AccessibilityServiceInfo info;
        @org.robolectric.annotation.Implementation public AccessibilityServiceInfo getServiceInfo(){return info;}
        @org.robolectric.annotation.Implementation public void setServiceInfo(AccessibilityServiceInfo value){info=value;}
    }
    ServiceController<HomeRedirectService> controller;
    HomeRedirectService service;
    @Before public void setup(){
        RuntimeEnvironment.getApplication().getSharedPreferences("favorites",Context.MODE_PRIVATE).edit().clear().commit();
        controller=Robolectric.buildService(HomeRedirectService.class).create();service=controller.get();
        service.setServiceInfo(new AccessibilityServiceInfo());service.onServiceConnected();
        Shadows.shadowOf((PowerManager)service.getSystemService(Context.POWER_SERVICE)).setIsInteractive(true);
    }
    @After public void close(){controller.destroy();}
    KeyEvent home(long time,int action){return new KeyEvent(time,time+10,action,KeyEvent.KEYCODE_HOME,0);}
    Intent next(){return Shadows.shadowOf(service).getNextStartedActivity();}
    void enabled(){assertTrue(HomeRedirectController.setRequested(service,true));}
    boolean filtering(){return (service.getServiceInfo().flags&AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS)!=0;}

    @Test public void disabledByDefaultAndOldWindowConsentDoesNotEnableNewAccess(){
        service.getSharedPreferences("favorites",Context.MODE_PRIVATE).edit().putBoolean("homeRedirect",true).commit();
        assertFalse(HomeRedirectController.requested(service));assertFalse(filtering());
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertNull(next());
    }
    @Test public void firstPressConsumesBothHalvesAndLaunchesHomeExactlyOnce(){
        enabled();assertTrue(filtering());assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));
        assertNull(next());assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));
        Intent i=next();assertNotNull(i);assertEquals(MainActivity.class.getName(),i.getComponent().getClassName());
        assertEquals(Intent.ACTION_MAIN,i.getAction());assertTrue(i.hasCategory(Intent.CATEGORY_HOME));
        assertTrue((i.getFlags()&Intent.FLAG_ACTIVITY_NEW_TASK)!=0);
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertNull(next());
        assertTrue(HomeRedirectService.lastResult().contains("launch requested"));
    }
    @Test public void heldHomeAndZeroRepeatDuplicatesDoNotLaunchAgain(){
        enabled();assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));assertNull(next());
        for(int repeat:new int[]{0,0,1,5}){
            assertTrue(service.onKeyEvent(new KeyEvent(100,400,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_HOME,repeat)));assertNull(next());
        }
        assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertNotNull(next());assertNull(next());
    }
    @Test public void rapidSeparatePressesEachLaunchOnceWithoutDebounceDelay(){
        enabled();
        for(long time:new long[]{100,200,300,400,500,600}){
            assertTrue(service.onKeyEvent(home(time,KeyEvent.ACTION_DOWN)));assertNull(next());
            assertTrue(service.onKeyEvent(home(time,KeyEvent.ACTION_UP)));assertNotNull(next());assertNull(next());
        }
        assertTrue(HomeRedirectController.requested(service));
    }
    @Test public void everyNonHomeKeyPassesThrough(){
        enabled();
        for(int code=0;code<=KeyEvent.getMaxKeyCode();code++){
            if(code==KeyEvent.KEYCODE_HOME)continue;
            for(int action:new int[]{KeyEvent.ACTION_DOWN,KeyEvent.ACTION_UP})
                assertFalse("Key "+code,service.onKeyEvent(new KeyEvent(action,code)));
        }
        assertFalse(service.onKeyEvent(null));assertNull(next());
    }
    @Test public void googleWindowsNeverTriggerDelayedFallback(){
        enabled();
        for(String pkg:new String[]{HomeRedirectController.GOOGLE_PACKAGE,"org.cleanframe.sharp","com.google.android.youtube.tv"}){
            AccessibilityEvent e=AccessibilityEvent.obtain(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);e.setPackageName(pkg);
            service.onAccessibilityEvent(e);assertNull(next());
        }
        service.onAccessibilityEvent(null);assertNull(next());
    }
    @Test public void enablingBetweenDownAndUpDoesNotConsumeOrLaunchOrphanUp(){
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));enabled();
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertNull(next());
        assertFalse(service.onKeyEvent(home(200,KeyEvent.ACTION_UP)));assertNull(next());
    }
    @Test public void pauseDuringPressStillConsumesMatchingUpThenReleasesFilter(){
        enabled();assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));assertNull(next());
        HomeRedirectController.setRequested(service,false);assertTrue(filtering());
        assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertFalse(filtering());
        assertFalse(service.onKeyEvent(home(200,KeyEvent.ACTION_DOWN)));
        assertFalse(service.onKeyEvent(home(200,KeyEvent.ACTION_UP)));assertNull(next());
    }
    @Test public void screenOffHomePairPassesToSystem(){
        enabled();Shadows.shadowOf((PowerManager)service.getSystemService(Context.POWER_SERVICE)).setIsInteractive(false);
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertNull(next());
    }
    @Test public void lockedHomePairPassesToSystem(){
        enabled();Shadows.shadowOf((KeyguardManager)service.getSystemService(Context.KEYGUARD_SERVICE)).setKeyguardLocked(true);
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertNull(next());
    }
    @Test public void repeatWithoutInitialDownPassesToSystem(){
        enabled();assertFalse(service.onKeyEvent(new KeyEvent(100,200,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_HOME,2)));
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertNull(next());
    }
    public static class BlockedService extends HomeRedirectService {
        @Override public void startActivity(Intent intent){throw new SecurityException("Simulated OEM block");}
    }
    @Test public void refusedLaunchPausesAndNextPairPassesToSystem(){
        ServiceController<BlockedService> blocked=Robolectric.buildService(BlockedService.class).create();
        try{
            HomeRedirectController.setRequested(blocked.get(),true);
            assertTrue(blocked.get().onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));
            assertTrue(blocked.get().onKeyEvent(home(100,KeyEvent.ACTION_UP)));
            assertFalse(blocked.get().onKeyEvent(home(200,KeyEvent.ACTION_DOWN)));assertFalse(blocked.get().onKeyEvent(home(200,KeyEvent.ACTION_UP)));
            assertFalse(HomeRedirectController.requested(blocked.get()));assertFalse(HomeRedirectController.error(blocked.get()).isEmpty());
        }finally{blocked.destroy();}
    }
    @Test public void openingGooglePausesBeforeLaunch(){
        enabled();HomeRedirectController.openGoogle(service);
        Intent i=next();assertEquals(HomeRedirectController.GOOGLE_PACKAGE,i.getComponent().getPackageName());
        assertEquals(HomeRedirectController.GOOGLE_ACTIVITY,i.getComponent().getClassName());
        assertFalse(HomeRedirectController.requested(service));assertFalse(filtering());
        assertFalse(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));assertNull(next());
    }
    @Test public void metadataRequestsOnlyKeyFilteringWithoutWindowAccess() throws Exception {
        ServiceInfo info=service.getPackageManager().getServiceInfo(new ComponentName(service,HomeRedirectService.class),PackageManager.GET_META_DATA);
        assertEquals("android.permission.BIND_ACCESSIBILITY_SERVICE",info.permission);assertTrue(info.exported);
        String ns="http://schemas.android.com/apk/res/android";
        try(XmlResourceParser p=service.getResources().getXml(R.xml.home_redirect_service)){
            while(p.next()!=XmlResourceParser.START_TAG){}
            assertEquals(0,p.getAttributeIntValue(ns,"accessibilityEventTypes",0));
            assertTrue(p.getAttributeBooleanValue(ns,"canRequestFilterKeyEvents",false));
            for(String key:new String[]{"canRetrieveWindowContent","canRequestTouchExplorationMode","canPerformGestures"})
                assertFalse(key,p.getAttributeBooleanValue(ns,key,true));
        }
    }
    @Test public void interruptionDoesNotLaunchAnything(){service.onInterrupt();assertNull(next());}
    @Test public void cancelledReleaseIsConsumedWithoutOpeningAnApp(){
        enabled();assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));
        KeyEvent release=KeyEvent.changeFlags(home(100,KeyEvent.ACTION_UP),KeyEvent.FLAG_CANCELED);
        assertTrue(service.onKeyEvent(release));assertNull(next());
    }
    @Test public void reconnectedServiceRestoresOptInFilterWithoutLaunchingAnApp(){
        enabled();service.onUnbind(new Intent());assertFalse(HomeRedirectService.connected());
        service.onServiceConnected();assertTrue(filtering());assertTrue(HomeRedirectService.connected());assertNull(next());
        assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_DOWN)));
        assertTrue(service.onKeyEvent(home(100,KeyEvent.ACTION_UP)));assertNotNull(next());
    }}




