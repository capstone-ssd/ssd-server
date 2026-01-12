package or.hyu.ssd.global.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class AiResponseUtil {


    // 문자열을 JSON으로 변환하는 알고리즘
    public static String extractJsonArray(String raw) {
        if (raw == null) return "[]";
        String s = raw.trim();
        if (s.startsWith("```")) {
            int first = s.indexOf('\n');
            if (first > -1) {
                s = s.substring(first + 1);
            }
            int lastFence = s.lastIndexOf("```");
            if (lastFence > -1) {
                s = s.substring(0, lastFence).trim();
            }
        }
        int l = s.indexOf('[');
        int r = s.lastIndexOf(']');
        if (l >= 0 && r > l) {
            return s.substring(l, r + 1);
        }
        return s;
    }

    public static List<String> parseStringArray(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}

