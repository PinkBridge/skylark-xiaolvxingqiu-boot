package cn.skylark.xiaolvxingqiu.boot.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CareMetaService {

    private final Map<String, String> typeLabelMap = new HashMap<>();
    private final Map<String, String> typeIconMap = new HashMap<>();

    public CareMetaService() {
        typeLabelMap.put("water", "浇水");
        typeLabelMap.put("fertilize", "施肥");
        typeLabelMap.put("loosen", "松土");
        typeLabelMap.put("prune", "修剪");
        typeLabelMap.put("repot", "换盆");
        typeLabelMap.put("pest", "病虫害");
        typeLabelMap.put("measure", "测量");
        typeLabelMap.put("photo", "拍照");

        typeIconMap.put("water", "/static/icon/water.png");
        typeIconMap.put("fertilize", "/static/icon/fertilize.png");
        typeIconMap.put("loosen", "/static/icon/loosen.png");
        typeIconMap.put("prune", "/static/icon/prune.png");
        typeIconMap.put("repot", "/static/icon/repot.png");
        typeIconMap.put("pest", "/static/icon/pest.png");
        typeIconMap.put("measure", "/static/icon/measure.png");
        typeIconMap.put("photo", "/static/icon/photo.png");
    }

    public List<String> allActivityTypes() {
        return Arrays.asList("water", "fertilize", "loosen", "prune", "repot", "pest", "measure", "photo");
    }

    public String label(String type) {
        return typeLabelMap.getOrDefault(type, type);
    }

    public String icon(String type) {
        return typeIconMap.getOrDefault(type, "/static/icon/water.png");
    }

    public String seasonOf(LocalDate date) {
        int month = date.getMonthValue();
        if (month >= 3 && month <= 5) return "SPRING";
        if (month >= 6 && month <= 8) return "SUMMER";
        if (month >= 9 && month <= 11) return "AUTUMN";
        return "WINTER";
    }
}
