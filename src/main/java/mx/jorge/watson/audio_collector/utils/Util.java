package mx.jorge.watson.audio_collector.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.System.arraycopy;
import static mx.jorge.watson.audio_collector.Constants.DEFAULT_RECORD_TIME_MILLIS;
import static mx.jorge.watson.audio_collector.Constants.GSON;

/**
 * @author huerta.jorge at gmail.com
 */
public final class Util {

    private Util() {}

    public static Map<String, Object> buildResponse(String transcript) {
        Map<String, Object> alternative = new HashMap<>();
        alternative.put("transcript", transcript);

        List<Map<String, Object>> alternatives = new ArrayList<>();
        alternatives.add(alternative);

        Map<String, Object> result = new HashMap<>();
        result.put("alternatives", alternatives);
        result.put("final", true);

        List<Map<String, Object>> results = new ArrayList<>();
        results.add(result);

        Map<String, Object> response = new HashMap<>();
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
        arraycopy(a, 0, c, 0, a.length);
        arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static long getTimeToRecInMillis(String millis) {
        try {
            return Long.parseLong(millis);
        } catch (NumberFormatException nfe) {
            return DEFAULT_RECORD_TIME_MILLIS;
        }
    }
}
