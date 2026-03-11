package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.PlantRecognitionResponse;
import cn.skylark.xiaolvxingqiu.boot.service.PlantRecognitionService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final UserContextProvider userContextProvider;
    private final PlantRecognitionService plantRecognitionService;

    public AiController(UserContextProvider userContextProvider, PlantRecognitionService plantRecognitionService) {
        this.userContextProvider = userContextProvider;
        this.plantRecognitionService = plantRecognitionService;
    }

    @PostMapping(value = "/plant/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PlantRecognitionResponse> recognizePlant(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                                @RequestPart("file") MultipartFile file) {
        // Keep same auth behavior as other biz APIs.
        userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(plantRecognitionService.recognize(file));
    }
}
