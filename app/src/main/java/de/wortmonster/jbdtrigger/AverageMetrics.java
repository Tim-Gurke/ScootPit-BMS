package de.wortmonster.jbdtrigger;

import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

final class AverageMetrics {
    static final String STATE="average_state";
    private AverageMetrics(){}
    static boolean supported(String key){return key.equals("average")||key.equals("consumption")||key.equals("consumption_500m");}
    static int mode(JSONObject tile){return Math.max(0,Math.min(2,tile.optInt("average_mode",tile.optString("key").equals("consumption_500m")?0:1)));}
    static int window(JSONObject tile,SharedPreferences prefs){return Math.max(1,Math.min(AverageTracker.MAX_WINDOW_METERS,tile.optInt("average_window_m",prefs.getInt("consumption_window_m",500))));}
    static String id(JSONObject tile){return tile.optString("average_id","average-"+tile.optString("key"));}
    static String note(JSONObject tile,SharedPreferences prefs){return mode(tile)==0?"letzte "+window(tile,prefs)+" m":mode(tile)==1?"aktuelle Fahrt":"ab Reset · antippen zum Zurücksetzen";}
    static boolean ensureIds(JSONArray tiles)throws JSONException{
        boolean changed=false;Map<String,Integer> counts=new HashMap<>();
        for(int i=0;i<tiles.length();i++){JSONObject tile=tiles.getJSONObject(i);String key=tile.optString("key");if(!supported(key))continue;int n=counts.getOrDefault(key,0);counts.put(key,n+1);if(!tile.has("average_id")){tile.put("average_id","average-"+key+"-"+n);changed=true;}}
        return changed;
    }
    static void initialize(AverageTracker tracker,SharedPreferences prefs){
        for(String key:new String[]{"cockpit_board","cockpit_board_landscape"})try{JSONArray tiles=new JSONArray(prefs.getString(key,"[]"));ensureIds(tiles);for(int i=0;i<tiles.length();i++){JSONObject t=tiles.getJSONObject(i);if(supported(t.optString("key"))&&mode(t)==2)tracker.ensureCounter(id(t));}}catch(JSONException ignored){}
    }
    static boolean available(SharedPreferences prefs){return live(prefs)!=null||prefs.contains(STATE);}
    static AverageTracker tracker(SharedPreferences prefs){BmsMonitorService service=live(prefs);return service==null?AverageTracker.restore(prefs.getString(STATE,"")):service.averageTracker();}
    static AverageTracker.Reading reading(SharedPreferences prefs,JSONObject tile){AverageTracker tracker=tracker(prefs);if(mode(tile)==2&&tracker.ensureCounter(id(tile)))save(prefs,tracker);return tracker.reading(mode(tile),window(tile,prefs),id(tile));}
    static void reset(SharedPreferences prefs,JSONObject tile){AverageTracker tracker=tracker(prefs);tracker.reset(id(tile));save(prefs,tracker);}
    private static void save(SharedPreferences prefs,AverageTracker tracker){prefs.edit().putString(STATE,tracker.encode()).commit();}
    private static BmsMonitorService live(SharedPreferences prefs){BmsMonitorService service=BmsMonitorService.liveService;return service!=null&&service.averageProfile().equals(prefs.getString(ScooterProfiles.ACTIVE,""))?service:null;}
}
