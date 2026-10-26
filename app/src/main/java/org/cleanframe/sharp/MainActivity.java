
package org.cleanframe.sharp;
import android.app.Activity;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import android.text.TextUtils;
import java.io.*;
import java.util.*;

/** Offline native TV interface. All package guidance is display-only. */
public class MainActivity extends Activity {
    private static final String GRADIENT_REFERENCE="Settings";
    private android.content.res.Resources words;
    private static final int INK=0xfff4f3ee, MUTED=0xffb9c2c5, PICK_PHOTO=1234;
    private SharedPreferences prefs;
    private FrameLayout root,preview;
    private LinearLayout body;
    private View first;
    private String page="home", focusedHome="",restoreTag="";
    private Runnable optionBack;
    private Typeface font,uiFont;
    private final Map<String,Typeface> fonts=new HashMap<>();
    private Bitmap cachedPhoto;
    private String cachedPhotoKey="";
    private final List<Photo> photos=new ArrayList<>();
    private final List<View> mainItems=new ArrayList<>();
    private boolean editingHome=false;
    private static final String[][] PACKAGES={
        {"Android TV Home", "com.google.android.tvlauncher", "KEEP: original launcher and recovery route"},
        {"Sharp Analytics", "jp.co.sharp.av.android.analyticsservice", "Proposed disable; Sharp analytics/provider dependencies uncertain"},
        {"COCORO VISION", "jp.co.sharp.av.android.cocorovision", "Proposed disable; recommendations and Sharp integrations may be affected"},
        {"Android TV Core Services", "com.google.android.tvrecommendations", "Optional trial; notifications, stock Home and other integrations may be affected"},
        {"Google Feedback", "com.google.android.feedback", "Proposed disable; feedback functions may be affected"},
        {"TV Bug Report Sender", "com.google.android.tv.bugreportsender", "Proposed disable; diagnostics reporting may be affected"},
        {"Google Assistant", "com.google.android.katniss", "KEEP: voice button and Assistant"},
        {"Google Play Services", "com.google.android.gms", "KEEP: app authentication and services"},
        {"Google Play Store", "com.android.vending", "KEEP: app installs and updates"},
        {"Sharp Recording Core", "jp.co.sharp.av.android.rsvcore", "KEEP: recordings and reservations"},
        {"Sharp EPG Linker", "jp.co.sharp.av.android.epglinker", "KEEP: program guide"},
        {"Sharp Hybridcast", "jp.co.sharp.av.android.hybridcastcpdm", "KEEP: Japanese broadcast functions"}, {"Sharp Monitor Maintainer", "jp.co.sharp.av.android.monitormaintainer", "KEEP: TV system component"}, {"Sharp TV Launcher", "jp.co.sharp.av.android.tvlauncher", "KEEP: TV system component"}
    };
    private static class Photo {
        String id,name,credit,source;
        Photo(org.json.JSONObject o) throws org.json.JSONException {
            id=o.getString("id");name=o.getString("name");credit=o.getString("credit");source=o.getString("source");
        }
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
            |View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        prefs=getSharedPreferences("favorites",MODE_PRIVATE);uiFont=loadFont("Manrope");
        try(InputStream in=getAssets().open("wallpapers/SOURCES.json")) {
            ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[4096];int n;
            while((n=in.read(b))!=-1)out.write(b,0,n);
            org.json.JSONArray a=new org.json.JSONArray(out.toString("UTF-8"));
            for(int i=0;i<a.length();i++)photos.add(new Photo(a.getJSONObject(i)));
        }catch(Exception e){Toast.makeText(this,tr("Wallpaper catalogue unavailable"),Toast.LENGTH_LONG).show();}
        if(!prefs.getBoolean("design4",false)) {
            SharedPreferences.Editor edit=prefs.edit().putBoolean("design4",true);
            if(!"photo".equals(prefs.getString("background","")))edit.putString("background","wallpaper");
            edit.apply();
        }
        font=loadFont(prefs.getString("font","Manrope"));showHome();
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);showHome();}

    @Override protected void onResume(){
        super.onResume();
        if("homeButton".equals(page)&&prefs!=null)showHomeButton();
        if(root!=null)root.post(this::ensureFocus);
    }
    @Override public void onWindowFocusChanged(boolean focused){super.onWindowFocusChanged(focused);if(focused&&root!=null)root.post(this::ensureFocus);}
    private Typeface loadFont(String name) {
        if(fonts.containsKey(name))return fonts.get(name);
        Typeface result;
        try{result=Typeface.createFromAsset(getAssets(),"fonts/"+name+".ttf");}
        catch(Exception e){result=Typeface.create("sans-serif",Typeface.NORMAL);}
        result=Typeface.create(result,500,false);fonts.put(name,result);return result;
    }
    private String tr(String source){
        if(words==null){
            android.content.res.Configuration config=new android.content.res.Configuration(getResources().getConfiguration());
            config.setLocale(new Locale(prefs.getString("language","en")));
            words=createConfigurationContext(config).getResources();
        }
        return UiText.get(words,source);
    }
    private int dp(float value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private int homeTextWeight(){
        String value=prefs.getString("homeTextWeight","600");
        return "300".equals(value)?300:"500".equals(value)?500:600;
    }
    private String homeTextWeightName(){
        switch(homeTextWeight()){case 300:return tr("Light");case 500:return tr("Original");default:return tr("Semibold");}
    }
    private TextView label(String value,float size,int color,boolean home) {
        TextView t=new TextView(this);t.setText(value);t.setTextSize(size*1.2f);t.setTextColor(color);
        
        int weight=home?homeTextWeight():500;
        t.setTypeface(Typeface.create(home?font:uiFont,weight,false));
        t.setFontVariationSettings("'wght' "+weight);
        if(home)t.setShadowLayer(dp(2.5f),0,dp(1.25f),0xdd000000);
        t.setIncludeFontPadding(false);return t;
    }
    private GradientDrawable plate(boolean focused,boolean glass) {
        
        if(glass&&!focused){
            GradientDrawable tray=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xb5223038,0xd014222b});
            tray.setCornerRadius(dp(20));tray.setStroke(dp(.6f),0x28ffffff);return tray;
        }
        int a=focused?prefs.getInt("focus4",20)*255/100:0;
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{(a<<24)|0x00e5e9e6,((int)(a*.67f)<<24)|0x00c5d2d1});
        d.setCornerRadius(dp(glass?20:10));
        if(focused||glass)d.setStroke(dp(.6f),focused?0x45ffffff:0x20ffffff);return d;
    }
    private void wire(View v,String tag,Runnable action,boolean glass) {
        final boolean homeScene=page.equals("home");
        v.setId(View.generateViewId());v.setTag(tag);v.setFocusable(true);v.setFocusableInTouchMode(true);
        if(v instanceof ViewGroup)((ViewGroup)v).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        v.setClickable(action!=null);v.setBackground(plate(false,glass));
        if(action!=null)v.setOnClickListener(w->action.run());
        v.setOnFocusChangeListener((w,f)->{
            w.setBackground(plate(f,glass));w.setAlpha(homeScene?1f:f?1f:.9f);
            if(f)w.post(()->w.requestRectangleOnScreen(new Rect(0,0,w.getWidth(),w.getHeight()),true));
        });
        v.setAlpha(homeScene?1f:.9f);if(first==null)first=v;
    }
    private void ensureFocus(){if(root==null)return;View v=root.findFocus();if(v!=null&&v.isFocusable())return;if(first!=null)first.requestFocus();}
    private void finishPage(String tag) {
        View initial=tag==null?first:root.findViewWithTag(tag);if(initial!=null)initial.requestFocus();
        root.post(()->{View v=tag==null?null:root.findViewWithTag(tag);
            if(v!=null&&v.isFocusable())v.requestFocus();else if(first!=null)first.requestFocus();});
    }
    private Bitmap photoBitmap(String mode,String id) {
        String key=mode.equals("photo")?"user:"+prefs.getString("wallpaperUri",""):mode+":"+id;
        if(key.equals(cachedPhotoKey)&&cachedPhoto!=null&&!cachedPhoto.isRecycled())return cachedPhoto;
        Bitmap b=null;
        try{
            if(mode.equals("photo"))b=decodePhoto(Uri.parse(prefs.getString("wallpaperUri","")));
            else if(mode.equals("wallpaper"))try(InputStream in=getAssets().open("wallpapers/"+id+".jpg")){b=BitmapFactory.decodeStream(in);}
        }catch(Exception ignored){}
        cachedPhotoKey=key;cachedPhoto=b;return b;
    }
    private void backdrop(FrameLayout target,String candidate,boolean panel) {
        String mode=candidate==null?prefs.getString("background","wallpaper"):"wallpaper";
        String id=candidate==null?prefs.getString("wallpaper","misty"):candidate;
        int top=mode.equals("dusk")?0xff35445d:mode.equals("slate")?0xff364d55:0xff16232c;
        target.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{top,0xff080e15}));
        Bitmap b=photoBitmap(mode,id);
        if(b!=null){ImageView image=new ImageView(this);image.setImageBitmap(b);image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            target.addView(image,new FrameLayout.LayoutParams(-1,-1));}
        View shade=new View(this);int dim=prefs.getInt("shade4",18)*255/100;
        shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{((Math.min(255,dim+40))<<24)|0x00080c10,(dim<<24)|0x00080c10}));
        target.addView(shade,new FrameLayout.LayoutParams(-1,-1));
        if(panel){View scrim=new View(this);scrim.setBackgroundColor(0xd00b1219);target.addView(scrim,new FrameLayout.LayoutParams(-1,-1));}
    }
    private void newRoot(boolean panel) {
        first=null;preview=null;root=new FrameLayout(this);root.setFocusable(false);
        root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);backdrop(root,null,panel);setContentView(root);
    }
    private List<ResolveInfo> apps() {
        PackageManager pm=getPackageManager();List<ResolveInfo> result=new ArrayList<>();Set<String> seen=new HashSet<>();
        for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}) {
            Intent intent=new Intent(Intent.ACTION_MAIN).addCategory(category);
            for(ResolveInfo r:pm.queryIntentActivities(intent,0)){
                String pkg=r.activityInfo.packageName;if(!pkg.equals(getPackageName())&&seen.add(pkg))result.add(r);
            }
        }
        result.sort((a,b)->String.valueOf(a.loadLabel(pm)).compareToIgnoreCase(String.valueOf(b.loadLabel(pm))));return result;
    }
    private void launch(String pkg) {
        try{Intent i=getPackageManager().getLeanbackLaunchIntentForPackage(pkg);if(i==null)i=getPackageManager().getLaunchIntentForPackage(pkg);
            if(i==null)throw new ActivityNotFoundException();startActivity(i);
        }catch(Exception e){Toast.makeText(this,tr("This app could not be opened"),Toast.LENGTH_LONG).show();}
    }
    private List<ResolveInfo> homeApps() {
        Map<String,ResolveInfo> available=new HashMap<>();for(ResolveInfo r:apps())available.put(r.activityInfo.packageName,r);
        List<ResolveInfo> ordered=new ArrayList<>();for(String pkg:favorites())if(available.containsKey(pkg))ordered.add(available.get(pkg));return ordered;
    }
    private FrameLayout.LayoutParams frame(int w,int h,int gravity,float left,float top,float right,float bottom) {
        FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h),gravity);
        p.setMargins(dp(left),dp(top),dp(right),dp(bottom));return p;
    }
    private void showHome(){page="home";mainItems.clear();newRoot(false);renderHome(root,false);finishPage(focusedHome.isEmpty()?null:"home:"+focusedHome);}
    private void renderHome(FrameLayout scene,boolean miniature) {
        float scale=miniature?.44f:1f;
        boolean dock="dock".equals(prefs.getString("layout","list"));
        ReadabilityScrim scrim=new ReadabilityScrim(this,!dock,scale);
        scene.addView(scrim,new FrameLayout.LayoutParams(-1,-1));
        TextClock clock=new TextClock(this);clock.setFormat12Hour("h:mm");clock.setFormat24Hour("HH:mm");
        clock.setTextColor(INK);clock.setTextSize(23*scale*1.2f);clock.setTypeface(Typeface.create(font,homeTextWeight(),false));clock.setFontVariationSettings("'wght' "+homeTextWeight());clock.setIncludeFontPadding(false);
        clock.setShadowLayer(dp(2.5f*scale),0,dp(1.25f*scale),0xdd000000);clock.setFocusable(false);
        scene.addView(clock,frame(-2,-2,Gravity.TOP|Gravity.END,0,34*scale,60*scale,0));
        List<ResolveInfo> chosen=homeApps();LinearLayout list=new LinearLayout(this);
        list.setOrientation(dock?LinearLayout.HORIZONTAL:LinearLayout.VERTICAL);
        if(dock){
            HorizontalScrollView scroll=new HorizontalScrollView(this);scroll.setHorizontalScrollBarEnabled(false);scroll.setFocusable(false);
            scroll.setClipToPadding(false);scroll.setPadding(dp(8*scale),dp(8*scale),dp(8*scale),dp(8*scale));
            scroll.setBackground(plate(false,true));scroll.addView(list);
            scene.addView(scroll,frame(-1,Math.round(140*scale),Gravity.BOTTOM,52*scale,0,52*scale,96*scale));
        }else{
            ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);scroll.setFocusable(false);scroll.setClipToPadding(false);scroll.addView(list);
            scene.addView(scroll,frame(Math.round(320*scale),-1,Gravity.TOP|Gravity.START,52*scale,64*scale,0,108*scale));
        }
        View previous=null;
        for(ResolveInfo app:chosen){
            String pkg=app.activityInfo.packageName,name=String.valueOf(app.loadLabel(getPackageManager()));
            LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(dp(14*scale),dp(9*scale),dp(14*scale),dp(9*scale));item.setGravity(dock?Gravity.CENTER:Gravity.CENTER_VERTICAL);
            if(dock){ImageView icon=new ImageView(this);icon.setImageDrawable(app.loadIcon(getPackageManager()));icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                item.addView(icon,new LinearLayout.LayoutParams(dp(42*scale),dp(42*scale)));}
            TextView text=label(name,(dock?14:prefs.getInt("textSize4",18))*scale,INK,true);
            text.setSingleLine(true);text.setEllipsize(TextUtils.TruncateAt.END);text.setShadowLayer(dp(2.5f*scale),0,dp(1.25f*scale),0xdd000000);
            if(dock){text.setGravity(Gravity.CENTER);text.setPadding(0,dp(9*scale),0,0);}item.addView(text,new LinearLayout.LayoutParams(dock?-1:-2,-2));
            LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dock?dp(116*scale):-2,dp((dock?120:50)*scale));
            if(dock)ip.rightMargin=dp(5*scale);else ip.bottomMargin=dp(3*scale);list.addView(item,ip);item.setContentDescription(name);
            if(!miniature){
                wire(item,"home:"+pkg,()->{focusedHome=pkg;launch(pkg);},false);item.setAlpha(1f);
                item.setOnFocusChangeListener((v,f)->{v.setBackground(plate(f,false));v.setAlpha(1f);
                    if(f){focusedHome=pkg;v.post(()->v.requestRectangleOnScreen(new Rect(0,0,v.getWidth(),v.getHeight()),true));}});
                mainItems.add(item);
                if(dock){item.setNextFocusLeftId(item.getId());item.setNextFocusRightId(item.getId());}else item.setNextFocusUpId(item.getId());
                if(previous!=null){if(dock){previous.setNextFocusRightId(item.getId());item.setNextFocusLeftId(previous.getId());}
                    else{previous.setNextFocusDownId(item.getId());item.setNextFocusUpId(previous.getId());}}previous=item;
            }
        }
        if(chosen.isEmpty()){TextView empty=label(tr("Add your apps from All Apps"),14*scale,MUTED,true);empty.setPadding(dp(14*scale),dp(9*scale),0,0);list.addView(empty);}
        LinearLayout utilities=new LinearLayout(this);utilities.setOrientation(LinearLayout.HORIZONTAL);
        TextView all=utility(tr("All Apps"),scale),settings=utility(tr("Settings"),scale);utilities.addView(all);
        LinearLayout.LayoutParams up=new LinearLayout.LayoutParams(-2,-2);up.leftMargin=dp(16*scale);utilities.addView(settings,up);
        scene.addView(utilities,frame(-2,-2,Gravity.BOTTOM|Gravity.START,52*scale,0,0,32*scale));
        utilities.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{
            float textWidth=settings.getPaint().measureText(settings.getText().toString());
            float anchor=settings.getPaint().measureText("Setti")/settings.getPaint().measureText(GRADIENT_REFERENCE);
            float start=l+settings.getLeft()+settings.getPaddingLeft()+textWidth*anchor;
            scrim.setListShadeBounds(start,r+dp(24*scale));
        });
        if(!miniature){
            wire(all,"allApps",this::showApps,false);wire(settings,"settings",this::showSettings,false);
            all.setNextFocusLeftId(all.getId());all.setNextFocusRightId(settings.getId());settings.setNextFocusLeftId(all.getId());settings.setNextFocusRightId(settings.getId());
            all.setNextFocusDownId(all.getId());settings.setNextFocusDownId(settings.getId());
            if(!mainItems.isEmpty()){View last=mainItems.get(mainItems.size()-1);
                if(dock)for(View v:mainItems)v.setNextFocusDownId(all.getId());else last.setNextFocusDownId(all.getId());
                all.setNextFocusUpId(last.getId());settings.setNextFocusUpId(last.getId());
            }else{all.setNextFocusUpId(all.getId());settings.setNextFocusUpId(settings.getId());}
        }
    }
    private TextView utility(String title,float scale){
        TextView t=label(title,13*scale,INK,true);t.setPadding(dp(14*scale),dp(9*scale),dp(14*scale),dp(9*scale));t.setContentDescription(title);return t;
    }

    /** Grouped menu: a quiet left-hand column and committed Home preview on the right. */
    private void screen(String title,String subtitle,boolean withPreview) {
        newRoot(true);LinearLayout column=new LinearLayout(this);column.setOrientation(LinearLayout.VERTICAL);
        root.addView(column,frame(380,-1,Gravity.TOP|Gravity.START,56,38,0,42));
        column.addView(label(title,24,INK,false));
        if(subtitle!=null){TextView sub=label(subtitle,12,MUTED,false);sub.setPadding(0,dp(9),dp(10),dp(22));column.addView(sub);}
        ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);scroll.setFocusable(false);
        column.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(0,dp(2),dp(8),dp(10));scroll.addView(body);
        root.addView(label(tr("Back  ·  ")+backLabel(),11,MUTED,false),frame(-2,-2,Gravity.BOTTOM|Gravity.START,68,0,0,22));
        LinearLayout context=new LinearLayout(this);context.setOrientation(LinearLayout.VERTICAL);
        root.addView(context,frame(-1,-1,Gravity.TOP|Gravity.START,480,104,56,76));
        if(withPreview){
            preview=new FrameLayout(this);preview.setClipToOutline(true);preview.setOutlineProvider(new ViewOutlineProvider(){@Override public void getOutline(View v,Outline o){o.setRoundRect(0,0,v.getWidth(),v.getHeight(),dp(12));}});preview.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);preview.setBackground(plate(false,true));
            context.addView(preview,new LinearLayout.LayoutParams(-1,dp(236)));updatePreview(null);
            TextView caption=label(tr("Home preview"),11,MUTED,false);caption.setPadding(0,dp(14),0,0);context.addView(caption);
        }else{
            context.addView(label("CleanFrame Sharp",17,INK,false));
            TextView description=label(contextText(),13,MUTED,false);description.setPadding(0,dp(14),0,0);description.setLineSpacing(dp(4),1);context.addView(description);
        }
    }
    private String contextText(){
        if(page.equals("homeButton"))return tr("Direct Home-button handling.\n\nFirst enable CleanFrame Home button in TV Accessibility, then turn on Home interception here.\n\nHome opens CleanFrame once on release. Held Home uses the same action. Google stays enabled; Open Google launcher pauses interception.");
        if(page.equals("packages"))return tr("Read-only package status.\n\nNo commands run from this app. Refresh reads Android again.\n\nKEEP packages must remain unchanged.");
        if(page.equals("apps")||page.equals("appActions"))return tr("Your apps, your order.\n\nChoose an app to open it or add it to Home. Editing this menu does not install or remove an app.");
        return tr("A photograph. Your apps. Room to breathe.\n\nAll preferences stay on this TV. No advertising, analytics or network access.");
    }
    private String backLabel(){return page.equals("options")?tr("previous menu"):page.equals("settings")||page.equals("apps")?tr("Home"):tr("previous screen");}
    private void updatePreview(String photo){if(preview==null)return;preview.removeAllViews();backdrop(preview,photo,false);renderHome(preview,true);}
    private View row(String title,String detail,String tag,Runnable action){
        LinearLayout item=new LinearLayout(this);item.setOrientation(LinearLayout.VERTICAL);item.setPadding(dp(12),dp(10),dp(12),dp(10));item.setMinimumHeight(dp(44));
        item.addView(label(title,16,INK,false));
        if(detail!=null&&!detail.isEmpty()){TextView sub=label(detail,11,MUTED,false);sub.setPadding(0,dp(5),0,0);sub.setLineSpacing(dp(2),1);item.addView(sub);}
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(3);body.addView(item,p);
        item.setContentDescription(title+(detail==null?"":". "+detail));wire(item,tag,action,false);return item;
    }
    private void note(String value){TextView t=label(value,12,MUTED,false);t.setPadding(dp(12),dp(8),dp(12),dp(15));t.setLineSpacing(dp(3),1);body.addView(t);}
    private void section(String title,boolean firstSection){
        TextView heading=label(title,10,MUTED,false);
        heading.setLetterSpacing(.10f);heading.setPadding(dp(12),dp(firstSection?2:20),dp(12),dp(8));body.addView(heading);
    }
    private void choiceRow(String title,boolean selected,String tag,Runnable action){
        LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(12),dp(11),dp(12),dp(11));item.setMinimumHeight(dp(46));
        item.addView(label(title,16,INK,false),new LinearLayout.LayoutParams(0,-2,1));
        TextView mark=label(selected?"✓":"",15,INK,false);mark.setGravity(Gravity.END);
        item.addView(mark,new LinearLayout.LayoutParams(dp(28),-2));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.bottomMargin=dp(3);body.addView(item,lp);
        item.setSelected(selected);item.setContentDescription(title+(selected?tr(", selected"):""));wire(item,tag,action,false);
    }
    private String appVersion(){
        try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}
        catch(PackageManager.NameNotFoundException e){return tr("Version unavailable");}
    }
    private void aboutText(String title,String detail,String tag){
        LinearLayout block=new LinearLayout(this);block.setOrientation(LinearLayout.VERTICAL);
        block.setPadding(dp(12),dp(10),dp(12),dp(12));block.setTag(tag);
        block.addView(label(title,16,INK,false));
        TextView text=label(detail,12,MUTED,false);text.setPadding(0,dp(7),0,0);text.setLineSpacing(dp(3),1);block.addView(text);
        body.addView(block,new LinearLayout.LayoutParams(-1,-2));
    }
    private void showAbout(){
        page="about";guide(tr("About CleanFrame Sharp"));
        View back=row(tr("Back"),null,"aboutBack",this::onBackPressed);
        back.setNextFocusDownId(back.getId());back.setNextFocusUpId(back.getId());
        aboutText(tr("Version"),appVersion(),"version");
        aboutText(tr("Made for your TV"),tr("An offline native Android launcher inspired by Nathan Clark’s CleanFrame. List and Dock layouts, your apps and your photographs."),"aboutDesign");
        aboutText(tr("Privacy"),tr("No advertising, analytics or network permission. Preferences remain on this TV. Home interception is optional."),"aboutPrivacy");
        aboutText("CleanFrame Sharp",tr("A photograph. Your apps. Room to breathe.\n\nAll preferences stay on this TV. No advertising, analytics or network access."),"aboutBlurb");
        finishPage("aboutBack");
    }
    private void showSettings(){
        page="settings";screen(tr("Settings"),tr("Make this space your own."),true);
        section(tr("APPEARANCE"),true);
        row(tr("Layout"),capital(prefs.getString("layout","list")),"layout",this::showLayout);
        row(tr("Background"),backgroundName(),"background",this::showBackground);
        row(tr("Typography"),prefs.getString("font","Manrope")+" · "+homeTextWeightName(),"typography",this::showTypography);
        row(tr("Language / 言語"),prefs.getString("language","en").equals("ja")?"日本語":"English","language",this::showLanguage);
        row(tr("Focus & contrast"),null,"focus",this::showFocus);
        section(tr("HOME"),false);
        row(tr("Apps on Home"),favorites().size()+tr(" selected"),"homeApps",this::showHomeEditor);

        row(tr("Home button"),tr(HomeRedirectController.status(this)),"homeButton",this::showHomeButton);
        
        section(tr("SYSTEM"),false);
        row(tr("Privacy & system"),null,"privacy",this::showSystem);
        row(tr("About"),"CleanFrame Sharp · "+appVersion(),"about",this::showAbout);finishPage(restoreTag);restoreTag="";
    }
    private void showLanguage(){choose(tr("Language / 言語"),"language",new String[]{"English","日本語"},new String[]{"en","ja"},"en",()->{restoreTag="language";showSettings();});}
    private void showLayout(){choose(tr("Layout"),"layout",new String[]{tr("List"),tr("Dock")},new String[]{"list","dock"},"list",()->{restoreTag="layout";showSettings();});}
    private void showBackground(){
        page="background";screen(tr("Background"),tr("Choose a photograph or a quiet gradient."),true);
        row(tr("Wallpapers"),wallpaperName(),"wallpapers",this::showWallpapers);
        row(tr("Gradients"),null,"gradients",()->choose(tr("Gradients"),"background",new String[]{tr("Night"),tr("Dusk"),tr("Slate")},new String[]{"night","dusk","slate"},"wallpaper",this::showBackground));
        row(tr("Your own photo"),null,"photo",()->{
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE);
            try{startActivityForResult(i,PICK_PHOTO);}catch(Exception e){Toast.makeText(this,tr("No TV photo picker is available"),Toast.LENGTH_LONG).show();}
        });
        row(tr("Dimming"),prefs.getInt("shade4",18)+"%","dimming",this::showDimming);
        row(tr("Photo credits & licenses"),null,"credits",this::showCredits);finishPage(null);
    }
    private void showTypography(){
        page="typography";screen(tr("Typography"),tr("Choose the character of your Home screen."),true);
        row(tr("Typeface"),prefs.getString("font","Manrope"),"font",()->choose(tr("Typeface"),"font",new String[]{"Manrope","Geist","Newsreader"},new String[]{"Manrope","Geist","Newsreader"},"Manrope",this::showTypography));
        row(tr("Text weight"),homeTextWeightName(),"textWeight",()->choose(tr("Text weight"),"homeTextWeight",new String[]{tr("Light"),tr("Original"),tr("Semibold")},new String[]{"300","500","600"},"600",this::showTypography));
        row(tr("App text size"),textSizeName(),"textsize",()->choose(tr("App text size"),"textSize4",new String[]{tr("Small"),tr("Standard"),tr("Medium"),tr("Large")},new String[]{"16","18","20","22"},"18",this::showTypography));finishPage(null);
    }
    private void showFocus(){
        page="focus";screen(tr("Focus & contrast"),tr("Subtle focus. Comfortable contrast."),true);
        row(tr("Highlight opacity"),prefs.getInt("focus4",20)+"%","opacity",()->choose(tr("Highlight opacity"),"focus4",new String[]{tr("Subtle · 12%"),tr("Soft · 20%"),tr("Medium · 30%"),tr("Strong · 45%")},new String[]{"12","20","30","45"},"20",this::showFocus));
        row(tr("Background dimming"),prefs.getInt("shade4",18)+"%","dimming",this::showDimming);finishPage(null);
    }
    private void showDimming(){
        Runnable parent=page.equals("focus")?this::showFocus:this::showBackground;
        choose(tr("Background dimming"),"shade4",new String[]{tr("None · 0%"),tr("Light · 10%"),tr("Balanced · 18%"),tr("Dim · 30%"),tr("Dark · 45%"),tr("Very dark · 60%")},new String[]{"0","10","18","30","45","60"},"18",parent);
    }
    private String textSizeName(){switch(prefs.getInt("textSize4",18)){case 16:return tr("Small");case 20:return tr("Medium");case 22:return tr("Large");default:return tr("Standard");}}
    private boolean numeric(String key){return key.equals("textSize4")||key.equals("focus4")||key.equals("shade4");}
    private void choose(String title,String key,String[] labels,String[] values,String fallback,Runnable parent){
        if(!"options".equals(page)){
            View origin=root==null?null:root.findFocus();
            final String returnTag=origin!=null&&origin.getTag() instanceof String?(String)origin.getTag():null;
            optionBack=()->{parent.run();finishPage(returnTag);};
        }
        page="options";screen(title,tr("Browse with Up / Down. Press OK to apply."),true);
        String selected=numeric(key)?String.valueOf(prefs.getInt(key,Integer.parseInt(fallback))):prefs.getString(key,fallback);
        for(int i=0;i<values.length;i++){
            final String value=values[i];
            choiceRow(labels[i],value.equals(selected),"choice:"+value,()->{
                SharedPreferences.Editor edit=prefs.edit();
                if(numeric(key))edit.putInt(key,Integer.parseInt(value));else edit.putString(key,value);
                if(!edit.commit()){Toast.makeText(this,tr("Could not save this choice"),Toast.LENGTH_LONG).show();return;}
                if(key.equals("font"))font=loadFont(value);
                if(key.equals("language")){words=null;showLanguage();return;}
                choose(title,key,labels,values,fallback,parent);
            });
        }
        finishPage("choice:"+selected);
    }
    private void showWallpapers(){
        page="wallpapers";screen(tr("Wallpapers"),tr("Browse to preview. Press OK to keep."),true);
        String selected=prefs.getString("wallpaper","misty");
        for(Photo photo:photos){
            LinearLayout item=new LinearLayout(this);item.setGravity(Gravity.CENTER_VERTICAL);item.setPadding(dp(10),dp(6),dp(10),dp(6));
            ImageView thumb=new ImageView(this);thumb.setScaleType(ImageView.ScaleType.CENTER_CROP);
            try(InputStream in=getAssets().open("wallpapers/"+photo.id+"-thumb.jpg")){thumb.setImageDrawable(Drawable.createFromStream(in,null));}catch(Exception ignored){}
            item.addView(thumb,new LinearLayout.LayoutParams(dp(68),dp(40)));
            LinearLayout names=new LinearLayout(this);names.setOrientation(LinearLayout.VERTICAL);names.setPadding(dp(12),0,0,0);
            names.addView(label((selected.equals(photo.id)&&prefs.getString("background","wallpaper").equals("wallpaper")?"✓  ":"")+tr(photo.name),14,INK,false));
            names.addView(label(photo.credit,10,MUTED,false));item.addView(names);
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(58));lp.bottomMargin=dp(3);body.addView(item,lp);
            wire(item,"wall:"+photo.id,()->{
                if(photoBitmap("wallpaper",photo.id)==null){Toast.makeText(this,tr("Photo could not be loaded"),Toast.LENGTH_LONG).show();return;}
                if(prefs.edit().putString("background","wallpaper").putString("wallpaper",photo.id).commit())showWallpapers();
                else Toast.makeText(this,tr("Photo could not be saved"),Toast.LENGTH_LONG).show();
            },false);
            item.setContentDescription(tr(photo.name)+tr(", photo by ")+photo.credit);
            item.setOnFocusChangeListener((v,f)->{v.setBackground(plate(f,false));v.setAlpha(f?1f:.8f);
                if(f){updatePreview(photo.id);v.post(()->v.requestRectangleOnScreen(new Rect(0,0,v.getWidth(),v.getHeight()),true));}});
        }
        finishPage("wall:"+selected);
    }
    private String wallpaperName(){String id=prefs.getString("wallpaper","misty");for(Photo p:photos)if(p.id.equals(id))return tr(p.name);return tr("Misty coast");}
    private String backgroundName(){String mode=prefs.getString("background","wallpaper");return mode.equals("wallpaper")?wallpaperName():mode.equals("photo")?tr("Your photo"):capital(mode);}
    private String capital(String s){return tr(s.isEmpty()?s:s.substring(0,1).toUpperCase(Locale.ROOT)+s.substring(1));}


    private void showApps(){
        page="apps";guide(tr("All Apps"));note(tr("Select an app to open it or edit your Home menu."));
        List<ResolveInfo> catalogue=apps();List<View> tiles=new ArrayList<>();LinearLayout line=null;
        if(catalogue.isEmpty())note(tr("No launchable apps are visible."));
        int total=((catalogue.size()+4)/5)*5;
        for(int i=0;i<total;i++){
            if(i%5==0){line=new LinearLayout(this);line.setOrientation(LinearLayout.HORIZONTAL);body.addView(line,new LinearLayout.LayoutParams(-1,-2));}
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(116),1);lp.setMargins(dp(4),dp(4),dp(4),dp(4));
            if(i>=catalogue.size()){line.addView(new View(this),lp);continue;}
            ResolveInfo app=catalogue.get(i);String pkg=app.activityInfo.packageName,name=String.valueOf(app.loadLabel(getPackageManager()));
            LinearLayout tile=new LinearLayout(this);tile.setOrientation(LinearLayout.VERTICAL);tile.setGravity(Gravity.CENTER);tile.setPadding(dp(10),dp(10),dp(10),dp(10));
            ImageView icon=new ImageView(this);icon.setImageDrawable(app.loadIcon(getPackageManager()));icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            tile.addView(icon,new LinearLayout.LayoutParams(dp(42),dp(42)));
            TextView title=label(name,14,INK,false);title.setSingleLine(true);title.setEllipsize(TextUtils.TruncateAt.END);title.setGravity(Gravity.CENTER);title.setPadding(0,dp(10),0,0);
            tile.addView(title,new LinearLayout.LayoutParams(-1,-2));
            TextView state=label(favorites().contains(pkg)?tr("On Home"):"",10,MUTED,false);state.setPadding(0,dp(4),0,0);tile.addView(state);
            line.addView(tile,lp);tile.setContentDescription(name+(favorites().contains(pkg)?tr(", on Home"):""));
            wire(tile,"app:"+pkg,()->showAppActions(name,pkg,false),false);tiles.add(tile);
        }
        for(int i=0;i<tiles.size();i++){
            View v=tiles.get(i);v.setNextFocusLeftId(tiles.get(i%5==0?i:i-1).getId());
            v.setNextFocusRightId(tiles.get(i%5==4||i==tiles.size()-1?i:i+1).getId());
            v.setNextFocusUpId(tiles.get(i<5?i:i-5).getId());
            v.setNextFocusDownId(tiles.get(i+5<tiles.size()?i+5:i).getId());
        }
        finishPage(null);
    }
    private void showHomeEditor(){
        page="homeEditor";screen(tr("Apps on Home"),tr("Choose an app to arrange or remove it."),false);
        for(ResolveInfo app:homeApps()){String pkg=app.activityInfo.packageName,name=String.valueOf(app.loadLabel(getPackageManager()));
            row(name,null,"edit:"+pkg,()->showAppActions(name,pkg,true));}
        row(tr("Add apps"),null,"addApps",this::showApps);finishPage(null);
    }
    private void showAppActions(String name,String pkg,boolean editor){
        editingHome=editor;page="appActions";screen(name,tr("App options"),false);
        row(tr("Open app"),null,"open",()->launch(pkg));boolean onHome=favorites().contains(pkg);
        row(onHome?tr("Remove from Home"):tr("Add to Home"),null,"membership",()->{
            List<String> list=favorites();if(onHome)list.remove(pkg);else list.add(pkg);saveFavorites(list);showAppActions(name,pkg,editor);
        });
        if(onHome){row(tr("Move up"),null,"up",()->moveFavorite(name,pkg,-1,editor));row(tr("Move down"),null,"down",()->moveFavorite(name,pkg,1,editor));}finishPage(null);
    }
    private void showSystem(){
        page="system";guide(tr("Privacy & system"));
        note(tr("Your TV remains in your control."));
        row(tr("System package status"),tr("Read-only · refresh on demand"),"packages",this::showPackages);
        row(tr("Android TV settings"),tr("Open the TV's own Settings"),"androidSettings",()->{
            try{startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));}
            catch(Exception e){Toast.makeText(this,tr("TV Settings unavailable"),Toast.LENGTH_LONG).show();}
        });
        note(tr("CleanFrame never changes your default Home, enables/disables packages or starts ADB."));finishPage(null);
    }

    private GradientDrawable warningPlate(boolean focused){
        GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            new int[]{focused?0x66e8bd45:0x33e8bd45,focused?0x44e8bd45:0x22e8bd45});
        d.setCornerRadius(dp(10));d.setStroke(dp(1),focused?0xfff6d66a:0x99e8bd45);return d;
    }
    private void warningRow(String title,String detail,String tag,Runnable action){
        View button=row(title,detail,tag,action);button.setAlpha(1f);button.setBackground(warningPlate(button.hasFocus()));
        button.setOnFocusChangeListener((v,focused)->{v.setBackground(warningPlate(focused));
            if(focused)v.post(()->v.requestRectangleOnScreen(new Rect(0,0,v.getWidth(),v.getHeight()),true));});
    }
    private void confirmGoogleLauncher(){
        android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(this)
            .setTitle(tr("Open the original launcher?"))
            .setMessage(tr("You will be taken to the original Google launcher and Home interception will be turned off. Home will then open Google Home.\n\nTo make Home open CleanFrame Sharp again, open CleanFrame → Settings → Home button and turn on Home interception again."))
            .setPositiveButton(tr("OK"),(d,w)->openGoogleLauncher())
            .setNegativeButton(tr("Cancel"),(d,w)->{}).create();
        dialog.setOnShowListener(d->{android.widget.Button cancel=dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE);
            cancel.setFocusableInTouchMode(true);cancel.requestFocus();});
        dialog.setOnDismissListener(d->{View button=root.findViewWithTag("googleHome");if(button!=null)button.requestFocus();});
        dialog.show();
    }
    private void openGoogleLauncher(){
        try{HomeRedirectController.openGoogle(this);}
        catch(RuntimeException e){Toast.makeText(this,tr("Could not open Google Home. Check Home button settings."),Toast.LENGTH_LONG).show();}
    }
    private void showAccessibilityDirections(){
        android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(this)
            .setTitle(tr("Enable CleanFrame Home button"))
            .setMessage(tr("On Sharp AQUOS, open the TV menu:\n\nSettings (設定) → Other settings (その他の設定) → Accessibility (ユーザー補助).\n\nAt the bottom, under Services (サービス), turn CleanFrame Home button On.\n\nThen return to CleanFrame → Settings → Home button → Turn on Home interception.\n\nMenu names may differ on other TVs."))
            .setPositiveButton(tr("OK"),(d,w)->{}).create();
        dialog.setOnShowListener(d->{
            android.widget.Button ok=dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            ok.setFocusableInTouchMode(true);ok.requestFocus();
        });
        dialog.setOnDismissListener(d->{
            View directions=root.findViewWithTag("accessibility");
            if(directions!=null)directions.requestFocus();
        });
        dialog.show();
    }
    private void showHomeButton(){
        page="homeButton";screen(tr("Home button"),tr("Use CleanFrame when you press Home."),false);
        note(tr("Status: ")+tr(HomeRedirectController.status(this)));
        note(tr(HomeRedirectService.lastResult()));
        String error=HomeRedirectController.error(this);
        if(!error.isEmpty())note(tr(error));
        boolean requested=HomeRedirectController.requested(this);
        row(requested?tr("Pause Home interception"):tr("Turn on Home interception"),
            requested?tr("Home will open the original Google launcher."):tr("First enable CleanFrame Home button in TV Accessibility settings."),
            "redirectToggle",()->{
                if(!HomeRedirectController.setRequested(this,!requested)){
                    Toast.makeText(this,tr("Could not save Home button choice"),Toast.LENGTH_LONG).show();return;
                }
                showHomeButton();

            });
        row(tr("How to enable Accessibility"),tr("Show the TV menu steps."),"accessibility",this::showAccessibilityDirections);
        row(tr("Refresh status"),null,"refreshRedirect",this::showHomeButton);
        warningRow(tr("Open Google launcher"),tr("Turns off Home interception. You must turn it on again to use CleanFrame with Home."),"googleHome",this::confirmGoogleLauncher);
        row(tr("How it works & privacy"),tr("What this access does and how to stop it."),"redirectPrivacy",this::showHomePrivacy);
        finishPage(null);
    }


    private void showHomePrivacy(){
        page="homePrivacy";guide(tr("Home button & privacy"));
        row(tr("Direct Home interception"),tr("Consumes Home press and release before normal Android Home handling, then opens CleanFrame once. No window watching, delayed retry or Google-launcher disable is used. Compatibility depends on TV firmware; verify on your device."),"homeBehavior",null);
        row(tr("Access"),tr("Android delivers global key events to the service. Only Home (3) is handled. All other keys pass through immediately without being stored. No screen contents, text, window events, gestures or touch exploration are requested."),"accessScope",null);
        row(tr("Setup"),tr("First enable CleanFrame Home button in TV Accessibility, then turn on Home interception here. After an update, check the service is still enabled."),"consent",null);
        row(tr("Home behavior"),tr("Short and held Home presses both open CleanFrame once. The original long-press Home action is replaced while enabled. Voice, Back, volume, tuner and other keys are passed through. Screen-off and locked-device Home events follow the system path."),"homeKeys",null);
        row(tr("Open Google launcher"),tr("This Settings button pauses interception before opening Google Home. It stays paused until you turn it on again here."),"escapeHelp",null);
        row(tr("Turn it off"),tr("Pause Home interception here, or disable CleanFrame Home button in TV Accessibility. The original launcher remains enabled. Other Accessibility services are never changed by this app."),"disableHelp",null);
        row(tr("Privacy"),tr("No network permission, screen retrieval or key history. Only the enable choice and a failure message are saved locally. The current service session shows whether a Home launch was requested; this does not prove the TV kept CleanFrame in front."),"privacyHelp",null);
        finishPage(null);
    }
    private String status(String pkg){
        try{PackageManager pm=getPackageManager();ApplicationInfo info=pm.getApplicationInfo(pkg,PackageManager.MATCH_DISABLED_COMPONENTS);
            return tr(PackagePolicy.describe(pm.getApplicationEnabledSetting(pkg),info.enabled));}
        catch(PackageManager.NameNotFoundException e){return tr("UNKNOWN · missing or not visible");}
        catch(Exception e){return tr("UNKNOWN · status unavailable");}
    }
    private void showPackages(){
        page="packages";screen(tr("System packages"),tr("Read-only. Nothing here changes a package."),false);
        row(tr("Refresh"),tr("Read package states again"),"refresh",this::showPackages);
        for(String[] p:PACKAGES)row(tr(p[0]),status(p[1])+" · "+(PackagePolicy.isCandidate(p[1])?tr("Proposed candidate"):tr("KEEP / unchanged")),"package:"+p[1],()->showPackage(p));
        finishPage(null);
    }
    /** Wrapped guide sections are focusable so D-pad navigation scrolls through long commands. */
    private void guide(String title){
        newRoot(true);LinearLayout column=new LinearLayout(this);column.setOrientation(LinearLayout.VERTICAL);
        root.addView(column,frame(-1,-1,Gravity.TOP,56,32,56,36));
        TextView h=label(title,23,INK,false);h.setPadding(dp(12),0,0,dp(18));column.addView(h);
        ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);scroll.setFocusable(false);
        column.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);scroll.addView(body);
    }
    private void showPackage(String[] p){
        page="packageDetail";guide(tr(p[0]));
        row(status(p[1]),p[1],"status",null);row(tr("Policy"),tr(p[2])+tr(". KEEP packages must remain unchanged."),"policy",null);
        row(tr("1 · Connect"),tr("Enable debugging using your TV manufacturer’s instructions. PC and TV must share the LAN. Open PowerShell in the folder containing adb.exe. Replace the example address and port below with your TV’s own values."),"connectHelp",null);
        row(tr("Connect to TV"),".\\adb.exe connect 192.0.2.10:5555","connect",null);
        row(tr("2 · Verify"),tr(".\\adb.exe devices\nContinue only if it says device, not offline or unauthorized."),"devices",null);
        row(tr("3 · Check disabled packages"),".\\adb.exe shell pm list packages -d","check",null);
        if(PackagePolicy.isCandidate(p[1])){
            row(tr("Optional manual change; research your TV first"),".\\adb.exe shell pm disable-user --user 0 "+p[1],"disable",null);
            row(tr("Restore that candidate"),".\\adb.exe shell pm enable --user 0 "+p[1],"enable",null);
            row(tr("Re-check and test"),tr(".\\adb.exe shell pm list packages -d\nTest tuner, EPG, recording, HDMI, remote keys and streaming. If needed, restore the candidate and restart the TV. Never uninstall system packages or clear their data as recovery."),"recheck",null);
        }else row(tr("KEEP / do not modify"),tr("No disable or enable command is offered. Preserve this package's current state."),"keep",null);
        row(tr("Refresh status"),null,"refreshDetail",()->showPackage(p));finishPage(null);
    }
    private void showCredits(){
        page="credits";guide(tr("Photographs & type"));
        row("CleanFrame Sharp",tr("Unofficial native Android adaptation · MIT license. Project code and original CleanFrame retain separate copyright notices bundled in assets/licenses."),"projectLicense",null);
        row(tr("CleanFrame inspiration"),tr("Nathan Clark · MIT license\nhttps://github.com/nthnclrk/CleanFrame"),"reference",null);
        for(Photo p:photos)row(tr(p.name),tr("Photo by ")+p.credit+" / Unsplash\n"+p.source,"credit:"+p.id,null);
        row(tr("Photo license"),tr("Unsplash License · https://unsplash.com/license\nBundled for launcher backgrounds. Photographers retain their rights. No affiliation or endorsement is implied."),"photoLicense",null);
        row(tr("Typefaces"),tr("Manrope, Geist and Newsreader · SIL Open Font License 1.1. Full license texts and source credits are bundled with this app and source."),"fontLicense",null);finishPage(null);
    }
    private List<String> favorites(){
        List<String> result=new ArrayList<>();String saved=prefs.getString("ordered",null);
        if(saved!=null){try{org.json.JSONArray a=new org.json.JSONArray(saved);for(int i=0;i<a.length();i++)result.add(a.getString(i));}catch(Exception ignored){}}
        else{result.addAll(prefs.getStringSet("items",Collections.emptySet()));Collections.sort(result);saveFavorites(result);}return result;
    }
    private void saveFavorites(List<String> list){if(!prefs.edit().putString("ordered",new org.json.JSONArray(list).toString()).commit())Toast.makeText(this,tr("Home menu could not be saved"),Toast.LENGTH_LONG).show();}
    private void moveFavorite(String name,String pkg,int delta,boolean editor){
        List<String> f=favorites();int i=f.indexOf(pkg),j=i+delta;if(i>=0&&j>=0&&j<f.size()){Collections.swap(f,i,j);saveFavorites(f);}showAppActions(name,pkg,editor);
    }
    private Bitmap decodePhoto(Uri uri) throws IOException{
        BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;
        try(InputStream in=getContentResolver().openInputStream(uri)){BitmapFactory.decodeStream(in,null,o);}
        if(o.outWidth<=0||o.outHeight<=0)throw new IOException("Invalid photo");
        o.inSampleSize=1;while(o.outWidth/o.inSampleSize>1920||o.outHeight/o.inSampleSize>1080)o.inSampleSize*=2;o.inJustDecodeBounds=false;
        try(InputStream in=getContentResolver().openInputStream(uri)){return BitmapFactory.decodeStream(in,null,o);}
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);
        if(request==PICK_PHOTO&&result==RESULT_OK&&data!=null&&data.getData()!=null){
            try{Bitmap test=decodePhoto(data.getData());if(test==null)throw new IOException();test.recycle();
                getContentResolver().takePersistableUriPermission(data.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if(!prefs.edit().putString("wallpaperUri",data.getData().toString()).putString("background","photo").commit())throw new IOException();
                cachedPhotoKey="";showBackground();
            }catch(Exception e){Toast.makeText(this,tr("Photo cannot be read or saved. Choose another image."),Toast.LENGTH_LONG).show();}
        }
    }

    private View pressedItem;
    /** Route remote keys through the current scene, avoiding a focusless Window on scene replacement. */
    @Override public boolean dispatchKeyEvent(KeyEvent event){
        if(root==null)return super.dispatchKeyEvent(event);
        int key=event.getKeyCode(),direction=0;
        switch(key){
            case KeyEvent.KEYCODE_DPAD_UP:direction=View.FOCUS_UP;break;
            case KeyEvent.KEYCODE_DPAD_DOWN:direction=View.FOCUS_DOWN;break;
            case KeyEvent.KEYCODE_DPAD_LEFT:direction=View.FOCUS_LEFT;break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:direction=View.FOCUS_RIGHT;break;
        }
        if(direction!=0){
            if(event.getAction()==KeyEvent.ACTION_DOWN){
                View current=root.findFocus();
                if(current==null)ensureFocus();
                else{
                    View next=FocusFinder.getInstance().findNextFocus(root,current,direction);
                    if(next!=null)next.requestFocus();
                }
            }
            return true;
        }
        if(key==KeyEvent.KEYCODE_DPAD_CENTER||key==KeyEvent.KEYCODE_ENTER){
            if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0)pressedItem=root.findFocus();
            if(event.getAction()==KeyEvent.ACTION_UP){
                View target=pressedItem;pressedItem=null;
                if(!event.isCanceled()&&target!=null&&target==root.findFocus()&&target.isClickable())target.performClick();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override public void onBackPressed(){
        switch(page){
            case "home":super.onBackPressed();break;
            case "options":if(optionBack!=null)optionBack.run();else showSettings();break;
            case "wallpapers":showBackground();break;
            case "credits":showBackground();break;
            case "about":restoreTag="about";showSettings();break;
            case "background":restoreTag="background";showSettings();break;
            case "typography":restoreTag="typography";showSettings();break;
            case "focus":restoreTag="focus";showSettings();break;
            case "homeEditor":restoreTag="homeApps";showSettings();break;
            case "homePrivacy":showHomeButton();break;
            case "homeButton":restoreTag="homeButton";showSettings();break;
            case "system":restoreTag="privacy";showSettings();break;
            case "packages":showSystem();break;
            case "packageDetail":showPackages();break;
            case "appActions":if(editingHome)showHomeEditor();else showApps();break;
            default:showHome();
        }
    }
}

