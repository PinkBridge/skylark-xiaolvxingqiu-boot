package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.Garden;
import cn.skylark.xiaolvxingqiu.boot.model.GardenInfo;
import cn.skylark.xiaolvxingqiu.boot.model.GardenUpsertRequest;
import cn.skylark.xiaolvxingqiu.boot.service.GardenService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GardenController {

    private final GardenService gardenService;
    private final UserContextProvider userContextProvider;

    public GardenController(GardenService gardenService, UserContextProvider userContextProvider) {
        this.gardenService = gardenService;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping("/api/gardens")
    public ApiResponse<List<Garden>> listGardens(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(gardenService.listByUserId(userId));
    }

    @PostMapping("/api/gardens")
    public ApiResponse<Garden> createGarden(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                            @Validated @RequestBody GardenUpsertRequest request) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(gardenService.create(userId, request));
    }

    @GetMapping("/api/gardens/{gardenId}")
    public ApiResponse<Garden> getGarden(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                         @PathVariable("gardenId") Long gardenId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(gardenService.getById(userId, gardenId));
    }

    @PutMapping("/api/gardens/{gardenId}")
    public ApiResponse<Garden> updateGarden(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                            @PathVariable("gardenId") Long gardenId,
                                            @Validated @RequestBody GardenUpsertRequest request) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(gardenService.update(userId, gardenId, request));
    }

    @DeleteMapping("/api/gardens/{gardenId}")
    public ApiResponse<Void> deleteGarden(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                          @PathVariable("gardenId") Long gardenId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        gardenService.delete(userId, gardenId);
        return ApiResponse.success();
    }

    @PutMapping("/api/gardens/{gardenId}/default")
    public ApiResponse<Void> setDefaultGarden(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                              @PathVariable("gardenId") Long gardenId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        gardenService.setDefault(userId, gardenId);
        return ApiResponse.success();
    }

    // Compatibility API for current miniapp /api/garden.
    @GetMapping("/api/garden")
    public ApiResponse<GardenInfo> getGardenInfo(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        Garden defaultGarden = gardenService.getDefaultGarden(userId);
        return ApiResponse.success(toLegacy(defaultGarden));
    }

    // Compatibility API for current miniapp /api/garden.
    @PutMapping("/api/garden")
    public ApiResponse<Void> updateGardenInfo(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                              @Validated @RequestBody GardenInfo gardenInfo) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        Garden defaultGarden = gardenService.getDefaultGarden(userId);
        GardenUpsertRequest request = new GardenUpsertRequest();
        request.setName(gardenInfo.getTitle());
        request.setEstablishedDate(gardenInfo.getSubTitle());
        request.setThumbUrl(gardenInfo.getThumb());
        request.setCoverUrl(gardenInfo.getImage());
        request.setDescription(gardenInfo.getDescription());
        gardenService.update(userId, defaultGarden.getId(), request);
        return ApiResponse.success();
    }

    private GardenInfo toLegacy(Garden garden) {
        GardenInfo legacy = new GardenInfo();
        legacy.setTitle(garden.getName());
        legacy.setSubTitle(garden.getEstablishedDate());
        legacy.setThumb(garden.getThumbUrl());
        legacy.setImage(garden.getCoverUrl());
        legacy.setDescription(garden.getDescription());
        return legacy;
    }
}
