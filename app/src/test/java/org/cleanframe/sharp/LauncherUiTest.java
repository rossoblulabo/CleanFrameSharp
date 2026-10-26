
package org.cleanframe.sharp;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.robolectric.android.controller.ActivityController;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=29, qualifiers="w960dp-h540dp-land-xhdpi")
@LooperMode(LooperMode.Mode.PAUSED)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LauncherUiTest {
    MainActivity a;
    ActivityController<MainActivity> controller;
    android.content.SharedPreferences prefs;
    @Before public void setup(){
        Context c=RuntimeEnvironment.getApplication();
        prefs=c.getSharedPreferences("favorites",Context.MODE_PRIVATE);
        prefs.edit().clear().putString("ordered","[\"test.netflix\",\"test.youtube\",\"test.plex\"]").commit();
        addApp("test.netflix","Netflix");addApp("test.youtube","YouTube");addApp("test.plex","Plex");
        addApp("com.android.vending","Google Play Store");
        controller=Robolectric.buildActivity(MainActivity.class).setup().visible().windowFocusChanged(true);
        a=controller.get();settle();
    }
    @After public void close(){if(controller!=null)controller.pause().stop().destroy();}
    void addApp(String pkg,String name){
        PackageInfo p=new PackageInfo();p.packageName=pkg;
        ApplicationInfo info=new ApplicationInfo();info.packageName=pkg;info.nonLocalizedLabel=name;info.enabled=true;
        info.icon=android.R.drawable.sym_def_app_icon;p.applicationInfo=info;
        Shadows.shadowOf(RuntimeEnvironment.getApplication().getPackageManager()).installPackage(p);
        ResolveInfo r=new ResolveInfo();r.activityInfo=new ActivityInfo();r.activityInfo.packageName=pkg;
        r.activityInfo.name=pkg+".MainActivity";r.activityInfo.applicationInfo=info;r.nonLocalizedLabel=name;
        Shadows.shadowOf(RuntimeEnvironment.getApplication().getPackageManager()).addResolveInfoForIntent(
            new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),r);
    }
    void settle(){
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        View v=a.getWindow().getDecorView();
        v.measure(View.MeasureSpec.makeMeasureSpec(1920,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(1080,View.MeasureSpec.EXACTLY));
        v.layout(0,0,1920,1080);Shadows.shadowOf(Looper.getMainLooper()).idle();
    }
    View tag(String name){return a.getWindow().getDecorView().findViewWithTag(name);}
    void click(String name){assertNotNull(name,tag(name));tag(name).performClick();settle();}
    void key(int code){a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,code));a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,code));settle();}
    void focus(String name){assertNotNull("Expected initial focus on "+name,a.findViewById(android.R.id.content).findFocus());assertEquals(name,a.findViewById(android.R.id.content).findFocus().getTag());}
    void capture(String file) throws Exception {
        settle();View content=a.findViewById(android.R.id.content);
        Bitmap b=Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);
        content.draw(new Canvas(b));
        File out=new File(System.getProperty("cleanframe.screenshots"),file+".png");out.getParentFile().mkdirs();
        try(FileOutputStream stream=new FileOutputStream(out)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,stream));}b.recycle();
    }
    @Test public void initialFocusAndOnePressMovesToNextApp() throws Exception {
        focus("home:test.netflix");key(KeyEvent.KEYCODE_DPAD_DOWN);focus("home:test.youtube");
        key(KeyEvent.KEYCODE_DPAD_DOWN);focus("home:test.plex");
        key(KeyEvent.KEYCODE_DPAD_DOWN);focus("allApps");key(KeyEvent.KEYCODE_DPAD_RIGHT);focus("settings");
        assertNull(tag("app:com.android.vending"));capture("list");
    }
    @Test public void explicitLayoutChoiceDoesNotApplyOnFocus() throws Exception {
        click("settings");focus("layout");capture("settings");click("layout");focus("choice:list");
        key(KeyEvent.KEYCODE_DPAD_DOWN);focus("choice:dock");assertEquals("list",prefs.getString("layout","list"));
        key(KeyEvent.KEYCODE_DPAD_CENTER);assertEquals("dock",prefs.getString("layout","list"));
        a.onBackPressed();settle();focus("layout");a.onBackPressed();settle();focus("home:test.netflix");
        key(KeyEvent.KEYCODE_DPAD_RIGHT);focus("home:test.youtube");
        capture("dock");key(KeyEvent.KEYCODE_DPAD_DOWN);focus("allApps");
    }
    @Test public void wallpaperBrowseIsTemporaryAndAllFifteenArePresent() throws Exception {
        click("settings");click("background");click("wallpapers");focus("wall:misty");
        String[] ids={"misty","cove","forest","valley","night","peaks","bridge","ridge","golden","summit","goldcoast","sydney","amber","mint","alpine"};
        for(String id:ids)assertNotNull(id,tag("wall:"+id));
        key(KeyEvent.KEYCODE_DPAD_DOWN);focus("wall:cove");
        assertEquals("misty",prefs.getString("wallpaper","misty"));capture("wallpapers");
        key(KeyEvent.KEYCODE_DPAD_CENTER);assertEquals("cove",prefs.getString("wallpaper","misty"));
        a.onBackPressed();settle();assertNotNull(tag("wallpapers"));
    }
    @Test public void protectedPackagesNeverOfferCommands() {
        click("settings");click("privacy");click("packages");click("package:com.google.android.katniss");
        assertNull(tag("disable"));assertNull(tag("enable"));assertNotNull(tag("keep"));
        a.onBackPressed();settle();click("package:jp.co.sharp.av.android.tvlauncher");
        assertNull(tag("disable"));assertNull(tag("enable"));assertNotNull(tag("keep"));
        a.onBackPressed();settle();click("package:jp.co.sharp.av.android.analyticsservice");
        assertNotNull(tag("disable"));assertNotNull(tag("enable"));assertFalse(tag("disable").isClickable());
    }
    @Test public void allAppsIncludesPlayStoreAndMembershipPersists(){
        click("allApps");assertNotNull(tag("app:com.android.vending"));click("app:com.android.vending");click("membership");
        assertTrue(prefs.getString("ordered","").contains("com.android.vending"));
        a.onBackPressed();settle();a.onBackPressed();settle();assertNotNull(tag("home:com.android.vending"));
    }
    @Test public void renderHomeAndSettings() throws Exception {capture("home-initial");click("settings");capture("settings-initial");a.onBackPressed();settle();click("allApps");capture("all-apps");}

    @Test public void typographyOptionsApplyOnlyOnOk(){
        click("settings");click("typography");click("font");focus("choice:Manrope");
        key(KeyEvent.KEYCODE_DPAD_DOWN);focus("choice:Geist");
        assertEquals("Manrope",prefs.getString("font","Manrope"));key(KeyEvent.KEYCODE_DPAD_CENTER);
        assertEquals("Geist",prefs.getString("font","Manrope"));
        a.onBackPressed();settle();click("textsize");focus("choice:18");key(KeyEvent.KEYCODE_DPAD_UP);focus("choice:16");
        assertEquals(18,prefs.getInt("textSize4",18));key(KeyEvent.KEYCODE_DPAD_CENTER);
        assertEquals(16,prefs.getInt("textSize4",18));
    }
    @Test public void repeatedOkDoesNotToggleMembershipMoreThanOnce(){
        click("allApps");click("app:com.android.vending");tag("membership").requestFocus();settle();
        a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_CENTER));
        a.dispatchKeyEvent(new KeyEvent(0,10,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_CENTER,1));
        a.dispatchKeyEvent(new KeyEvent(0,20,KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_DPAD_CENTER,2));
        assertFalse(prefs.getString("ordered","").contains("com.android.vending"));
        a.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,KeyEvent.KEYCODE_DPAD_CENTER));settle();
        assertTrue(prefs.getString("ordered","").contains("com.android.vending"));
    }
    @Test public void allWallpaperFilesDecode() throws Exception {
        for(String id:new String[]{"misty","cove","forest","valley","night","peaks","bridge","ridge","golden","summit","goldcoast","sydney","amber","mint","alpine"}){
            try(InputStream stream=a.getAssets().open("wallpapers/"+id+".jpg")){
                Bitmap b=BitmapFactory.decodeStream(stream);assertNotNull(id,b);assertTrue(b.getWidth()>100);b.recycle();
            }
        }
    }

    @Test public void homeSetupChangesOnlyOwnOptInWithoutGrantingSystemAccess(){
        String before=android.provider.Settings.Secure.getString(a.getContentResolver(),"enabled_accessibility_services");
        click("settings");click("homeButton");assertFalse(prefs.getBoolean("homeIntercept",false));click("redirectToggle");
        assertTrue(prefs.getBoolean("homeIntercept",false));
        Intent intent=Shadows.shadowOf(a).getNextStartedActivity();
        assertNull(intent);
        assertEquals(before,android.provider.Settings.Secure.getString(a.getContentResolver(),"enabled_accessibility_services"));
        click("redirectToggle");assertFalse(prefs.getBoolean("homeIntercept",false));
    }
    @Test public void googleWarningRequiresConfirmationAndCancelKeepsHomeEnabled(){
        HomeRedirectController.setRequested(a,true);click("settings");assertNull(tag("googleHome"));click("homeButton");
        tag("googleHome").requestFocus();click("googleHome");
        android.app.AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();
        assertTrue(dialog.isShowing());assertTrue(HomeRedirectController.requested(a));
        assertTrue(dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).hasFocus());
        assertNull(Shadows.shadowOf(a).getNextStartedActivity());
        dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).performClick();settle();
        assertTrue(HomeRedirectController.requested(a));focus("googleHome");
        click("googleHome");dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();
        dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).performClick();settle();
        assertFalse(HomeRedirectController.requested(a));
        Intent intent=Shadows.shadowOf(a).getNextStartedActivity();
        assertEquals("com.google.android.tvlauncher/.MainActivity",intent.getComponent().flattenToShortString());
    }
    @Test public void largerHomeTextIsOpaqueSemiboldAndShadowed() throws Exception {
        LinearLayout second=(LinearLayout)tag("home:test.youtube");
        TextView title=(TextView)second.getChildAt(0);
        assertEquals(18*1.2f*a.getResources().getDisplayMetrics().scaledDensity,title.getTextSize(),.1f);
        assertEquals(1f,second.getAlpha(),.001f);assertTrue(title.getFontVariationSettings().contains("600"));assertTrue(title.getShadowRadius()>0);
        prefs.edit().putString("wallpaper","mint").commit();
        a.onNewIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));settle();capture("list-bright");
        click("settings");click("homeButton");capture("home-button");
    }


    @Test public void ordinaryAppLaunchEntryExistsForRemappers(){
        android.content.pm.PackageManager pm=a.getPackageManager();
        Intent ordinary=pm.getLaunchIntentForPackage(a.getPackageName());
        assertNotNull("Standard app launch must resolve for button remappers",ordinary);
        assertEquals(MainActivity.class.getName(),ordinary.getComponent().getClassName());
        Intent tv=pm.getLeanbackLaunchIntentForPackage(a.getPackageName());
        assertNotNull(tv);assertEquals(ordinary.getComponent(),tv.getComponent());
    }

    @Test public void emptyHomeStartsOnAllApps(){
        prefs.edit().putString("ordered","[]").commit();
        a.onNewIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));settle();
        focus("allApps");key(KeyEvent.KEYCODE_DPAD_RIGHT);focus("settings");
    }
    @Test public void everyHomeAndAppEntryOpensHomeWithoutLaunchingAnotherApp(){
        for(String category:new String[]{Intent.CATEGORY_HOME,Intent.CATEGORY_LAUNCHER,Intent.CATEGORY_LEANBACK_LAUNCHER}){
            controller.pause().stop().destroy();controller=null;
            Intent entry=new Intent(Intent.ACTION_MAIN).addCategory(category)
                .setComponent(new ComponentName(RuntimeEnvironment.getApplication(),MainActivity.class));
            controller=Robolectric.buildActivity(MainActivity.class,entry).setup().visible().windowFocusChanged(true);
            a=controller.get();settle();
            assertNotNull(category,tag("home:test.netflix"));
            assertNull(category,Shadows.shadowOf(a).getNextStartedActivity());
            assertFalse(category,a.isFinishing());
        }
    }
    @Test public void remapperRelaunchReturnsFromSettingsAndAllAppsOnFirstIntent(){
        for(String category:new String[]{Intent.CATEGORY_HOME,Intent.CATEGORY_LAUNCHER,Intent.CATEGORY_LEANBACK_LAUNCHER}){
            for(String pageTag:new String[]{"settings","allApps"}){
                click(pageTag);controller.pause().stop();
                a.onNewIntent(new Intent(Intent.ACTION_MAIN).addCategory(category));
                controller.restart().start().resume().visible().windowFocusChanged(true);settle();
                assertNotNull(category+" from "+pageTag,tag("home:test.netflix"));
                assertNull(Shadows.shadowOf(a).getNextStartedActivity());
                assertFalse(a.isFinishing());
            }
        }
    }

    @Test public void weightChoiceAppliesOnlyOnOkAndBackRestoresItsRow() throws Exception {
        click("settings");click("typography");tag("textWeight").requestFocus();click("textWeight");
        focus("choice:600");assertTrue(tag("choice:600").isSelected());
        key(KeyEvent.KEYCODE_DPAD_UP);focus("choice:500");
        assertEquals("600",prefs.getString("homeTextWeight","600"));
        key(KeyEvent.KEYCODE_DPAD_CENTER);assertEquals("500",prefs.getString("homeTextWeight","600"));
        assertTrue(tag("choice:500").isSelected());assertFalse(tag("choice:600").isSelected());capture("text-weight-original");
        a.onBackPressed();settle();focus("textWeight");
        a.onBackPressed();settle();a.onBackPressed();settle();
        assertEquals(500,((TextView)((LinearLayout)tag("home:test.netflix")).getChildAt(0)).getTypeface().getWeight());
        capture("list-original");controller.recreate();a=controller.get();settle();
        assertEquals("500",prefs.getString("homeTextWeight","600"));
        assertEquals(500,((TextView)((LinearLayout)tag("home:test.netflix")).getChildAt(0)).getTypeface().getWeight());
    }
    TextClock findClock(View v){
        if(v instanceof TextClock)return (TextClock)v;
        if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){
            TextClock clock=findClock(((ViewGroup)v).getChildAt(i));if(clock!=null)return clock;
        }
        return null;
    }
    @Test public void allWeightsKeepShadingAndApplyToHomeFooterAndClockInBothLayouts() throws Exception {
        for(String layout:new String[]{"list","dock"})for(String weight:new String[]{"300","500","600"}){
            prefs.edit().putString("layout",layout).putString("homeTextWeight",weight).commit();
            a.onNewIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));settle();
            LinearLayout item=(LinearLayout)tag("home:test.youtube");
            TextView name=(TextView)item.getChildAt("dock".equals(layout)?1:0);
            assertEquals(Integer.parseInt(weight),name.getTypeface().getWeight());
            assertEquals(Integer.parseInt(weight),((TextView)tag("allApps")).getTypeface().getWeight());
            assertEquals(Integer.parseInt(weight),findClock(a.findViewById(android.R.id.content)).getTypeface().getWeight());
            assertEquals(1f,item.getAlpha(),.001f);assertTrue(name.getShadowRadius()>0);
            assertEquals(18,prefs.getInt("shade4",18));
            capture(layout+"-weight-"+weight);
        }
    }
    @Test public void originalWeightWorksWithEveryTypeface(){
        for(String face:new String[]{"Manrope","Geist","Newsreader"}){
            click("settings");click("typography");click("font");click("choice:"+face);
            a.onBackPressed();settle();click("textWeight");click("choice:500");
            a.onBackPressed();settle();a.onBackPressed();settle();a.onBackPressed();settle();
            TextView name=(TextView)((LinearLayout)tag("home:test.netflix")).getChildAt(0);
            assertEquals(500,name.getTypeface().getWeight());assertEquals(face,prefs.getString("font","Manrope"));
        }
    }
    @Test public void aboutHasOnlyBackFocusableAndPlainText() throws Exception {
        click("settings");click("about");focus("aboutBack");
        assertNotNull(tag("version"));assertNotNull(tag("aboutBlurb"));assertNull(tag("aboutCredits"));
        java.util.ArrayList<View> focusable=new java.util.ArrayList<>();
        a.findViewById(android.R.id.content).addFocusables(focusable,View.FOCUS_DOWN);
        assertEquals(1,focusable.size());assertEquals(tag("aboutBack"),focusable.get(0));
        assertFalse(tag("version").isClickable());key(KeyEvent.KEYCODE_DPAD_DOWN);focus("aboutBack");capture("about");
        key(KeyEvent.KEYCODE_DPAD_CENTER);focus("about");
    }
    @Test public void systemUsesFullWidthWithoutDuplicateLauncherShortcut() throws Exception {
        click("settings");click("privacy");assertNull(tag("original"));
        assertTrue(tag("packages").getWidth()>1500);assertTrue(tag("androidSettings").getWidth()>1500);
        capture("system");
    }
    @Test public void accessibilityShowsDirectionsWithoutLaunchingSettings() throws Exception {
        click("settings");click("homeButton");click("accessibility");
        android.app.AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);assertTrue(dialog.isShowing());
        TextView message=dialog.findViewById(android.R.id.message);
        assertTrue(message.getText().toString().contains("Other settings"));
        assertTrue(message.getText().toString().contains("CleanFrame Home button"));
        assertTrue(dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).hasFocus());
        assertNull(Shadows.shadowOf(a).getNextStartedActivity());dialog.dismiss();settle();
        focus("accessibility");
    }

    ReadabilityScrim findScrim(View view){
        if(view instanceof ReadabilityScrim)return (ReadabilityScrim)view;
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++){
            ReadabilityScrim found=findScrim(((ViewGroup)view).getChildAt(i));if(found!=null)return found;
        }
        return null;
    }
    @Test public void homeShadeLeavesClockAndAreaPastSettingsClear() throws Exception {
        for(String layout:new String[]{"list","dock"}){
            prefs.edit().putString("layout",layout).commit();
            a.onNewIntent(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));settle();
            ReadabilityScrim scrim=findScrim(a.findViewById(android.R.id.content));assertNotNull(scrim);
            Bitmap shade=Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);scrim.draw(new Canvas(shade));
            assertEquals("No shade behind the clock",0,Color.alpha(shade.getPixel(1700,90)));
            int[] settingsPosition=new int[2];tag("settings").getLocationOnScreen(settingsPosition);
            int beyond=Math.min(1919,settingsPosition[0]+tag("settings").getWidth()+60);
            assertEquals("No left shade beyond Settings",0,Color.alpha(shade.getPixel(beyond,100)));
            if(layout.equals("list"))assertTrue("Keep list readability",Color.alpha(shade.getPixel(20,100))>0);
            else assertEquals(0,Color.alpha(shade.getPixel(20,100)));
            assertTrue("Keep footer readability",Color.alpha(shade.getPixel(1000,1070))>0);
            if(layout.equals("list")){
                TextView settings=(TextView)tag("settings");
                float firstT=settingsPosition[0]+settings.getPaddingLeft()+settings.getPaint().measureText("Setti");
                float edge=settingsPosition[0]+settings.getWidth()+48;
                assertEquals(112,Color.alpha(shade.getPixel(Math.round(firstT)-4,100)),2);
                for(int step=1;step<=3;step++){
                    int x=Math.round(firstT+(edge-firstT)*step/4f);
                    assertEquals("Even fade at quarter "+step,Math.round(112*(1-step/4f)),Color.alpha(shade.getPixel(x,100)),3);
                }
            }
            shade.recycle();capture(layout+"-1.1.0");
        }
    }


    String allText(View view){
        StringBuilder text=new StringBuilder();if(view instanceof TextView)text.append(((TextView)view).getText()).append('\n');
        if(view instanceof ViewGroup)for(int i=0;i<((ViewGroup)view).getChildCount();i++)text.append(allText(((ViewGroup)view).getChildAt(i)));
        return text.toString();
    }
    @Test public void japaneseLanguagePersistsAndKeepsInstalledAppNames() throws Exception {
        addApp("test.settings","Settings");click("settings");tag("language").requestFocus();click("language");focus("choice:en");
        key(KeyEvent.KEYCODE_DPAD_DOWN);focus("choice:ja");assertEquals("en",prefs.getString("language","en"));
        key(KeyEvent.KEYCODE_DPAD_CENTER);assertEquals("ja",prefs.getString("language","en"));
        assertTrue(allText(a.findViewById(android.R.id.content)).contains("言語"));
        a.onBackPressed();settle();focus("language");capture("settings-ja");
        assertTrue(allText(tag("homeButton")).contains("ホームボタン"));
        a.onBackPressed();settle();assertEquals("すべてのアプリ",((TextView)tag("allApps")).getText().toString());
        assertTrue(allText(tag("home:test.youtube")).contains("YouTube"));capture("list-ja");
        click("allApps");assertTrue(allText(tag("app:test.settings")).contains("Settings"));
        a.onBackPressed();settle();controller.recreate();a=controller.get();settle();
        assertEquals("すべてのアプリ",((TextView)tag("allApps")).getText().toString());
        click("settings");click("language");click("choice:en");a.onBackPressed();settle();
        assertTrue(allText(tag("homeButton")).contains("Home button"));
    }
    @Test public void japaneseDirectionsWarningAndPackageStatusAreLocalized() throws Exception {
        prefs.edit().putString("language","ja").commit();controller.recreate();a=controller.get();settle();
        click("settings");click("homeButton");capture("home-button-ja");click("accessibility");
        android.app.AlertDialog dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();
        assertTrue(((TextView)dialog.findViewById(android.R.id.message)).getText().toString().contains("設定 → その他の設定 → ユーザー補助"));
        dialog.dismiss();settle();click("googleHome");dialog=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();
        assertEquals("キャンセル",dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).getText().toString());
        assertTrue(((TextView)dialog.findViewById(android.R.id.message)).getText().toString().contains("割り当てをオフ"));
        dialog.dismiss();settle();a.onBackPressed();settle();click("privacy");click("packages");
        assertTrue(allText(tag("package:jp.co.sharp.av.android.analyticsservice")).contains("シャープ"));
        a.onBackPressed();settle();a.onBackPressed();settle();click("about");capture("about-ja");focus("aboutBack");
        a.onBackPressed();settle();click("background");click("wallpapers");capture("wallpapers-ja");
        assertTrue(allText(tag("wall:misty")).contains("霧の海岸"));
    }
    @Test public void upgradingExistingPreferencesPreservesHomeAndAppearance(){
        prefs.edit().putBoolean("design4",true).putBoolean("homeIntercept",true)
            .putString("font","Geist").putString("layout","dock").putString("wallpaper","cove")
            .putInt("shade4",30).putInt("textSize4",20).commit();
        controller.recreate();a=controller.get();settle();
        assertTrue(HomeRedirectController.requested(a));assertEquals("Geist",prefs.getString("font",""));
        assertEquals("dock",prefs.getString("layout",""));assertEquals("cove",prefs.getString("wallpaper",""));
        assertEquals(30,prefs.getInt("shade4",0));assertEquals(20,prefs.getInt("textSize4",0));
        assertNotNull(tag("home:test.netflix"));
        TextView name=(TextView)((LinearLayout)tag("home:test.netflix")).getChildAt(1);
        assertEquals(600,name.getTypeface().getWeight());
    }
}


