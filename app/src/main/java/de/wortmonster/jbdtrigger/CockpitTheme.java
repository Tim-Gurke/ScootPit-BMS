package de.wortmonster.jbdtrigger;

import android.content.SharedPreferences;
import android.graphics.Color;
import org.json.*;

final class CockpitTheme {
    static int color(SharedPreferences p,String key,String fallback){try{return Color.parseColor(p.getString(key,fallback));}catch(Exception e){return Color.parseColor(fallback);}}
    static boolean light(SharedPreferences p){int c=color(p,"app_background","#0B1015");return Color.red(c)*.2126+Color.green(c)*.7152+Color.blue(c)*.0722>150;}
    static int foreground(SharedPreferences p){return color(p,"tile_text",light(p)?"#172B40":"#FFFFFF");}
    static String scale(SharedPreferences p){return p.getString("scale_color",light(p)?"#B9CEDB":"#34434D");}
    static int readableTileText(SharedPreferences p,int requested,int tileBackground){
        int[] backgrounds;
        if(Color.alpha(tileBackground)==255)backgrounds=new int[]{tileBackground};
        else if(p.getBoolean("background_gradient_enabled",false))backgrounds=new int[]{
                composite(tileBackground,color(p,"background_gradient_start",light(p)?"#FFFFFF":"#14232D")),
                composite(tileBackground,color(p,"background_gradient_end",light(p)?"#DCECF5":"#071018"))};
        else backgrounds=new int[]{composite(tileBackground,color(p,"app_background",light(p)?"#F7FAFD":"#071018"))};
        if(minContrast(requested,backgrounds)>=4.5)return requested;
        int black=Color.BLACK,white=Color.WHITE;
        return minContrast(black,backgrounds)>=minContrast(white,backgrounds)?black:white;
    }
    private static int composite(int foreground,int background){
        int alpha=Color.alpha(foreground),inverse=255-alpha;
        return Color.rgb((Color.red(foreground)*alpha+Color.red(background)*inverse)/255,
                (Color.green(foreground)*alpha+Color.green(background)*inverse)/255,
                (Color.blue(foreground)*alpha+Color.blue(background)*inverse)/255);
    }
    private static double minContrast(int foreground,int[] backgrounds){
        double result=Double.MAX_VALUE;
        for(int background:backgrounds){
            int visible=composite(foreground,background);
            double a=luminance(visible),b=luminance(background);
            result=Math.min(result,(Math.max(a,b)+.05)/(Math.min(a,b)+.05));
        }
        return result;
    }
    private static double luminance(int color){
        double r=linear(Color.red(color)/255d),g=linear(Color.green(color)/255d),b=linear(Color.blue(color)/255d);
        return .2126*r+.7152*g+.0722*b;
    }
    private static double linear(double value){return value<=.04045?value/12.92:Math.pow((value+.055)/1.055,2.4);}
    static final String[] COLOR_FIELDS={"background","text","instrument_color","scale_color","icon_color","border_color"};
    private static String hex(int color){return String.format(java.util.Locale.ROOT,"#%08X",color);}
    private static String lightSymbol(String value){int color=Color.parseColor(value);for(int n=0;n<40&&minContrast(color,new int[]{0xffffffff,0xffdcecf5})<3;n++)color=ScaleColorRange.blend(color,Color.BLACK,.05);return hex(color);}
    static void tileColors(JSONObject t,boolean light)throws Exception{
        String accent=light?"#1678B8":"#FF981F",tile=light?"#FFFFFF":"#0C1720",text=light?"#172B40":"#FFFFFF",scale=light?"#B9CEDB":"#34434D";
        JSONObject dark=t.optJSONObject("dark_colors");
        if(!light&&dark!=null){for(String field:COLOR_FIELDS)if(dark.has(field))t.put(field,dark.get(field));t.remove("dark_colors");t.put("custom_colors",true);return;}
        if(light&&dark==null){dark=new JSONObject();for(String field:COLOR_FIELDS)if(t.has(field))dark.put(field,t.get(field));t.put("dark_colors",dark);}
        boolean transparent=Color.alpha(Color.parseColor(t.optString("background",tile)))==0;
        String icon=light?dark.optString("icon_color",hex(CockpitSymbols.defaultColor(t.optString("key"),false))):t.optString("icon_color",hex(CockpitSymbols.defaultColor(t.optString("key"),false)));
        String instrument=light?dark.optString("instrument_color","#FF981F"):t.optString("instrument_color",accent);
        t.put("background",transparent?"#00000000":tile).put("text",text).put("instrument_color",light?(instrument.equalsIgnoreCase("#FF981F")?accent:lightSymbol(instrument)):instrument).put("icon_color",light?lightSymbol(icon):icon).put("scale_color",scale).put("custom_colors",true);
    }
    static void apply(SharedPreferences p,boolean light,boolean replaceTiles)throws Exception{
        String accent=light?"#1678B8":"#FF981F",background=light?"#F7FAFD":"#071018",tile=light?"#FFFFFF":"#0C1720",text=light?"#172B40":"#FFFFFF",scale=light?"#B9CEDB":"#34434D";
        SharedPreferences.Editor e=p.edit().putString("app_background",background).putString("accent_color",accent).putString("header_color",accent).putString("tile_background",tile).putString("tile_text",text).putString("scale_color",scale).putString("outline_color",light?"#BBD7E5":"#28647A").putBoolean("background_gradient_enabled",true).putString("background_gradient_start",light?"#FFFFFF":"#14232D").putString("background_gradient_end",light?"#DCECF5":"#071018");
        if(replaceTiles){for(String key:new String[]{"cockpit_board","cockpit_board_landscape"}){
            JSONArray tiles=CockpitBoard.load(p,key);for(int i=0;i<tiles.length();i++)tileColors(tiles.getJSONObject(i),light);
            CockpitBoard.validate(tiles);e.putString(key,tiles.toString());
        }}e.apply();
    }
}

