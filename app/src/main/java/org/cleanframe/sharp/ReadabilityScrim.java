package org.cleanframe.sharp;
import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Local shading: keeps the photograph visible without permanent app tiles. */
final class ReadabilityScrim extends View {
    private final boolean list;
    private final float scale;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private Shader leftShade,footerShade;
    private float fadeStart,leftEnd,footerTop;
    public ReadabilityScrim(Context c){this(c,true,1f);}
    ReadabilityScrim(Context c,boolean list,float scale){super(c);paint.setDither(true);this.list=list;this.scale=scale;setFocusable(false);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    private float dp(float v){return v*getResources().getDisplayMetrics().density*scale;}
    // Keep a constant tint up to the n in Settings, then fade evenly to the edge.
    void setListShadeBounds(float start,float end){
        leftEnd=Math.max(1,Math.min(getWidth(),end));
        fadeStart=Math.max(0,Math.min(start,leftEnd-1));
        if(list)leftShade=new LinearGradient(fadeStart,0,leftEnd,0,
            new int[]{0x7003080d,0x0003080d},null,Shader.TileMode.CLAMP);
        invalidate();
    }
    @Override protected void onSizeChanged(int w,int h,int oldw,int oldh){
        super.onSizeChanged(w,h,oldw,oldh);
        if(w<=0||h<=0)return;
        setListShadeBounds(leftEnd>0?fadeStart:dp(200),leftEnd>0?leftEnd:dp(300));
        footerTop=Math.max(0,h-dp(120));
        footerShade=new LinearGradient(0,footerTop,0,h,new int[]{0x0003080d,0x8803080d,0x8803080d},new float[]{0,.4f,1},Shader.TileMode.CLAMP);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if(leftShade!=null){paint.setShader(leftShade);canvas.drawRect(0,0,leftEnd,getHeight(),paint);}
        if(footerShade!=null){paint.setShader(footerShade);canvas.drawRect(0,footerTop,getWidth(),getHeight(),paint);}
        paint.setShader(null);
    }
}
