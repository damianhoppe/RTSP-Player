package pl.huczeq.rtspplayer.data.typeconverters;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;

public class MapConverter {

    @TypeConverter
    public static String mapToString(Map<String, String> map) {
        Gson gson = new Gson();
        return gson.toJson(map);
    }

    @TypeConverter
    public static Map<String, String> mapFromString(String value) {
        Gson gson = new Gson();
        return gson.fromJson(value, new TypeToken<Map<String, String>>(){}.getType());
    }
}
