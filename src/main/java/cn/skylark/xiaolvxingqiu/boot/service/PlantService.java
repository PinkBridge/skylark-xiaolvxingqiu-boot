package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.PlantMapper;
import cn.skylark.xiaolvxingqiu.boot.model.PlantFocusRequest;
import cn.skylark.xiaolvxingqiu.boot.model.Plant;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlantService {

    private final PlantMapper plantMapper;
    private final GardenService gardenService;

    public PlantService(PlantMapper plantMapper, GardenService gardenService) {
        this.plantMapper = plantMapper;
        this.gardenService = gardenService;
    }

    public List<Plant> listPlants(Long userId, String filter) {
        return listPlants(userId, filter, null);
    }

    public List<Plant> listPlants(Long userId, String filter, Long gardenId) {
        List<Plant> plants;
        if ("healthy".equalsIgnoreCase(filter) || "normal".equalsIgnoreCase(filter)) {
            plants = plantMapper.selectHealthyByUserId(userId);
        } else if ("abnormal".equalsIgnoreCase(filter)) {
            plants = plantMapper.selectAbnormalByUserId(userId);
        } else if ("favorite".equalsIgnoreCase(filter)) {
            plants = plantMapper.selectFavoriteByUserId(userId);
        } else if ("focus".equalsIgnoreCase(filter)) {
            plants = plantMapper.selectFocusedByUserId(userId);
        } else if ("todo".equalsIgnoreCase(filter)) {
            // todo filter has no dedicated table yet, fallback to abnormal as pending-care candidates
            plants = plantMapper.selectAbnormalByUserId(userId);
        } else {
            plants = plantMapper.selectByUserId(userId);
        }
        if (gardenId != null) {
            plants.removeIf(plant -> !gardenId.equals(plant.getGardenId()));
        }
        for (Plant plant : plants) {
            normalizePlantForView(plant);
        }
        return plants;
    }

    public Plant getPlant(Long userId, Long id) {
        Plant plant = plantMapper.selectByUserIdAndId(userId, id);
        if (plant == null) {
            throw new IllegalArgumentException("plant not found: " + id);
        }
        normalizePlantForView(plant);
        return plant;
    }

    public Plant createPlant(Long userId, Plant plant) {
        // Currently attach newly created plants to user's default garden
        Long defaultGardenId = gardenService.getDefaultGarden(userId).getId();
        plant.setUserId(userId);
        plant.setGardenId(defaultGardenId);
        if (plant.getHealthStatus() == null || plant.getHealthStatus().trim().isEmpty()) {
            plant.setHealthStatus("healthy");
        }
        if (plant.getFavorite() == null) {
            plant.setFavorite(false);
        }
        plant.setHealthStatus(normalizeHealthStatusCode(plant.getHealthStatus()));
        plantMapper.insert(plant);
        plantMapper.insertStatusLog(userId, plant.getId(), plant.getHealthStatus(), resolveInitialStatusTime(plant.getPlantingDate()));
        return getPlant(userId, plant.getId());
    }

    public Plant updatePlant(Long userId, Long id, Plant request) {
        Plant old = plantMapper.selectByUserIdAndId(userId, id);
        if (old == null) {
            throw new IllegalArgumentException("plant not found: " + id);
        }
        old.setName(request.getName());
        old.setSpecies(request.getSpecies());
        old.setImage(request.getImage());
        old.setCultivationType(request.getCultivationType());
        old.setPlantingDate(request.getPlantingDate());
        old.setNote(request.getNote());
        String previousStatus = normalizeHealthStatusCode(old.getHealthStatus());
        String nextStatus = request.getHealthStatus() == null
                ? previousStatus
                : normalizeHealthStatusCode(request.getHealthStatus());
        old.setHealthStatus(nextStatus);
        old.setFavorite(request.getFavorite() == null ? old.getFavorite() : request.getFavorite());
        // Defensive assignment: ensure update WHERE clause always uses resolved request userId/id.
        old.setUserId(userId);
        old.setId(id);
        int rows = plantMapper.updateByUserIdAndId(old);
        if (rows == 0) {
            // MySQL may return 0 when submitted values are effectively unchanged.
            // Re-check existence to avoid misclassifying "no-op update" as not found.
            Plant exists = plantMapper.selectByUserIdAndId(userId, id);
            if (exists == null) {
                throw new IllegalArgumentException("plant not found: " + id);
            }
        }
        if (!previousStatus.equals(nextStatus)) {
            plantMapper.insertStatusLog(userId, id, nextStatus, LocalDateTime.now());
        }
        return getPlant(userId, id);
    }

    public void deletePlant(Long userId, Long id) {
        int rows = plantMapper.softDelete(userId, id);
        if (rows == 0) {
            throw new IllegalArgumentException("plant not found: " + id);
        }
        plantMapper.clearFocus(userId, id);
        plantMapper.clearStatusLogs(userId, id);
    }

    public Plant setFocus(Long userId, Long id, PlantFocusRequest request) {
        Plant target = plantMapper.selectByUserIdAndId(userId, id);
        if (target == null) {
            throw new IllegalArgumentException("plant not found: " + id);
        }
        plantMapper.upsertFocus(userId, id, request.getReason(), request.getPhotoUrl());
        return getPlant(userId, id);
    }

    public Plant clearFocus(Long userId, Long id) {
        Plant target = plantMapper.selectByUserIdAndId(userId, id);
        if (target == null) {
            throw new IllegalArgumentException("plant not found: " + id);
        }
        plantMapper.clearFocus(userId, id);
        return getPlant(userId, id);
    }

    private void normalizePlantForView(Plant plant) {
        String healthStatus = plant.getHealthStatus() == null ? "healthy" : plant.getHealthStatus();
        plant.setHealthStatus(healthStatus);
        plant.setStatusLabel(toStatusLabel(healthStatus));
        plant.setDays(calcDays(plant.getPlantingDate()));
        plant.setFocused(Boolean.TRUE.equals(plant.getFocused()));
        if (plant.getTodayCareTasks() == null) {
            plant.setTodayCareTasks(new ArrayList<String>());
        }
    }

    private String toStatusLabel(String healthStatus) {
        if ("sick".equalsIgnoreCase(healthStatus)) return "生病";
        if ("dormant".equalsIgnoreCase(healthStatus)) return "休眠";
        if ("dead".equalsIgnoreCase(healthStatus)) return "已死亡";
        if ("gifted".equalsIgnoreCase(healthStatus)) return "已送人";
        if ("sold".equalsIgnoreCase(healthStatus)) return "已售出";
        if ("生病".equals(healthStatus)) return "生病";
        if ("休眠".equals(healthStatus)) return "休眠";
        if ("已死亡".equals(healthStatus) || "死亡".equals(healthStatus)) return "已死亡";
        if ("已送人".equals(healthStatus)) return "已送人";
        if ("已售出".equals(healthStatus)) return "已售出";
        return "健康";
    }

    private Integer calcDays(String plantingDate) {
        try {
            if (plantingDate == null || plantingDate.trim().isEmpty()) return 1;
            long days = ChronoUnit.DAYS.between(LocalDate.parse(plantingDate), LocalDate.now()) + 1;
            return (int) Math.max(days, 1);
        } catch (Exception e) {
            return 1;
        }
    }

    private String normalizeHealthStatusCode(String healthStatus) {
        if (healthStatus == null || healthStatus.trim().isEmpty()) return "healthy";
        String raw = healthStatus.trim();
        if ("健康".equals(raw) || "healthy".equalsIgnoreCase(raw)) return "healthy";
        if ("生病".equals(raw) || "sick".equalsIgnoreCase(raw)) return "sick";
        if ("休眠".equals(raw) || "dormant".equalsIgnoreCase(raw)) return "dormant";
        if ("死亡".equals(raw) || "已死亡".equals(raw) || "dead".equalsIgnoreCase(raw)) return "dead";
        if ("已送人".equals(raw) || "gifted".equalsIgnoreCase(raw)) return "gifted";
        if ("已售出".equals(raw) || "sold".equalsIgnoreCase(raw)) return "sold";
        return "healthy";
    }

    private LocalDateTime resolveInitialStatusTime(String plantingDate) {
        try {
            if (plantingDate == null || plantingDate.trim().isEmpty()) {
                return LocalDateTime.now();
            }
            return LocalDate.parse(plantingDate).atStartOfDay();
        } catch (Exception ignore) {
            return LocalDateTime.now();
        }
    }
}
