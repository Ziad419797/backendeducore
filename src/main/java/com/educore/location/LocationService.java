package com.educore.location;


import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Service
public class LocationService {
    private Map<String, List<String>> locations;

    public LocationService() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("locations.json").getInputStream();
            locations = mapper.readValue(is, new TypeReference<Map<String, List<String>>>() {});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Cacheable("governorates")
    public List<String> getGovernorates() {
        return locations.keySet().stream().toList();
    }
    @Cacheable(value = "areas", key = "#governorate")

    public List<String> getAreas(String governorate) {
        return locations.getOrDefault(governorate, List.of());
    }
}
