package mx.jorge.watson.audio_collector.utils;

import com.google.gson.Gson;
import mx.jorge.watson.audio_collector.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huerta.jorge at gmail.com
 */
public final class Util {

    private Util() {}

    private static final Gson GSON = new Gson();

    public static Map<String, Object> buildResponse(String transcript) {
        Map<String, Object> alternative = new HashMap<>(1);
        alternative.put("transcript", transcript);

        List<Map<String, Object>> alternatives = new ArrayList<>(1);
        alternatives.add(alternative);

        Map<String, Object> result = new HashMap<>(2);
        result.put("alternatives", alternatives);
        result.put("final", true);

        List<Map<String, Object>> results = new ArrayList<>(1);
        results.add(result);

        Map<String, Object> response = new HashMap<>(2);
        response.put("results", results);
        response.put("result_index", 0);

        return response;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseJSONStringToMap(String json) {
        return GSON.fromJson(json, Map.class);
    }

    public static String parseMapToJSONString(Map<String, Object> map) {
        return GSON.toJson(map);
    }

    public static byte[] concatByteArray(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static long getTimeToRecInMillis(String millis) {
        try {
            return Long.parseLong(millis);
        } catch (NumberFormatException nfe) {
            return Constants.DEFAULT_RECORD_TIME_MILLIS;
        }
    }
}
