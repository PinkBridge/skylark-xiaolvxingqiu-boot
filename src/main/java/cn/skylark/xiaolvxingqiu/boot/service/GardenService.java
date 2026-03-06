package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.model.Garden;
import cn.skylark.xiaolvxingqiu.boot.model.GardenUpsertRequest;
import cn.skylark.xiaolvxingqiu.boot.mapper.GardenMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GardenService {

    private final GardenMapper gardenMapper;

    public GardenService(GardenMapper gardenMapper) {
        this.gardenMapper = gardenMapper;
    }

    public List<Garden> listByUserId(Long userId) {
        return gardenMapper.selectByUserId(userId);
    }

    public Garden getById(Long userId, Long gardenId) {
        Garden garden = gardenMapper.selectByUserIdAndId(userId, gardenId);
        if (garden == null) {
            throw new IllegalArgumentException("garden not found: " + gardenId);
        }
        return garden;
    }

    public Garden getDefaultGarden(Long userId) {
        Garden defaultGarden = gardenMapper.selectDefaultByUserId(userId);
        if (defaultGarden != null) {
            return defaultGarden;
        }
        Garden firstGarden = gardenMapper.selectFirstByUserId(userId);
        if (firstGarden == null) {
            throw new IllegalArgumentException("garden not found");
        }
        return firstGarden;
    }

    @Transactional
    public Garden create(Long userId, GardenUpsertRequest request) {
        Garden garden = new Garden();
        garden.setUserId(userId);
        garden.setName(request.getName());
        garden.setEstablishedDate(request.getEstablishedDate());
        garden.setThumbUrl(request.getThumbUrl());
        garden.setCoverUrl(request.getCoverUrl());
        garden.setDescription(request.getDescription());
        Integer count = gardenMapper.countByUserId(userId);
        garden.setIsDefault(count == null || count == 0);
        gardenMapper.insert(garden);
        return getById(userId, garden.getId());
    }

    public Garden update(Long userId, Long gardenId, GardenUpsertRequest request) {
        Garden garden = new Garden();
        garden.setId(gardenId);
        garden.setUserId(userId);
        garden.setName(request.getName());
        garden.setEstablishedDate(request.getEstablishedDate());
        garden.setThumbUrl(request.getThumbUrl());
        garden.setCoverUrl(request.getCoverUrl());
        garden.setDescription(request.getDescription());
        int rows = gardenMapper.updateByUserIdAndId(garden);
        if (rows == 0) {
            throw new IllegalArgumentException("garden not found: " + gardenId);
        }
        return getById(userId, gardenId);
    }

    @Transactional
    public void delete(Long userId, Long gardenId) {
        Garden current = getById(userId, gardenId);
        int rows = gardenMapper.softDelete(userId, gardenId);
        if (rows == 0) {
            throw new IllegalArgumentException("garden not found: " + gardenId);
        }
        if (Boolean.TRUE.equals(current.getIsDefault())) {
            List<Garden> remain = listByUserId(userId);
            if (!remain.isEmpty()) {
                gardenMapper.setDefault(userId, remain.get(0).getId());
            }
        }
    }

    @Transactional
    public void setDefault(Long userId, Long gardenId) {
        getById(userId, gardenId);
        gardenMapper.clearDefaultByUserId(userId);
        gardenMapper.setDefault(userId, gardenId);
    }
}
