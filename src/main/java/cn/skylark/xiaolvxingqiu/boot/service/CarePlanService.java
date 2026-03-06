package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.CarePlanMapper;
import cn.skylark.xiaolvxingqiu.boot.mapper.CarePlanRuleMapper;
import cn.skylark.xiaolvxingqiu.boot.mapper.PlantMapper;
import cn.skylark.xiaolvxingqiu.boot.model.CarePlanConfigRequest;
import cn.skylark.xiaolvxingqiu.boot.model.CarePlanConfigResponse;
import cn.skylark.xiaolvxingqiu.boot.model.CarePlanEntity;
import cn.skylark.xiaolvxingqiu.boot.model.CarePlanRuleConfig;
import cn.skylark.xiaolvxingqiu.boot.model.CarePlanRuleEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CarePlanService {
    private static final Map<String, Integer> DEFAULT_INTERVAL_DAYS;

    static {
        Map<String, Integer> defaults = new HashMap<>();
        defaults.put("water", 5);
        defaults.put("fertilize", 21);
        defaults.put("loosen", 30);
        defaults.put("prune", 30);
        defaults.put("repot", 365);
        defaults.put("pest", 7);
        defaults.put("measure", 14);
        defaults.put("photo", 7);
        DEFAULT_INTERVAL_DAYS = defaults;
    }

    private final CarePlanMapper carePlanMapper;
    private final CarePlanRuleMapper carePlanRuleMapper;
    private final PlantMapper plantMapper;
    private final CareMetaService careMetaService;

    public CarePlanService(CarePlanMapper carePlanMapper,
                           CarePlanRuleMapper carePlanRuleMapper,
                           PlantMapper plantMapper,
                           CareMetaService careMetaService) {
        this.carePlanMapper = carePlanMapper;
        this.carePlanRuleMapper = carePlanRuleMapper;
        this.plantMapper = plantMapper;
        this.careMetaService = careMetaService;
    }

    public CarePlanConfigResponse getPlanConfig(Long userId, Long plantId) {
        ensurePlantExists(userId, plantId);
        CarePlanEntity plan = carePlanMapper.selectByUserIdAndPlantId(userId, plantId);
        if (plan == null) {
            return defaultConfig(plantId);
        }
        List<CarePlanRuleEntity> rules = carePlanRuleMapper.selectByPlanId(userId, plan.getId());
        CarePlanConfigResponse resp = new CarePlanConfigResponse();
        resp.setPlantId(plantId);
        resp.setEnabled(Boolean.TRUE.equals(plan.getEnabled()));
        resp.setSeasonalMode(Boolean.TRUE.equals(plan.getSeasonalMode()));
        List<CarePlanRuleConfig> configList = new ArrayList<>();
        for (CarePlanRuleEntity rule : rules) {
            CarePlanRuleConfig config = new CarePlanRuleConfig();
            config.setActivityType(rule.getActivityType());
            config.setSeason(rule.getSeason());
            config.setEnabled(Boolean.TRUE.equals(rule.getEnabled()));
            config.setIntervalDays(rule.getIntervalDays());
            configList.add(config);
        }
        resp.setRules(configList);
        return resp;
    }

    @Transactional
    public CarePlanConfigResponse savePlanConfig(Long userId, Long plantId, CarePlanConfigRequest request) {
        ensurePlantExists(userId, plantId);
        CarePlanEntity plan = carePlanMapper.selectByUserIdAndPlantId(userId, plantId);
        if (plan == null) {
            plan = new CarePlanEntity();
            plan.setUserId(userId);
            plan.setPlantId(plantId);
            plan.setEnabled(request.getEnabled() == null ? true : request.getEnabled());
            plan.setSeasonalMode(request.getSeasonalMode() == null ? false : request.getSeasonalMode());
            carePlanMapper.insert(plan);
        } else {
            plan.setEnabled(request.getEnabled() == null ? plan.getEnabled() : request.getEnabled());
            plan.setSeasonalMode(request.getSeasonalMode() == null ? plan.getSeasonalMode() : request.getSeasonalMode());
            carePlanMapper.updateConfig(plan);
            carePlanRuleMapper.softDeleteByPlanId(userId, plan.getId());
        }

        List<CarePlanRuleConfig> inputRules = normalizeInputRules(request, Boolean.TRUE.equals(plan.getSeasonalMode()));
        LocalDate initDate = LocalDate.now();
        for (CarePlanRuleConfig config : inputRules) {
            CarePlanRuleEntity entity = new CarePlanRuleEntity();
            entity.setUserId(userId);
            entity.setPlanId(plan.getId());
            entity.setActivityType(config.getActivityType());
            entity.setSeason(config.getSeason());
            entity.setEnabled(config.getEnabled() == null ? false : config.getEnabled());
            entity.setIntervalDays(normalizeIntervalDays(config.getActivityType(), config.getIntervalDays()));
            entity.setNextDueDate(Boolean.TRUE.equals(entity.getEnabled()) ? initDate : null);
            carePlanRuleMapper.insert(entity);
        }

        return getPlanConfig(userId, plantId);
    }

    private void ensurePlantExists(Long userId, Long plantId) {
        if (plantMapper.selectByUserIdAndId(userId, plantId) == null) {
            throw new IllegalArgumentException("plant not found: " + plantId);
        }
    }

    private CarePlanConfigResponse defaultConfig(Long plantId) {
        CarePlanConfigResponse resp = new CarePlanConfigResponse();
        resp.setPlantId(plantId);
        resp.setEnabled(true);
        resp.setSeasonalMode(false);
        List<CarePlanRuleConfig> rules = new ArrayList<>();
        for (String type : careMetaService.allActivityTypes()) {
            CarePlanRuleConfig config = new CarePlanRuleConfig();
            config.setActivityType(type);
            config.setSeason("ALL");
            config.setEnabled(false);
            config.setIntervalDays(defaultIntervalDays(type));
            rules.add(config);
        }
        resp.setRules(rules);
        return resp;
    }

    private List<CarePlanRuleConfig> normalizeInputRules(CarePlanConfigRequest request, boolean seasonalMode) {
        if (request.getRules() == null || request.getRules().isEmpty()) {
            return defaultConfig(0L).getRules();
        }
        List<CarePlanRuleConfig> result = new ArrayList<>();
        if (!seasonalMode) {
            Map<String, CarePlanRuleConfig> merged = new HashMap<>();
            for (CarePlanRuleConfig item : request.getRules()) {
                if (item.getActivityType() == null) continue;
                CarePlanRuleConfig normalized = new CarePlanRuleConfig();
                normalized.setActivityType(item.getActivityType());
                normalized.setSeason("ALL");
                normalized.setEnabled(item.getEnabled() == null ? false : item.getEnabled());
                normalized.setIntervalDays(normalizeIntervalDays(item.getActivityType(), item.getIntervalDays()));
                merged.put(item.getActivityType(), normalized);
            }
            for (String type : careMetaService.allActivityTypes()) {
                result.add(merged.getOrDefault(type, defaultRule(type, "ALL")));
            }
            return result;
        }

        Map<String, CarePlanRuleConfig> merged = new HashMap<>();
        for (CarePlanRuleConfig item : request.getRules()) {
            if (item.getActivityType() == null || item.getSeason() == null) continue;
            CarePlanRuleConfig normalized = new CarePlanRuleConfig();
            normalized.setActivityType(item.getActivityType());
            normalized.setSeason(item.getSeason().toUpperCase());
            normalized.setEnabled(item.getEnabled() == null ? false : item.getEnabled());
            normalized.setIntervalDays(normalizeIntervalDays(item.getActivityType(), item.getIntervalDays()));
            merged.put(item.getActivityType() + "_" + normalized.getSeason(), normalized);
        }
        List<String> seasons = new ArrayList<>();
        seasons.add("SPRING");
        seasons.add("SUMMER");
        seasons.add("AUTUMN");
        seasons.add("WINTER");
        for (String type : careMetaService.allActivityTypes()) {
            for (String season : seasons) {
                result.add(merged.getOrDefault(type + "_" + season, defaultRule(type, season)));
            }
        }
        return result;
    }

    private CarePlanRuleConfig defaultRule(String type, String season) {
        CarePlanRuleConfig config = new CarePlanRuleConfig();
        config.setActivityType(type);
        config.setSeason(season);
        config.setEnabled(false);
        config.setIntervalDays(defaultIntervalDays(type));
        return config;
    }

    private int normalizeIntervalDays(String activityType, Integer intervalDays) {
        if (intervalDays != null && intervalDays > 0) {
            return intervalDays;
        }
        return defaultIntervalDays(activityType);
    }

    private int defaultIntervalDays(String activityType) {
        Integer value = DEFAULT_INTERVAL_DAYS.get(activityType);
        return value == null ? 3 : value;
    }
}
