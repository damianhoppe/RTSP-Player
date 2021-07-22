package pl.huczeq.rtspplayer.common;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import pl.huczeq.rtspplayer.data.expression.VariableModel;

public class Utils {
    private static final Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    private static final String key = "key";
    private static final String value = "value";

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return numericPattern.matcher(strNum).matches();
    }

    public static boolean isSystemOrientationLocked(Context context) {
        try {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION) != 1;
        } catch (Settings.SettingNotFoundException e) {
            return true;
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T1, T2> HashMap<T1, T2> jsonArray2HashMap(JSONArray jsonArray) {
        Log.d("Test", "JsonArray: " + jsonArray.toString());
        HashMap<T1, T2> data = new HashMap<>();
        JSONObject jsonObject;
        T1 key;
        T2 value;
        if(jsonArray == null)
            return data;
        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            Log.d("Test", "jsonObject: " + jsonObject.toString());
            try {
                key = (T1) jsonObject.get(Utils.key);
            } catch (JSONException | ClassCastException e) {
                e.printStackTrace();
                continue;
            }
            try {
                value = (T2) jsonObject.get(Utils.value);
            } catch (JSONException | ClassCastException e) {
                e.printStackTrace();
                continue;
            }
            data.put(key, value);
        }
        return data;
    }

    public static <T1,T2> JSONArray hashMap2JsonArray(HashMap<T1, T2> data) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        if(data == null)
            return jsonArray;
        for(Map.Entry<T1, T2> entry : data.entrySet()) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put(Utils.key, entry.getKey());
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            try {
                jsonObject.put(Utils.value, entry.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }


    public static List<VariableModel> jsonArray2Variables(JSONArray jsonArray) {
        List<VariableModel> data = new ArrayList<>();
        JSONObject jsonObject;
        String key;
        String value;
        if(jsonArray == null)
            return data;
        for(int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            try {
                key = jsonObject.getString(Utils.key);
            } catch (JSONException | ClassCastException e) {
                e.printStackTrace();
                continue;
            }
            try {
                value = jsonObject.getString(Utils.value);
            } catch (JSONException | ClassCastException e) {
                e.printStackTrace();
                continue;
            }
            data.add(new VariableModel(key, value));
        }
        return data;
    }

    public static JSONArray variables2JsonArray(List<VariableModel> data) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        if(data == null)
            return jsonArray;
        for(VariableModel variable : data) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put(Utils.key, variable.getName());
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            try {
                jsonObject.put(Utils.value, variable.getValue());
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
}
