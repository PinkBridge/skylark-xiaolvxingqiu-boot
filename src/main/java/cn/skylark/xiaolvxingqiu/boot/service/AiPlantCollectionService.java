package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.AiPlantCollectionMapper;
import cn.skylark.xiaolvxingqiu.boot.model.AiCollectionRequest;
import cn.skylark.xiaolvxingqiu.boot.model.AiPlantCollection;
import cn.skylark.xiaolvxingqiu.boot.model.Plant;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AiPlantCollectionService {

    private final AiPlantCollectionMapper aiPlantCollectionMapper;
    private final PlantService plantService;

    public AiPlantCollectionService(AiPlantCollectionMapper aiPlantCollectionMapper, PlantService plantService) {
        this.aiPlantCollectionMapper = aiPlantCollectionMapper;
        this.plantService = plantService;
    }

    public AiPlantCollection create(Long userId, AiCollectionRequest request) {
        AiPlantCollection item = new AiPlantCollection();
        item.setUserId(userId);
        item.setName(normalizeText(request.getName()));
        item.setDescription(normalizeText(request.getDescription()));
        item.setImageUrl(normalizeText(request.getImageUrl()));
        item.setRecognizedImageUrl(normalizeText(request.getRecognizedImageUrl()));
        item.setScore(request.getScore());
        item.setSource(normalizeText(request.getSource()).isEmpty() ? "baidu_ai" : normalizeText(request.getSource()));
        aiPlantCollectionMapper.insert(item);
        return aiPlantCollectionMapper.selectByUserIdAndId(userId, item.getId());
    }

    public List<AiPlantCollection> listByUserId(Long userId) {
        return aiPlantCollectionMapper.selectByUserId(userId);
    }

    public void delete(Long userId, Long id) {
        int rows = aiPlantCollectionMapper.softDelete(userId, id);
        if (rows == 0) {
            throw new IllegalArgumentException("collection not found: " + id);
        }
    }

    public Plant addToGardenByCollectionId(Long userId, Long id) {
        AiPlantCollection item = aiPlantCollectionMapper.selectByUserIdAndId(userId, id);
        if (item == null) {
            throw new IllegalArgumentException("collection not found: " + id);
        }
        return createPlantFromCollection(userId, item.getName(), item.getDescription(), item.getImageUrl(), item.getRecognizedImageUrl());
    }

    public Plant addToGardenByRecognition(Long userId, AiCollectionRequest request) {
        return createPlantFromCollection(
                userId,
                normalizeText(request.getName()),
                normalizeText(request.getDescription()),
                normalizeText(request.getImageUrl()),
                normalizeText(request.getRecognizedImageUrl())
        );
    }

    private Plant createPlantFromCollection(Long userId,
                                            String name,
                                            String description,
                                            String imageUrl,
                                            String recognizedImageUrl) {
        if (name.isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        Plant plant = new Plant();
        plant.setName(name);
        plant.setSpecies(name);
        String selectedImage = imageUrl.isEmpty() ? recognizedImageUrl : imageUrl;
        plant.setImage(selectedImage);
        plant.setNote(description);
        plant.setCultivationType("soil");
        plant.setPlantingDate(LocalDate.now().toString());
        plant.setHealthStatus("healthy");
        plant.setFavorite(false);
        return plantService.createPlant(userId, plant);
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }
}
