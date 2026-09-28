package com.openminis.mirrorwhitelist;

import android.app.*;import android.os.*;import android.provider.Settings;import android.content.*;import android.content.pm.*;import android.graphics.Color;import android.graphics.drawable.ColorDrawable;import android.view.*;import android.widget.*;import java.util.*;

import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
 static final String ALLOW="fold_adaptive_screen_app_list", DENY="fold_adaptive_app_not_in_lab";
 MirrorUi ui; TextView status,progress,authButton,retryButton; EditText search; PackageManager pm; ArrayList<App> apps=new ArrayList<>();
 MirrorUi.Toggle autoRefresh; boolean busy=false; RefreshEngine engine; Runnable pending;
 final Shizuku.OnBinderReceivedListener binderListener=()->updateStatus();
 final Shizuku.OnBinderDeadListener deadListener=()->updateStatus();
 final Shizuku.OnRequestPermissionResultListener permissionListener=(request,result)->{updateStatus();if(request==73){Runnable r=pending;pending=null;if(result==PackageManager.PERMISSION_GRANTED){if(r!=null)r.run();}else toast("未获得 Shizuku 授权，未修改白名单");}};
 static class App { String label,pkg; App(String l,String p){label=l;pkg=p;} }
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);} TextView tv(String s,int z){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(Color.rgb(25,25,25));v.setPadding(dp(16),dp(10),dp(16),dp(10));return v;}
 @Override public void onCreate(Bundle b){super.onCreate(b);getPreferences(0).edit().remove("allow0").remove("deny0").remove("saved").apply();if(getPreferences(0).getInt("uiVersion",0)<3)getPreferences(0).edit().putInt("uiVersion",3).putString("refreshResult","1.2：修改后自动刷新，不自动打开管理页；使用【魔镜管理】按钮手动进入。").apply();pm=getPackageManager(); build();engine=new RefreshEngine(new RefreshEngine.Listener(){public void stage(String s){progress.setText(s);}public void done(boolean ok,String s){busy=false;progress.setText(s);getPreferences(0).edit().putString("refreshResult",s).putBoolean("needsRefresh",!ok).apply();refresh(search.getText().toString());toast(ok?"原生魔镜刷新完成":"刷新未完成，请重试");}});Shizuku.addBinderReceivedListenerSticky(binderListener);Shizuku.addBinderDeadListener(deadListener);Shizuku.addRequestPermissionResultListener(permissionListener);loadApps();refresh("");}
 @Override public void onResume(){super.onResume();if(status!=null)updateStatus();}
 @Override public void onDestroy(){Shizuku.removeBinderReceivedListener(binderListener);Shizuku.removeBinderDeadListener(deadListener);Shizuku.removeRequestPermissionResultListener(permissionListener);super.onDestroy();}
 void build(){ui=new MirrorUi(this);ui.build();status=ui.shizuku;progress=ui.progress;search=ui.search;autoRefresh=ui.auto;authButton=ui.auth;retryButton=ui.retry;}
 boolean hasPerm(){return checkSelfPermission("android.permission.WRITE_SECURE_SETTINGS")==PackageManager.PERMISSION_GRANTED;}
 String get(String k){String s=Settings.Secure.getString(getContentResolver(),k);return s==null?"":s;}
 LinkedHashSet<String> set(String s){LinkedHashSet<String> r=new LinkedHashSet<>();for(String x:s.split(",")){x=x.trim();if(!x.isEmpty()&&!x.equals("null"))r.add(x);}return r;}
 String csv(Collection<String> c){return android.text.TextUtils.join(",",c);}
 void loadApps(){Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(Intent.CATEGORY_LAUNCHER);List<ResolveInfo> rs=pm.queryIntentActivities(i,PackageManager.MATCH_ALL);HashSet<String> seen=new HashSet<>();for(ResolveInfo r:rs){String p=r.activityInfo.packageName;if(seen.add(p))apps.add(new App(String.valueOf(r.loadLabel(pm)),p));}Collections.sort(apps,(a,b)->java.text.Collator.getInstance(Locale.CHINA).compare(a.label,b.label));}
 void refresh(String q){if(ui!=null)ui.render(q);}
 void updateStatus(){if(ui!=null)ui.status();}
 void ensureShizuku(Runnable next){if(busy){toast("正在刷新，请稍候");return;}if(RefreshEngine.ready()){next.run();return;}try{if(!Shizuku.pingBinder()){progress.setText("Shizuku 服务未运行或 Binder 尚未接收。请先启动 Shizuku，再点击授权/重试；未修改白名单。");return;}pending=next;Shizuku.requestPermission(73);}catch(Throwable e){pending=null;progress.setText("无法请求 Shizuku 授权："+e.getMessage());}}
 void requestRefresh(){ensureShizuku(()->{busy=true;updateStatus();getPreferences(0).edit().putBoolean("needsRefresh",true).apply();engine.start();});}
 void mutate(Runnable action){if(busy){toast("正在刷新，请稍候");return;}if(!hasPerm()){toast("缺少 WRITE_SECURE_SETTINGS 权限，未修改白名单");return;}if(autoRefresh.isChecked())ensureShizuku(action);else action.run();}
 boolean writeVerified(String a,String d){String oldA=get(ALLOW),oldD=get(DENY);try{boolean x=Settings.Secure.putString(getContentResolver(),ALLOW,a);boolean y=Settings.Secure.putString(getContentResolver(),DENY,d);if(!x||!y||!get(ALLOW).equals(a)||!get(DENY).equals(d))throw new IllegalStateException("写入回读不一致");return true;}catch(Exception e){try{Settings.Secure.putString(getContentResolver(),ALLOW,oldA);Settings.Secure.putString(getContentResolver(),DENY,oldD);}catch(Exception ignored){}progress.setText("白名单写入失败："+e.getMessage()+"；已尝试恢复修改前的值，不执行刷新。");toast("写入未验证成功");return false;}}
 void changed(String message){refresh(search.getText().toString());getPreferences(0).edit().putBoolean("needsRefresh",true).apply();progress.setText(message+"，已回读验证。");if(autoRefresh.isChecked())requestRefresh();else progress.setText(message+"；自动刷新已关闭，请点击重试刷新。");}
 void toggle(String p){mutate(()->{LinkedHashSet<String>a=set(get(ALLOW)),d=set(get(DENY));boolean on=a.contains(p)&&!d.contains(p);if(on)a.remove(p);else{a.add(p);d.remove(p);}if(writeVerified(csv(a),csv(d)))changed(on?"已移出白名单":"已加入白名单");});}
 void confirmAll(){new AlertDialog.Builder(this).setTitle("全部加入白名单").setMessage("将所有可启动应用加入候选列表，但原生外屏最多实际添加 32 个。继续？").setNegativeButton("取消",null).setPositiveButton("继续",(d,w)->addAll()).show();}
 void addAll(){mutate(()->{LinkedHashSet<String>a=set(get(ALLOW));for(App x:apps)a.add(x.pkg);if(writeVerified(csv(a),get(DENY)))changed("已扩展候选白名单（保留厂商排除列表）");});}
 void openNative(){if(busy){toast("正在刷新，请稍候");return;}if(getPreferences(0).getBoolean("needsRefresh",true)){ensureShizuku(()->{busy=true;updateStatus();engine.start(true);});return;}try{ActivityOptions options=ActivityOptions.makeBasic();android.hardware.display.DisplayManager dm=(android.hardware.display.DisplayManager)getSystemService(DISPLAY_SERVICE);android.view.Display inner=dm.getDisplay(0);options.setLaunchDisplayId(inner!=null&&inner.getState()==android.view.Display.STATE_ON?0:1);startActivity(new Intent("com.vivo.fliplauncher.action.SETTING_APP_LIB"),options.toBundle());}catch(Exception e){progress.setText("无法打开魔镜管理："+e.getMessage());}}
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
