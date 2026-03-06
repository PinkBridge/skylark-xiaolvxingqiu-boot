package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.Plant;
import cn.skylark.xiaolvxingqiu.boot.model.PlantFocusRequest;
import cn.skylark.xiaolvxingqiu.boot.service.PlantService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plants")
public class PlantController {

    private final PlantService plantService;
    private final UserContextProvider userContextProvider;

    public PlantController(PlantService plantService, UserContextProvider userContextProvider) {
        this.plantService = plantService;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping
    public ApiResponse<List<Plant>> listPlants(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                               @RequestParam(value = "filter", required = false) String filter,
                                               @RequestParam(value = "gardenId", required = false) Long gardenId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(plantService.listPlants(userId, filter, gardenId));
    }

    @GetMapping("/{id}")
    public ApiResponse<Plant> getPlant(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                       @PathVariable("id") Long id) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(plantService.getPlant(userId, id));
    }

    @PostMapping
    public ApiResponse<Plant> createPlant(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                          @Validated @RequestBody Plant plant) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(plantService.createPlant(userId, plant));
    }

    @PutMapping("/{id}")
    public ApiResponse<Plant> updatePlant(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                          @PathVariable("id") Long id,
                                          @Validated @RequestBody Plant plant) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(plantService.updatePlant(userId, id, plant));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePlant(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                         @PathVariable("id") Long id) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        plantService.deletePlant(userId, id);
        return ApiResponse.success();
    }

    @PutMapping("/{id}/focus")
    public ApiResponse<Plant> setFocus(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                       @PathVariable("id") Long id,
                                       @Validated @RequestBody PlantFocusRequest request) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(plantService.setFocus(userId, id, request));
    }

    @DeleteMapping("/{id}/focus")
    public ApiResponse<Plant> clearFocus(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                         @PathVariable("id") Long id) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(plantService.clearFocus(userId, id));
    }
}
