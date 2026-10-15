package de.wortmonster.jbdtrigger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import org.json.JSONArray;
import org.json.JSONObject;

/** Real app screenshots on a disposable emulator; all ride readings are examples. */
final class GuideScreenshots {
    private final Instrumentation runner;
    GuideScreenshots(Instrumentation runner){this.runner=runner;}
    void capture(){
        Bundle result=new Bundle();
        try{
            checkPresentationMigration();
            runner.getTargetContext().stopService(new Intent(runner.getTargetContext(),BmsMonitorService.class));
            SharedPreferences prefs=runner.getTargetContext().getSharedPreferences("settings",0);
            prefs.edit().clear().commit();
            Activity activity=runner.startActivitySync(new Intent(runner.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runner.waitForIdleSync();
            runner.runOnMainSync(()->{
                try{
                    ScooterProfiles.rename(prefs,"Beispiel-Scooter");
                    prefs.edit().putString("device_address","02:00:00:00:00:01").putString("device_name","Beispiel-BMS").putString("total_km","123.45").putString("tour_km","16.63").putString("daily_km","16.63")
                        .putString("daily_day",DistanceCounters.day(System.currentTimeMillis())).putBoolean("weather_enabled",true).commit();
                    AverageTracker averages=new AverageTracker();AverageMetrics.initialize(averages,prefs);averages.beginTrip();
                    averages.record(7900,1422000,130.0,true);averages.record(300,54000,4.02,true);
                    prefs.edit().putString(AverageMetrics.STATE,averages.encode()).commit();
                    invoke(activity,"rebuild",new Class[0],new Object[0]);
                }catch(Exception e){throw new RuntimeException(e);}
            });
            for(boolean light:new boolean[]{false,true}){
                runner.runOnMainSync(()->{
                    try{
                        CockpitTheme.apply(prefs,light,true);
                        set(activity,"boardTiles",CockpitBoard.load(prefs));
                        BmsMonitorService.running=true;
                        invoke(activity,"rebuild",new Class[0],new Object[0]);
                        invoke(activity,"renderStatus",new Class[]{Intent.class},new Object[]{sample(false)});
                        JSONArray tiles=CockpitBoard.load(prefs);JSONObject pause=null;
                        for(int i=0;i<tiles.length();i++)if("trip_end".equals(tiles.getJSONObject(i).optString("key")))pause=tiles.getJSONObject(i);
                        if(pause==null)throw new AssertionError("Pause tile missing");
                        int icon=(Integer)invoke(activity,"tripActionColor",new Class[]{JSONObject.class},new Object[]{pause});
                        int background=(Integer)invoke(activity,"tripActionBackground",new Class[0],new Object[0]);
                        if(androidx.core.graphics.ColorUtils.calculateContrast(icon,background)<4.5)throw new AssertionError("Pause icon contrast");
                        // Old user layouts can still specify black; the rendered control must stay readable.
                        pause.put("icon_color","#FF000000");
                        icon=(Integer)invoke(activity,"tripActionColor",new Class[]{JSONObject.class},new Object[]{pause});
                        if(androidx.core.graphics.ColorUtils.calculateContrast(icon,background)<4.5)throw new AssertionError("Legacy pause icon contrast");
                        Button button=(Button)get(activity,"tripActionButton");
                        if(!button.isEnabled())throw new AssertionError("Pause menu unavailable during a ride");
                    }catch(Exception e){throw new RuntimeException(e);}
                });
                screenshot(light?"anleitung-cockpit-hell.png":"anleitung-cockpit-dunkel.png");
            }
            runner.runOnMainSync(()->{
                try{CockpitTheme.apply(prefs,false,true);set(activity,"boardTiles",CockpitBoard.load(prefs));invoke(activity,"rebuild",new Class[0],new Object[0]);invoke(activity,"renderStatus",new Class[]{Intent.class},new Object[]{sample(false)});invoke(activity,"tripAction",new Class[0],new Object[0]);}catch(Exception e){throw new RuntimeException(e);}
            });
            screenshot("anleitung-pausenmenue.png");
            requireText("Fahrt pausieren");requireText("Fahrt beenden");
            runner.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);runner.waitForIdleSync();
            runner.runOnMainSync(()->{invoke(activity,"renderStatus",new Class[]{Intent.class},new Object[]{sample(true)});invoke(activity,"tripAction",new Class[0],new Object[0]);});
            screenshot("anleitung-pause-automatik.png");
            requireText("Weiterfahrt: automatisch fortsetzen");
            runner.sendKeyDownUpSync(android.view.KeyEvent.KEYCODE_BACK);runner.waitForIdleSync();
            runner.runOnMainSync(()->invoke(activity,"showSettings",new Class[0],new Object[0]));
            screenshot("anleitung-einstellungen.png");
            runner.runOnMainSync(()->{try{((AlertDialog)get(activity,"settingsDialog")).dismiss();BmsMonitorService.running=false;invoke(activity,"editLayout",new Class[0],new Object[0]);}catch(Exception e){throw new RuntimeException(e);}});
            screenshot("anleitung-cockpit-bearbeiten.png");
            runner.runOnMainSync(()->{
                try{JSONArray tiles=(JSONArray)get(activity,"boardTiles");int index=-1;for(int i=0;i<tiles.length();i++)if("consumption".equals(tiles.getJSONObject(i).optString("key")))index=i;
                    if(index<0)throw new AssertionError("Consumption tile missing");invoke(activity,"editBoardTile",new Class[]{int.class},new Object[]{index});
                }catch(Exception e){throw new RuntimeException(e);}
            });
            screenshot("anleitung-durchschnitt.png");
            result.putString("stream","Guide screenshots: OK\n");runner.finish(Activity.RESULT_OK,result);
        }catch(Throwable e){result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(e));runner.finish(Activity.RESULT_CANCELED,result);}
        finally{BmsMonitorService.running=false;}
    }
    private Intent sample(boolean paused){long now=System.currentTimeMillis();return new Intent()
        .putExtra("trip_active",true).putExtra("trip_paused",paused).putExtra("trip_paused_manual",paused)
        .putExtra("bms_connected",true).putExtra("bms_at",now).putExtra("gps_at",now).putExtra("discharge_enabled",true)
        .putExtra("soc",75).putExtra("range_km",24.5).putExtra("speed_kmh",paused?0:18.2)
        .putExtra("voltage",50.4).putExtra("current",paused?0:-8.0).putExtra("discharge_watts",paused?0:403.2)
        .putExtra("temperatures",new double[]{25,23}).putExtra("distance_m",8200.0).putExtra("moving_ms",1476000L)
        .putExtra("standing_ms",180000L).putExtra("max_power_w",980.0).putExtra("wh_500m",13.4)
        .putExtra("outside_temperature",19.0).putExtra("weather_at",now);
    }
    private void checkPresentationMigration()throws Exception{
        JSONObject preset;try(java.io.InputStream in=runner.getTargetContext().getAssets().open("default_cockpit_settings.json")){java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);preset=new JSONObject(out.toString("UTF-8"));}
        JSONObject previous=preset.getJSONObject("previous_presentation");
        SharedPreferences test=runner.getTargetContext().getSharedPreferences("guide-migration-test",0);
        try{
            for(boolean custom:new boolean[]{false,true}){
                test.edit().clear().commit();JSONArray old=new JSONArray(previous.getJSONArray("tiles").toString());
                if(custom){old.getJSONObject(0).put("x",1).put("w",23).put("scale_max",57);}
                test.edit().putString("cockpit_board",old.toString()).putString("cockpit_board_landscape",previous.getJSONArray("landscape_tiles").toString())
                    .putString("cockpit_style_sync_version","1.6.6").putString("total_km","321.09").putString("device_address","02:00:00:00:00:01").putString("app_background","#071018").commit();
                ScooterProfiles.install(test);CockpitStyles.migrate(runner.getTargetContext(),test);
                JSONArray current=new JSONArray(test.getString("cockpit_board","[]"));java.util.Map<String,JSONObject> tiles=CockpitStyles.keyed(current);
                JSONObject pause=tiles.get("trip_end:0"),consumption=tiles.get("consumption:0"),gauge=tiles.get("consumption_500m:0");
                if(!custom&&(pause.getInt("x")!=9||pause.getInt("y")!=5||pause.getInt("w")!=6||consumption.getInt("x")!=12||consumption.getInt("y")!=27))throw new AssertionError("Standard cockpit did not adopt the new positions");
                if(custom&&(gauge.getInt("x")!=1||gauge.getInt("w")!=23||gauge.getInt("scale_max")!=57||pause.getInt("x")!=20))throw new AssertionError("Custom cockpit geometry or scale changed");
                if(consumption.optInt("average_mode")!=AverageTracker.RESET||gauge.optInt("average_window_m")!=300||!"#FFD7DEE5".equals(pause.getString("icon_color")))throw new AssertionError("Updated cockpit values not adopted");
                if(!"321.09".equals(test.getString("total_km",""))||!"02:00:00:00:00:01".equals(test.getString("device_address","")))throw new AssertionError("Presentation migration changed scooter data");
                String once=current.toString();CockpitStyles.migrate(runner.getTargetContext(),test);
                if(!once.equals(test.getString("cockpit_board","")))throw new AssertionError("Presentation migration repeated");
            }
        }finally{test.edit().clear().commit();}
    }
    private void requireText(String text){android.view.accessibility.AccessibilityNodeInfo root=runner.getUiAutomation().getRootInActiveWindow();if(root==null||root.findAccessibilityNodeInfosByText(text).isEmpty())throw new AssertionError("Missing guide control: "+text);}
    private static Object invoke(Object target,String name,Class[] types,Object[] args){try{Method m=target.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(target,args);}catch(Exception e){throw new RuntimeException(e);}}
    private static Object get(Object target,String name)throws Exception{java.lang.reflect.Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}
    private static void set(Object target,String name,Object value)throws Exception{java.lang.reflect.Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
    private void screenshot(String name)throws Exception{runner.waitForIdleSync();Thread.sleep(350);Bitmap bitmap=runner.getUiAutomation().takeScreenshot();if(bitmap==null)throw new AssertionError("Screenshot unavailable");File dir=new File(runner.getTargetContext().getExternalFilesDir(null),"screenshots");if(!dir.isDirectory()&&!dir.mkdirs())throw new java.io.IOException("Screenshot folder");try(FileOutputStream out=new FileOutputStream(new File(dir,name))){bitmap.compress(Bitmap.CompressFormat.PNG,100,out);}finally{bitmap.recycle();}}
}
