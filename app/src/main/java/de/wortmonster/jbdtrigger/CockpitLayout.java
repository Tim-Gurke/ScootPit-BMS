package de.wortmonster.jbdtrigger;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.LinkedHashMap;
import java.util.Map;

final class CockpitLayout {
    static final double DEFAULT_SPEED_SCALE_MAX = 22;
    static final String[] KEYS = {"speed","soc","range","distance","total","power","voltage","current","energy","consumption","consumption_500m","trip_time","moving","standing","average","maximum","altitude","ascent","temp1","temp2","outside","max_power","bms_output","ready_start","ready_end","trip_end","log","image","free_text","daily","tour"};
    static final String[] TITLES = {"Geschwindigkeit","Akku","Restreichweite ≈","Fahrtstrecke","Gesamtkilometer","Leistung","Spannung","Strom","Verbrauchte Energie","Durchschnittsverbrauch","Verbrauch · letzte Strecke","Gesamtzeit","Fahrzeit","Standzeit","Durchschnittsgeschwindigkeit","Maximum","Höhe","Höhenmeter","Temp1","Temp2","Außentemperatur","Maximale Fahrtleistung","BMS-Lastausgang","Bereitschaft starten","Bereitschaft beenden","Fahrt beenden","Statusdetails / Log","Bild","Freitext","Tageskilometer","Tourenzähler"};
    static JSONObject tile(String key) {
        JSONObject t = new JSONObject();
        try { t.put("key",key).put("font",20).put("weight",1).put("background","#00000000").put("text","#FFFFFFFF")
                .put("allow_overlap",true).put("custom_colors",true).put("instrument_color","#FF981F")
                .put("scale_color","#34434D").put("icon_color","#FFFFB41F").put("border_color","#28647A")
                .put("border_enabled",false).put("corner_radius",12).put("content_inset",0);
            if(AverageMetrics.supported(key))t.put("average_id",java.util.UUID.randomUUID().toString());
            if(key.equals("speed")||key.equals("average"))t.put("scale_max",DEFAULT_SPEED_SCALE_MAX);
            else if(key.equals("power")||key.equals("max_power"))t.put("scale_max",1400); }
        catch(Exception ignored) {}
        return t;
    }
    private static final String[] STYLE_FIELDS={"background","text","font","weight","allow_overlap","custom_colors","instrument_color","scale_color","icon_color","border_color","border_enabled","corner_radius","content_inset","dark_colors"};
    static JSONObject tile(String key,JSONArray existing) {
        JSONObject tile=tile(key);if(existing==null)return tile;
        try{for(String field:STYLE_FIELDS){Map<String,Integer> counts=new LinkedHashMap<>();Map<String,Object> values=new LinkedHashMap<>();
            for(int i=0;i<existing.length();i++){JSONObject current=existing.optJSONObject(i);if(current==null||!current.has(field))continue;Object value=current.get(field);String signature=value.toString();counts.put(signature,counts.getOrDefault(signature,0)+1);values.put(signature,value);}
            int bestCount=0;String best=null;boolean tied=false;for(Map.Entry<String,Integer> entry:counts.entrySet()){if(entry.getValue()>bestCount){bestCount=entry.getValue();best=entry.getKey();tied=false;}else if(entry.getValue()==bestCount)tied=true;}
            if(best!=null&&!tied){Object value=values.get(best);if(value instanceof JSONObject)value=new JSONObject(value.toString());else if(value instanceof JSONArray)value=new JSONArray(value.toString());tile.put(field,value);}
        }}catch(Exception ignored){}
        return tile;
    }
    static JSONObject row(String... keys) {
        JSONObject r = new JSONObject(); JSONArray cells = new JSONArray();
        for(String key:keys)cells.put(tile(key));
        try { r.put("height",120).put("tiles",cells); } catch(Exception ignored) {}
        return r;
    }
    static JSONArray defaults() {
        JSONArray rows = new JSONArray();
        rows.put(row("soc","range"));
        JSONObject speed=row("speed");
        try { speed.put("height",170);speed.getJSONArray("tiles").getJSONObject(0).put("font",56); } catch(Exception ignored) {}
        rows.put(speed);rows.put(row("distance","power"));rows.put(row("temp1","temp2"));
        rows.put(row("total","outside"));rows.put(row("moving","standing"));
        return rows;
    }
    static JSONArray load(SharedPreferences prefs) {
        try {
            JSONArray rows=new JSONArray(prefs.getString("cockpit_layout",""));
            if(rows.length()<1 || rows.length()>30)throw new Exception();
            for(int i=0;i<rows.length();i++){
                JSONArray cells=rows.getJSONObject(i).getJSONArray("tiles");
                if(cells.length()<1 || cells.length()>6)throw new Exception();
            }
            return rows;
        }catch(Exception ignored){return defaults();}
    }
    static String title(String key) {
        for(int i=0;i<KEYS.length;i++)if(KEYS[i].equals(key))return TITLES[i];
        String extra=BmsExtras.title(key);return extra==null?key:extra;
    }
}

