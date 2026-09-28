package com.openminis.mirrorwhitelist;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Native, responsive UI. No WebView and no network dependencies. */
final class MirrorUi {
 final MainActivity a;
 final int bg=Color.rgb(245,247,246),ink=Color.rgb(23,38,36),muted=Color.rgb(114,131,123),green=Color.rgb(50,155,120),dark=Color.rgb(25,60,52);
 ListView list; EditText search; TextView shizuku,secure,allowValue,denyValue,total,auth,retry,progress; Toggle auto;
 TextView[] chips=new TextView[3]; int filter=0; String query="";
 final ArrayList<MainActivity.App> visible=new ArrayList<>();
 Set<String> allowed=new HashSet<>(),denied=new HashSet<>();
 final HashMap<String,Drawable> iconCache=new HashMap<>(); AppAdapter adapter; LinearLayout root; View more,nativeButton;
 MirrorUi(MainActivity activity){a=activity;}
 int dp(float v){return (int)(v*a.getResources().getDisplayMetrics().density+.5f);}
 LinearLayout col(){LinearLayout l=new LinearLayout(a);l.setOrientation(1);return l;}
 LinearLayout row(){LinearLayout l=new LinearLayout(a);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(a);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));return t;}
 GradientDrawable shape(int color,float radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
 Drawable press(int color,float radius){return new RippleDrawable(ColorStateList.valueOf(0x18329b78),shape(color,radius),shape(Color.WHITE,radius));}
 void space(LinearLayout l,int h){l.addView(new View(a),new LinearLayout.LayoutParams(1,dp(h)));}
 LinearLayout.LayoutParams lp(int w,int h){return new LinearLayout.LayoutParams(w<0?w:dp(w),h<0?h:dp(h));}
 void build(){
  a.getWindow().setStatusBarColor(bg);a.getWindow().setNavigationBarColor(bg);a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  root=col();root.setBackgroundColor(bg);root.setFitsSystemWindows(true);root.setFocusableInTouchMode(true);
  root.setOnApplyWindowInsetsListener((v,i)->{android.graphics.Insets in=i.getInsets(android.view.WindowInsets.Type.systemBars()|android.view.WindowInsets.Type.displayCutout());v.setPadding(in.left,in.top,in.right,in.bottom);return i;});
  list=new ListView(a);list.setDivider(null);list.setSelector(new ColorDrawable(Color.TRANSPARENT));list.setCacheColorHint(bg);list.setVerticalScrollBarEnabled(false);list.setClipToPadding(false);list.setPadding(dp(22),0,dp(22),dp(12));
  LinearLayout header=col();header.setPadding(0,dp(12),0,0);
  LinearLayout heading=row();LinearLayout title=col();TextView brand=text("MIRROR LAB",10,muted,false);brand.setLetterSpacing(.16f);title.addView(brand);space(title,5);title.addView(text("魔镜白名单",28,ink,true));heading.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  IconView menu=new IconView(a,"more",ink);menu.setBackground(press(Color.WHITE,24));menu.setContentDescription("更多选项");menu.setOnClickListener(v->menu(v));heading.addView(menu,lp(44,44));more=menu;header.addView(heading);space(header,12);
  LinearLayout permissions=row();shizuku=text("● Shizuku 未连接",10,muted,false);shizuku.setMinHeight(dp(36));shizuku.setGravity(Gravity.CENTER_VERTICAL);shizuku.setOnClickListener(v->a.ensureShizuku(()->a.toast("Shizuku 已授权")));permissions.addView(shizuku,new LinearLayout.LayoutParams(0,dp(36),1));secure=text("◇ 安全设置权限",10,muted,false);permissions.addView(secure);header.addView(permissions);auth=shizuku;space(header,10);
  FrameLayout hero=new FrameLayout(a);hero.setBackground(shape(dark,24));hero.setElevation(dp(1));hero.setClipToOutline(true);
  PhoneArt phone=new PhoneArt(a);FrameLayout.LayoutParams art=new FrameLayout.LayoutParams(dp(140),dp(155),Gravity.RIGHT|Gravity.CENTER_VERTICAL);art.rightMargin=dp(2);hero.addView(phone,art);
  LinearLayout intro=col();intro.setPadding(dp(20),dp(18),dp(12),0);TextView tagline=text("VIVO X FLIP · 原生外屏",10,0xffadcec3,false);intro.addView(tagline);space(intro,9);TextView slogan=text("让外屏，\n多一点可能。",22,Color.WHITE,true);slogan.setLineSpacing(dp(1),1);intro.addView(slogan);space(intro,12);
  TextView manage=text("魔镜管理    ↗",12,dark,true);manage.setGravity(Gravity.CENTER);manage.setBackground(press(0xffd9f4e6,11));manage.setContentDescription("魔镜管理");manage.setOnClickListener(v->a.openNative());intro.addView(manage,lp(120,38));nativeButton=manage;hero.addView(intro,new FrameLayout.LayoutParams(-1,-1));header.addView(hero,lp(-1,175));space(header,17);
  LinearLayout stats=row();allowValue=text("0",25,ink,true);denyValue=text("0",25,ink,true);
  LinearLayout stat1=row();stat1.addView(allowValue);TextView al=text("  白名单应用",10,muted,false);stat1.addView(al);stats.addView(stat1,new LinearLayout.LayoutParams(0,-2,1));View div=new View(a);div.setBackgroundColor(0xffdfe5e1);stats.addView(div,lp(1,23));LinearLayout stat2=row();stat2.setGravity(Gravity.CENTER);stat2.addView(denyValue);stat2.addView(text("  系统排除",10,muted,false));stats.addView(stat2,new LinearLayout.LayoutParams(0,-2,1));stats.addView(text("按需放行",10,muted,false));header.addView(stats);space(header,17);
  LinearLayout autoCard=row();autoCard.setPadding(dp(14),dp(10),dp(12),dp(10));autoCard.setBackground(shape(Color.WHITE,18));IconView autoIcon=new IconView(a,"refresh",0xff3a8d6d);autoIcon.setBackground(shape(0xffedf6f1,10));autoCard.addView(autoIcon,lp(36,36));LinearLayout desc=col();desc.setPadding(dp(12),0,dp(5),0);desc.addView(text("自动刷新外屏",13,ink,true));space(desc,5);desc.addView(text("修改后自动同步，不跳转管理页",10,muted,false));autoCard.addView(desc,new LinearLayout.LayoutParams(0,-2,1));auto=new Toggle(a);auto.setContentDescription("自动刷新外屏");auto.setChecked(a.getPreferences(0).getBoolean("autoRefresh",true));auto.setOnCheckedChangeListener((v,on)->a.getPreferences(0).edit().putBoolean("autoRefresh",on).apply());autoCard.addView(auto,lp(46,48));header.addView(autoCard);
  LinearLayout sync=row();progress=new ProgressLabel(a);progress.setTextSize(10);progress.setTextColor(muted);progress.setPadding(dp(2),dp(9),dp(4),dp(9));progress.setGravity(Gravity.CENTER_VERTICAL);sync.addView(progress,new LinearLayout.LayoutParams(0,-2,1));retry=text("重新刷新 ↻",11,0xff3c8970,true);retry.setGravity(Gravity.CENTER);retry.setMinHeight(dp(44));retry.setOnClickListener(v->a.requestRefresh());retry.setContentDescription("重新刷新");sync.addView(retry,lp(86,-2));header.addView(sync);space(header,12);
  LinearLayout section=row();section.addView(text("应用",19,ink,true),new LinearLayout.LayoutParams(0,-2,1));total=text("正在加载应用…",10,muted,false);section.addView(total);header.addView(section);space(header,12);
  LinearLayout searchBox=row();searchBox.setPadding(dp(12),0,dp(4),0);searchBox.setBackground(shape(0xffeaf0ec,13));searchBox.addView(new IconView(a,"search",0xff82978c),lp(20,20));search=new EditText(a);search.setTextSize(12);search.setTextColor(ink);search.setHintTextColor(0xff8a9c91);search.setHint("搜索应用名称或包名");search.setSingleLine();search.setBackgroundColor(Color.TRANSPARENT);search.setPadding(dp(10),0,dp(10),0);search.setImeOptions(android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH);searchBox.addView(search,new LinearLayout.LayoutParams(0,dp(46),1));header.addView(searchBox);
  LinearLayout filters=row();for(int i=0;i<3;i++){final int f=i;chips[i]=text("",11,muted,true);chips[i].setGravity(Gravity.CENTER);chips[i].setPadding(dp(9),0,dp(9),0);chips[i].setOnClickListener(v->{filter=f;a.refresh(search.getText().toString());});LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(40),1);p.rightMargin=i<2?dp(7):0;filters.addView(chips[i],p);}LinearLayout.LayoutParams fp=lp(-1,40);fp.topMargin=dp(12);fp.bottomMargin=dp(12);header.addView(filters,fp);
  list.addHeaderView(header,null,false);TextView foot=text("ⓘ 放行后，需在「魔镜管理」中添加 · 原生上限 32 个",10,muted,false);foot.setGravity(Gravity.CENTER);foot.setPadding(dp(2),dp(17),dp(2),dp(14));list.addFooterView(foot,null,false);adapter=new AppAdapter();list.setAdapter(adapter);root.addView(list,new LinearLayout.LayoutParams(-1,-1));a.setContentView(root);root.requestFocus();list.post(()->list.setSelectionFromTop(0,0));progress.setText(a.getPreferences(0).getString("refreshResult","等待同步"));
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int af){}public void onTextChanged(CharSequence s,int st,int before,int c){a.refresh(s.toString());}public void afterTextChanged(Editable e){}});
 }
 void status(){boolean ok=RefreshEngine.ready();shizuku.setText(ok?"● Shizuku 已连接":"● Shizuku 未授权 · 点击连接");shizuku.setTextColor(ok?0xff398565:0xffb67c36);secure.setText(a.hasPerm()?"◇ 安全设置权限已授权":"◇ 写入权限未授权");secure.setTextColor(a.hasPerm()?muted:0xffb67c36);allowValue.setText(String.valueOf(a.set(a.get(MainActivity.ALLOW)).size()));denyValue.setText(String.valueOf(a.set(a.get(MainActivity.DENY)).size()));retry.setEnabled(!a.busy);retry.setAlpha(a.busy?.45f:1);auto.setEnabled(!a.busy);nativeButton.setEnabled(!a.busy);nativeButton.setAlpha(a.busy?.6f:1);}
 void render(String q){query=q.trim().toLowerCase(Locale.ROOT);allowed=a.set(a.get(MainActivity.ALLOW));denied=a.set(a.get(MainActivity.DENY));visible.clear();int released=0;for(MainActivity.App app:a.apps){boolean on=isAllowed(app);if(on)released++;if(filter==1&&!on||filter==2&&on)continue;if(!query.isEmpty()&&!app.label.toLowerCase(Locale.ROOT).contains(query)&&!app.pkg.toLowerCase(Locale.ROOT).contains(query))continue;visible.add(app);}java.text.Collator nameOrder=java.text.Collator.getInstance(Locale.CHINA);Collections.sort(visible,(x,y)->{int stateOrder=Boolean.compare(isAllowed(y),isAllowed(x));return stateOrder!=0?stateOrder:nameOrder.compare(x.label,y.label);});total.setText(a.apps.size()+" 个可启动应用");String[] t={"全部应用 "+a.apps.size(),"已放行 "+released,"未放行 "+(a.apps.size()-released)};for(int i=0;i<3;i++){chips[i].setText(t[i]);chips[i].setTextColor(filter==i?0xff2a7357:muted);chips[i].setBackground(press(filter==i?0xffe0efe7:bg,10));chips[i].setSelected(filter==i);}status();adapter.notifyDataSetChanged();}
 boolean isAllowed(MainActivity.App app){return allowed.contains(app.pkg)&&!denied.contains(app.pkg);}
 void menu(View anchor){PopupMenu m=new PopupMenu(a,anchor);m.getMenu().add(0,1,0,"全部加入白名单…");m.getMenu().add(0,3,2,"Shizuku 授权 / 检查");m.getMenu().add(0,4,3,"关于魔镜白名单");m.setOnMenuItemClickListener(i->{switch(i.getItemId()){case 1:a.confirmAll();break;case 3:a.ensureShizuku(()->a.toast("Shizuku 已授权"));break;case 4:new android.app.AlertDialog.Builder(a).setTitle("魔镜白名单 1.3.1").setMessage("原生外屏白名单管理器\n\n修改白名单后自动刷新，不自动打开管理页。点击魔镜管理完成添加。\n\n无需 Root · 不清除外屏数据\n原生魔镜已添加上限：32 个\n\n界面与插画：本地原生绘制").setPositiveButton("知道了",null).show();}return true;});m.show();}
 class AppAdapter extends BaseAdapter {
  public int getCount(){return Math.max(1,visible.size());}public Object getItem(int p){return visible.isEmpty()?null:visible.get(p);}public long getItemId(int p){return p;}public int getViewTypeCount(){return 2;}public int getItemViewType(int p){return visible.isEmpty()?1:0;}
  public View getView(int pos,View old,ViewGroup parent){if(visible.isEmpty()){TextView e=text("没有匹配的应用",13,muted,false);e.setGravity(Gravity.CENTER);e.setPadding(0,dp(28),0,dp(28));e.setBackground(shape(Color.WHITE,18));return e;}
   Holder h;if(old==null||!(old.getTag() instanceof Holder)){h=new Holder();LinearLayout container=col();LinearLayout r=row();r.setPadding(dp(14),dp(5),dp(10),dp(5));h.icon=new ImageView(a);h.icon.setScaleType(ImageView.ScaleType.FIT_CENTER);r.addView(h.icon,lp(38,38));LinearLayout names=col();names.setPadding(dp(12),0,dp(7),0);h.title=text("",13,ink,true);h.title.setSingleLine();h.title.setEllipsize(TextUtils.TruncateAt.END);h.pkg=text("",9,0xff84958a,false);h.pkg.setSingleLine();h.pkg.setEllipsize(TextUtils.TruncateAt.MIDDLE);names.addView(h.title);space(names,4);names.addView(h.pkg);r.addView(names,new LinearLayout.LayoutParams(0,-2,1));h.toggle=new Toggle(a);r.addView(h.toggle,lp(46,48));container.addView(r,lp(-1,66));View line=new View(a);line.setBackgroundColor(0xffeff2ef);LinearLayout.LayoutParams l=lp(-1,1);l.leftMargin=dp(64);l.rightMargin=dp(14);container.addView(line,l);h.line=line;h.row=r;container.setTag(h);old=container;}else h=(Holder)old.getTag();
   MainActivity.App app=visible.get(pos);h.title.setText(app.label);h.pkg.setText(app.pkg);Drawable icon=iconCache.get(app.pkg);if(icon==null){try{icon=a.pm.getApplicationIcon(app.pkg);}catch(Exception e){icon=a.pm.getDefaultActivityIcon();}iconCache.put(app.pkg,icon);}h.icon.setImageDrawable(icon);h.toggle.setOnCheckedChangeListener(null);h.toggle.setChecked(isAllowed(app));h.toggle.setEnabled(!a.busy);h.toggle.setContentDescription(app.label+"白名单开关");h.toggle.setOnClickListener(v->a.toggle(app.pkg));h.row.setContentDescription(app.label+"，"+(isAllowed(app)?"已放行":"未放行"));h.row.setOnClickListener(v->a.toggle(app.pkg));old.setBackground(press(Color.WHITE,pos==0||pos==visible.size()-1?16:0));h.line.setVisibility(pos==visible.size()-1?View.GONE:View.VISIBLE);return old;
  }
 }
 static class Holder{ImageView icon;TextView title,pkg;Toggle toggle;View row,line;}
 class Toggle extends CompoundButton {
  Paint p=new Paint(3);Toggle(Context c){super(c);setButtonDrawable((Drawable)null);setBackground(press(Color.TRANSPARENT,22));setClickable(true);setFocusable(true);}
  protected void onDraw(Canvas c){float s=getResources().getDisplayMetrics().density;float w=38*s,h=24*s,x=(getWidth()-w)/2,y=(getHeight()-h)/2;p.setColor(isChecked()?green:0xffe7ede9);p.setAlpha(isEnabled()?255:140);c.drawRoundRect(x,y,x+w,y+h,12*s,12*s,p);p.setColor(Color.WHITE);c.drawCircle(x+(isChecked()?26:12)*s,y+12*s,9*s,p);}
 }
 class ProgressLabel extends TextView {
  String full="";ProgressLabel(Context c){super(c);setMaxLines(3);setOnClickListener(v->new android.app.AlertDialog.Builder(a).setTitle("刷新状态").setMessage(full).setPositiveButton("关闭",null).show());}
  public void setText(CharSequence s,BufferType type){full=s==null?"":s.toString();String display=full;if(full.startsWith("刷新完成")||full.startsWith("已刷新"))display="✓ 原生外屏已同步";else if(full.startsWith("1.2")||full.startsWith("1.3"))display=a.getPreferences(0).getBoolean("needsRefresh",true)?"待刷新 · 点击右侧同步":"✓ 原生外屏已同步";else if(full.contains("未完成")||full.contains("失败"))display="刷新未完成 · 点击查看详情";super.setText(display,type);setContentDescription(full);}
 }
 class IconView extends View {
  final String kind;final int color;Paint p=new Paint(3);IconView(Context c,String k,int col){super(c);kind=k;color=col;}
  protected void onDraw(Canvas c){super.onDraw(c);c.save();float s=Math.min(getWidth(),getHeight())/24f;c.translate((getWidth()-24*s)/2,(getHeight()-24*s)/2);c.scale(s,s);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setStrokeCap(Paint.Cap.ROUND);
   if(kind.equals("more")){p.setStyle(Paint.Style.FILL);for(int x:new int[]{8,12,16})c.drawCircle(x,12,1,p);}
   else if(kind.equals("search")){c.drawCircle(10,10,6,p);c.drawLine(14.5f,14.5f,20,20,p);}
   else{c.drawArc(4,4,20,20,210,135,false,p);c.drawArc(4,4,20,20,30,135,false,p);Path path=new Path();path.moveTo(18,3);path.lineTo(19,8);path.lineTo(14,8);path.moveTo(6,21);path.lineTo(5,16);path.lineTo(10,16);c.drawPath(path,p);}c.restore();
  }
 }
 class PhoneArt extends View {
  Paint p=new Paint(3);PhoneArt(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
  void rect(Canvas c,float x,float y,float w,float h,float r,int col){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(col);c.drawRoundRect(x,y,x+w,y+h,r,r,p);}
  protected void onDraw(Canvas c){c.save();c.scale(getWidth()/150f,getHeight()/170f);c.translate(37,9);c.rotate(12,43,75);rect(c,1,5,91,140,14,0x263b5748);p.setShader(new LinearGradient(0,0,90,140,new int[]{0xffc1d9cb,0xff669984},null,Shader.TileMode.CLAMP));c.drawRoundRect(-1,-1,89,139,14,14,p);p.setShader(null);p.setColor(0xff426b58);p.setStrokeWidth(2);c.drawLine(1,71,87,71,p);p.setShader(new LinearGradient(5,7,83,64,new int[]{0xff183329,0xff2d5d48},null,Shader.TileMode.CLAMP));c.drawRoundRect(5,7,83,64,10,10,p);p.setShader(null);
   for(int x:new int[]{57,70}){p.setColor(0xff84ae97);c.drawCircle(x,16,6,p);p.setColor(0xff12251f);c.drawCircle(x,16,4.6f,p);p.setColor(0xff375648);c.drawCircle(x-1,15,1.4f,p);}rect(c,13,26,15,15,5,0xffe2f1e4);rect(c,35,26,15,15,5,0xff9bceaa);rect(c,57,26,15,15,5,0xff588f73);p.setStyle(Paint.Style.STROKE);p.setColor(0xff276044);p.setStrokeWidth(1.3f);Path m=new Path();m.moveTo(16,37);m.lineTo(19,30);m.lineTo(21,34);m.lineTo(24,30);m.lineTo(25,37);c.drawPath(m,p);p.setStyle(Paint.Style.FILL);Path tri=new Path();tri.moveTo(40,30);tri.lineTo(45,33);tri.lineTo(40,36);tri.close();p.setColor(0xff305a40);c.drawPath(tri,p);p.setColor(0xffd9efdd);c.drawCircle(64.5f,33.5f,3,p);rect(c,13,46,59,3,1.5f,0xff65927a);rect(c,13,52,37,2,1,0xff436c56);
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.7f);p.setColor(0x88c7e1cc);for(int y:new int[]{82,91}){Path line=new Path();line.moveTo(10,y);line.cubicTo(40,y,46,y+39,77,y+30);c.drawPath(line,p);}p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans-serif",0));p.setTextSize(9);p.setTextAlign(Paint.Align.CENTER);p.setColor(0xffd7e8dc);c.drawText("vivo",45,127,p);c.restore();
  }
 }
}
