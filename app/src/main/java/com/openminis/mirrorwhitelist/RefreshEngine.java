package com.openminis.mirrorwhitelist;

import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import java.io.*;
import java.util.concurrent.*;
import java.util.regex.*;
import moe.shizuku.server.IShizukuService;
import moe.shizuku.server.IRemoteProcess;
import rikka.shizuku.Shizuku;

/** Runs the verified launcher-first refresh sequence. Never clears launcher data. */
public final class RefreshEngine {
 public interface Listener { void stage(String text); void done(boolean ok,String text); }
 final Handler main=new Handler(Looper.getMainLooper());
 final ExecutorService worker=Executors.newSingleThreadExecutor();
 final Listener listener; long deadline; int stable=0,last=-1,display; boolean active,openWhenDone;
 public RefreshEngine(Listener l){listener=l;}
 public static boolean ready(){try{return Shizuku.pingBinder() && Shizuku.checkSelfPermission()==PackageManager.PERMISSION_GRANTED;}catch(Throwable e){return false;}}
 public void start(){start(false);}
 public void start(boolean openAfterRefresh){
  if(active)return;
  active=true;openWhenDone=openAfterRefresh;display=0;stable=0;last=-1;listener.stage("1/2 重启外屏启动器（不清除数据）…");
  worker.execute(()->{
   try{
    if(!ready())throw new IOException("Shizuku 尚未运行或未授权");
    String state=run("dumpsys device_state | head -n 6");display=state.contains("name='CLOSED'")?1:0;
    String out=run("am force-stop com.vivo.fliplauncher && am start --display 1 -n com.vivo.fliplauncher/.Launcher");
    checkStart(out);
    deadline=android.os.SystemClock.elapsedRealtime()+20000;
    main.post(()->{listener.stage("2/2 等待原生外屏桌面初始化…");main.postDelayed(this::poll,2500);});
   }catch(Throwable e){fail(e);}
  });
 }
 void poll(){
  worker.execute(()->{
   try{
    String out=run("dumpsys activity provider com.vivo.fliplauncher/.data.provider.LauncherProvider --all");
    Matcher m=Pattern.compile("All apps list: size=(\\d+)").matcher(out);
    int count=m.find()?Integer.parseInt(m.group(1)):-1;
    stable=count>0?(count==last?stable+1:1):0;last=count;
    if(stable>=2){finishRefresh();return;}
    if(android.os.SystemClock.elapsedRealtime()>deadline)throw new IOException("初始化超时；为避免缓存空列表，没有打开管理页。请亮起并解锁外屏后点击重试刷新");
    main.post(()->main.postDelayed(this::poll,1000));
   }catch(Throwable e){fail(e);}
  });
 }
 void finishRefresh() throws Exception {
  if(openWhenDone){
   String state=run("dumpsys device_state | head -n 6");display=state.contains("name='CLOSED'")?1:0;
   String result=run("am start --display "+display+" -a com.vivo.fliplauncher.action.SETTING_APP_LIB");
   checkStart(result);
  }
  final String text=openWhenDone?"已刷新并打开魔镜管理页。请在可添加区点选新应用。":"刷新完成。白名单已生效；需要添加应用时，点击上方【魔镜管理】按钮。";
  main.post(()->{active=false;listener.done(true,text);});
 }
 static void checkStart(String out) throws IOException {
  if(out.contains("Error:")||out.contains("Exception")||out.contains("Permission Denial"))throw new IOException(out.trim());
 }
 void fail(Throwable e){String msg=e.getMessage();if(msg==null)msg=e.getClass().getSimpleName();final String text=msg;main.post(()->{active=false;listener.done(false,"刷新未完成："+text+"。白名单已保存，可重试；没有清除外屏数据。");});}
 public static String run(String command) throws Exception {
  if(!ready())throw new IOException("Shizuku 尚未运行或未授权");
  IShizukuService svc=IShizukuService.Stub.asInterface(Shizuku.getBinder());
  IRemoteProcess p=svc.newProcess(new String[]{"sh","-c","exec 2>&1; "+command},null,null);
  try{
   if(!p.waitForTimeout(15,"SECONDS")){p.destroy();throw new IOException("系统命令执行超时");}
   String out;
   try(InputStream in=new ParcelFileDescriptor.AutoCloseInputStream(p.getInputStream());ByteArrayOutputStream b=new ByteArrayOutputStream()){
    byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1){b.write(buf,0,n);if(b.size()>65536)throw new IOException("系统返回内容过大");}out=b.toString("UTF-8");
   }
   int code=p.exitValue();if(code!=0)throw new IOException("命令失败（"+code+"）："+out.trim());return out;
  }finally{try{p.destroy();}catch(Exception ignored){}}
 }
}
