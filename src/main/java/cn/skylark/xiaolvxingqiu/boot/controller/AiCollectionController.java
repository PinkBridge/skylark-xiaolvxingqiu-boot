package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.AiCollectionRequest;
import cn.skylark.xiaolvxingqiu.boot.model.AiPlantCollection;
import cn.skylark.xiaolvxingqiu.boot.model.Plant;
import cn.skylark.xiaolvxingqiu.boot.service.AiPlantCollectionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/collections")
public class AiCollectionController {

    private final UserContextProvider userContextProvider;
    private final AiPlantCollectionService aiPlantCollectionService;

    public AiCollectionController(UserContextProvider userContextProvider, AiPlantCollectionService aiPlantCollectionService) {
        this.userContextProvider = userContextProvider;
        this.aiPlantCollectionService = aiPlantCollectionService;
    }

    @PostMapping
    public ApiResponse<AiPlantCollection> create(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                 @Validated @RequestBody AiCollectionRequest request) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(aiPlantCollectionService.create(userId, request));
    }

    @GetMapping
    public ApiResponse<List<AiPlantCollection>> list(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(aiPlantCollectionService.listByUserId(userId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                    @PathVariable("id") Long id) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        aiPlantCollectionService.delete(userId, id);
        return ApiResponse.success();
    }

    @PostMapping("/{id}/add-to-garden")
    public ApiResponse<Plant> addToGarden(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                          @PathVariable("id") Long id) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(aiPlantCollectionService.addToGardenByCollectionId(userId, id));
    }

    @PostMapping("/add-to-garden")
    public ApiResponse<Plant> addRecognizedToGarden(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                    @Validated @RequestBody AiCollectionRequest request) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(aiPlantCollectionService.addToGardenByRecognition(userId, request));
    }
}
