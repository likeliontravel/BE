package org.example.be.Tourapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class TourApiParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<Map<String, Object>> parseItems(String json) {
        try {
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            Map<String, Object> response = (Map<String, Object>) map.get("response");
            Map<String, Object> body = (Map<String, Object>) response.get("body");
            Map<String, Object> items = (Map<String, Object>) body.get("items");

            if (items == null || !(items.get("item") instanceof List)) {
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) items.get("item");
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
