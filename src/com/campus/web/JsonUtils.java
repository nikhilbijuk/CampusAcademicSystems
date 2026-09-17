package com.campus.web;

import java.util.HashMap;
import java.util.Map;

/**
 * Robust, zero-dependency JSON utility for Campus Academic System.
 * Correctly parses JSON objects with escaped strings, commas inside quotes, unicode escapes,
 * numbers, and booleans without external libraries.
 */
public class JsonUtils {

    /**
     * Parses a flat JSON object string into a key-value Map.
     * Handles quoted strings with escape sequences (\", \\, \n, \t, etc.),
     * unicode escape sequences (such as \u0040), commas inside quoted strings, numbers, and booleans.
     */
    public static Map<String, String> parseFlatJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null) {
            return map;
        }
        String trimmed = json.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            return map;
        }

        int i = 1; // skip leading '{'
        int len = trimmed.length() - 1; // exclude trailing '}'

        while (i < len) {
            // skip whitespace and commas
            while (i < len && (Character.isWhitespace(trimmed.charAt(i)) || trimmed.charAt(i) == ',')) {
                i++;
            }
            if (i >= len) break;

            // expect string key
            if (trimmed.charAt(i) != '"') {
                i++;
                continue;
            }

            ParsedString parsedKey = readJsonString(trimmed, i);
            if (parsedKey == null) break;
            String key = parsedKey.value;
            i = parsedKey.nextIndex;

            // skip whitespace
            while (i < len && Character.isWhitespace(trimmed.charAt(i))) {
                i++;
            }

            // expect ':'
            if (i >= len || trimmed.charAt(i) != ':') {
                break;
            }
            i++; // skip ':'

            // skip whitespace
            while (i < len && Character.isWhitespace(trimmed.charAt(i))) {
                i++;
            }
            if (i >= len) break;

            // Parse value
            char c = trimmed.charAt(i);
            if (c == '"') {
                ParsedString parsedVal = readJsonString(trimmed, i);
                if (parsedVal == null) break;
                map.put(key, parsedVal.value);
                i = parsedVal.nextIndex;
            } else if (c == '{' || c == '[') {
                // Nested object or array - skip balanced delimiters
                char open = c;
                char close = (c == '{') ? '}' : ']';
                int depth = 0;
                boolean inString = false;
                int startVal = i;
                while (i < len) {
                    char ch = trimmed.charAt(i);
                    if (inString) {
                        if (ch == '\\') {
                            i += 2;
                            continue;
                        } else if (ch == '"') {
                            inString = false;
                        }
                    } else {
                        if (ch == '"') {
                            inString = true;
                        } else if (ch == open) {
                            depth++;
                        } else if (ch == close) {
                            depth--;
                            if (depth == 0) {
                                i++;
                                break;
                            }
                        }
                    }
                    i++;
                }
                map.put(key, trimmed.substring(startVal, Math.min(i, trimmed.length())));
            } else {
                // Primitive: number, boolean, or null
                int startVal = i;
                while (i < len && trimmed.charAt(i) != ',' && trimmed.charAt(i) != '}') {
                    i++;
                }
                String val = trimmed.substring(startVal, i).trim();
                map.put(key, val);
            }
        }

        return map;
    }

    private static class ParsedString {
        final String value;
        final int nextIndex;
        ParsedString(String value, int nextIndex) {
            this.value = value;
            this.nextIndex = nextIndex;
        }
    }

    private static ParsedString readJsonString(String s, int startIndex) {
        if (startIndex >= s.length() || s.charAt(startIndex) != '"') return null;
        StringBuilder sb = new StringBuilder();
        int i = startIndex + 1;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '"') {
                return new ParsedString(sb.toString(), i + 1);
            } else if (c == '\\') {
                i++;
                if (i >= s.length()) break;
                char esc = s.charAt(i);
                switch (esc) {
                    case '"':  sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'u':
                        if (i + 4 < s.length()) {
                            try {
                                int hexVal = Integer.parseInt(s.substring(i + 1, i + 5), 16);
                                sb.append((char) hexVal);
                                i += 4;
                            } catch (NumberFormatException nfe) {
                                sb.append("\\u");
                            }
                        } else {
                            sb.append("\\u");
                        }
                        break;
                    default:
                        sb.append(esc);
                        break;
                }
            } else {
                sb.append(c);
            }
            i++;
        }
        return null;
    }

    /**
     * Escapes special characters for JSON string output.
     */
    public static String escapeJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b"); break;
                case '\f': sb.append("\\f"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < ' ') {
                        String hex = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(hex.substring(hex.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}
