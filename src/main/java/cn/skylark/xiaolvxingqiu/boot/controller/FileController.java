package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.ImageUploadResponse;
import cn.skylark.xiaolvxingqiu.boot.service.CosImageStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final UserContextProvider userContextProvider;
    private final CosImageStorageService cosImageStorageService;

    @Value("${app.about.group-qr-key:}")
    private String groupQrKey;

    public FileController(UserContextProvider userContextProvider, CosImageStorageService cosImageStorageService) {
        this.userContextProvider = userContextProvider;
        this.cosImageStorageService = cosImageStorageService;
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ImageUploadResponse> uploadImage(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                        @RequestPart("file") MultipartFile file) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        CosImageStorageService.UploadResult result = cosImageStorageService.uploadImage(userId, file);
        return ApiResponse.success(new ImageUploadResponse(
                result.getUrl(),
                result.getKey(),
                result.getSignedUrl(),
                result.getExpireAtEpochSecond()
        ));
    }

    @GetMapping("/about-group-qr")
    public ApiResponse<ImageUploadResponse> aboutGroupQr() {
        CosImageStorageService.UploadResult result = cosImageStorageService.resolveObjectUrl(groupQrKey, true);
        return ApiResponse.success(new ImageUploadResponse(
                result.getUrl(),
                result.getKey(),
                result.getSignedUrl(),
                result.getExpireAtEpochSecond()
        ));
    }
}
