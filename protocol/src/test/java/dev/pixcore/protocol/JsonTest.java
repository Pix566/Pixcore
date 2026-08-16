package dev.pixcore.protocol;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonTest {
    @Test
    void writeAndParseObject() {
        Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("name", "Soul Blade");
        map.put("damage", 12);
        map.put("enabled", true);
        map.put("tags", List.of("a", "b"));

        String json = Json.write(map);
        Object parsed = Json.parse(json);

        Map<?, ?> result = assertInstanceOf(Map.class, parsed);
        assertEquals("Soul Blade", result.get("name"));
        assertEquals(12L, result.get("damage"));
        assertEquals(Boolean.TRUE, result.get("enabled"));
        assertEquals(List.of("a", "b"), result.get("tags"));
    }

    @Test
    void parseNestedStructures() {
        Object parsed = Json.parse("{\"a\":{\"b\":[1,2,{\"c\":\"x\"}]}}");
        Map<?, ?> root = assertInstanceOf(Map.class, parsed);
        Map<?, ?> a = assertInstanceOf(Map.class, root.get("a"));
        List<?> b = assertInstanceOf(List.class, a.get("b"));
        assertEquals(3, b.size());
        Map<?, ?> third = assertInstanceOf(Map.class, b.get(2));
        assertEquals("x", third.get("c"));
    }

    @Test
    void unicodeEscapes() {
        String json = Json.write(Map.of("text", "你好\n\"quoted\""));
        assertTrue(json.contains("\\n"));
        assertTrue(json.contains("\\\""));
        Map<?, ?> parsed = assertInstanceOf(Map.class, Json.parse(json));
        assertEquals("你好\n\"quoted\"", parsed.get("text"));
    }
}
