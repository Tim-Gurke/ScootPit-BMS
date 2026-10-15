package de.wortmonster.jbdtrigger;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

/** Syncs standard presentation while preserving an individually arranged board. */
final class CockpitStyles {
    private static final String VERSION="1.6.9",MARKER="cockpit_style_sync_version";
    static final String[] FIELDS={"caption","font","weight","unit_font","scale_max","consumption_display_unit","background","text","instrument_color","scale_color","icon_color","border_color","custom_colors","allow_overlap","border_enabled","corner_radius","content_inset","scale_gradient_mode","scale_gradient_low","scale_gradient_mid","scale_gradient_high","scale_gradient_low_full","scale_gradient_high_full","scale_reverse","average_mode","average_window_m","dark_colors"};
    private CockpitStyles(){}
    static Map<String,JSONObject> keyed(JSONArray tiles)throws JSONException{
        Map<String,JSONObject> result=new LinkedHashMap<>();Map<String,Integer> counts=new HashMap<>();
        for(int i=0;i<tiles.length();i++){JSONObject tile=tiles.getJSONObject(i);String key=tile.getString("key");int n=counts.getOrDefault(key,0);counts.put(key,n+1);result.put(key+":"+n,tile);}return result;
    }
    static JSONArray sync(JSONArray portrait,JSONArray landscape)throws Exception{
        JSONArray result=new JSONArray(landscape.toString());Map<String,JSONObject> source=keyed(portrait);
        for(Map.Entry<String,JSONObject> entry:keyed(result).entrySet()){JSONObject from=source.get(entry.getKey()),to=entry.getValue();if(from==null)continue;
            for(String field:FIELDS){if(from.has(field))to.put(field,from.get(field));else to.remove(field);}
            if(from.has("average_id"))to.put("average_id",from.get("average_id"));
        }AverageMetrics.ensureIds(result);CockpitBoard.validate(result);return result;
    }
    static void migrate(Context context,SharedPreferences prefs){
        try{
            JSONObject template;try(java.io.InputStream in=context.getAssets().open("default_cockpit_settings.json")){java.io.ByteArrayOutputStream buffer=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[4096];int count;while((count=in.read(bytes))!=-1)buffer.write(bytes,0,count);template=new JSONObject(new String(buffer.toByteArray(),java.nio.charset.StandardCharsets.UTF_8));}
            JSONObject old=template.getJSONObject("previous_presentation"),palette=template.getJSONObject("settings"),oldPalette=old.getJSONObject("settings");
            Map<String,JSONObject> before=keyed(old.getJSONArray("tiles")),after=keyed(template.getJSONArray("cockpit_board"));
            JSONArray oldPortrait=old.getJSONArray("tiles"),oldLandscape=old.optJSONArray("landscape_tiles");
            JSONArray profiles=ScooterProfiles.export(prefs);String active=prefs.getString(ScooterProfiles.ACTIVE,"");JSONObject current=null;boolean changed=false;
            for(int i=0;i<profiles.length();i++){
                JSONObject profile=profiles.getJSONObject(i),settings=profile.getJSONObject("settings");if(VERSION.equals(settings.optString(MARKER)))continue;
                if(!settings.has("cockpit_board"))continue;
                JSONArray portrait=new JSONArray(settings.getString("cockpit_board"));boolean stockPortrait=sameLayout(portrait,oldPortrait);
                if(stockPortrait)portrait=updateStandardLayout(portrait,oldPortrait,template.getJSONArray("cockpit_board"));
                boolean light=light(settings.optString("app_background","#071018"));
                for(Map.Entry<String,JSONObject> entry:keyed(portrait).entrySet()){
                    JSONObject was=before.get(entry.getKey()),next=after.get(entry.getKey()),tile=entry.getValue();if(was==null||next==null)continue;
                    for(String field:FIELDS){
                        if(light&&Arrays.asList(CockpitTheme.COLOR_FIELDS).contains(field))continue;
                        if(same(tile.opt(field),was.opt(field))){if(next.has(field))tile.put(field,next.get(field));else tile.remove(field);}
                    }
                    if(light){JSONObject dark=new JSONObject();for(String field:CockpitTheme.COLOR_FIELDS)if(next.has(field))dark.put(field,next.get(field));
                        // Preserve custom light colours; keep the supplied dark counterpart for standard colours.
                        int existing=android.graphics.Color.parseColor(tile.optString("icon_color","#FF981F"));
                        if(existing!=CockpitSymbols.defaultColor(tile.optString("key"),true)&&!same(tile.opt("icon_color"),was.opt("icon_color")))dark.put("icon_color",tile.optString("icon_color"));
                        tile.put("dark_colors",dark);CockpitTheme.tileColors(tile,true);
                    }
                }
                AverageMetrics.ensureIds(portrait);CockpitBoard.validate(portrait);
                JSONArray landscape=settings.has("cockpit_board_landscape")?new JSONArray(settings.getString("cockpit_board_landscape")):new JSONArray(portrait.toString());
                if(oldLandscape!=null&&sameLayout(landscape,oldLandscape))landscape=updateStandardLayout(landscape,oldLandscape,template.getJSONArray("cockpit_board_landscape"));
                landscape=sync(portrait,landscape);settings.put("cockpit_board",portrait.toString()).put("cockpit_board_landscape",landscape.toString()).put(MARKER,VERSION).put("cockpit_scale_sync_version","1.5.6");
                if(!light){Iterator<String> fields=oldPalette.keys();while(fields.hasNext()){String field=fields.next();if(same(settings.opt(field),oldPalette.opt(field)))settings.put(field,palette.get(field));}}
                if(profile.getString("id").equals(active))current=settings;changed=true;
            }
            if(changed){SharedPreferences.Editor edit=prefs.edit().putString(ScooterProfiles.LIST,profiles.toString());if(current!=null)ScooterProfiles.put(edit,current);if(!edit.commit())throw new java.io.IOException("Design konnte nicht gespeichert werden");}
        }catch(Exception e){android.util.Log.e("ScootPit","Darstellungswerte konnten nicht übernommen werden",e);}
    }
    private static boolean sameLayout(JSONArray current,JSONArray reference)throws JSONException{
        if(current.length()!=reference.length())return false;
        for(int i=0;i<current.length();i++){JSONObject a=current.getJSONObject(i),b=reference.getJSONObject(i);
            for(String field:new String[]{"key","x","y","w","h"})if(!same(a.opt(field),b.opt(field)))return false;
        }return true;
    }
    private static JSONArray updateStandardLayout(JSONArray current,JSONArray previous,JSONArray standard)throws Exception{
        Map<String,JSONObject> oldTiles=keyed(previous),currentTiles=keyed(current);JSONArray result=new JSONArray();Map<String,Integer> counts=new HashMap<>();
        Map<String,Integer> nextCounts=new HashMap<>();for(int i=0;i<standard.length();i++){JSONObject target=new JSONObject(standard.getJSONObject(i).toString());String key=target.optString("key");int ordinal=nextCounts.getOrDefault(key,0);nextCounts.put(key,ordinal+1);String id=key+":"+ordinal;JSONObject source=currentTiles.get(id),baseline=oldTiles.get(id);
            if(source!=null&&baseline!=null){java.util.Iterator<String> fields=source.keys();while(fields.hasNext()){String field=fields.next();if(field.equals("key")||field.equals("x")||field.equals("y")||field.equals("w")||field.equals("h"))continue;
                    if(field.equals("average_id")||!baseline.has(field)||!same(source.opt(field),baseline.opt(field)))target.put(field,source.get(field));}
                fields=baseline.keys();while(fields.hasNext()){String field=fields.next();if(!field.equals("key")&&!field.equals("x")&&!field.equals("y")&&!field.equals("w")&&!field.equals("h")&&!source.has(field))target.remove(field);}
            }
            result.put(target);counts.put(key,counts.getOrDefault(key,0)+1);
        }
        // A photo or personal text on a standard tile is user content, so do not discard it if its tile leaves the new default.
        Map<String,Integer> kept=new HashMap<>();for(int i=0;i<result.length();i++){String key=result.getJSONObject(i).optString("key");kept.put(key,kept.getOrDefault(key,0)+1);}
        for(Map.Entry<String,JSONObject> entry:currentTiles.entrySet()){String key=entry.getKey().substring(0,entry.getKey().lastIndexOf(':'));int ordinal=Integer.parseInt(entry.getKey().substring(entry.getKey().lastIndexOf(':')+1));if(ordinal<kept.getOrDefault(key,0))continue;JSONObject tile=entry.getValue();if(!tile.optString("image_data").isEmpty()||!tile.optString("free_text").isEmpty())return current;JSONObject baseline=oldTiles.get(entry.getKey());if(baseline!=null&&!same(tile.opt("caption"),baseline.opt("caption")))return current;}
        return result;
    }
    private static boolean same(Object a,Object b){return a==null?b==null:a.toString().equals(b==null?null:b.toString());}
    private static boolean light(String value){try{int c=android.graphics.Color.parseColor(value);return android.graphics.Color.red(c)*.2126+android.graphics.Color.green(c)*.7152+android.graphics.Color.blue(c)*.0722>150;}catch(Exception e){return false;}}
}
