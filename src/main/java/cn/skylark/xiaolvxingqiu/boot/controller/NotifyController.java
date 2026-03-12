package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.SubscribeSettingRequest;
import cn.skylark.xiaolvxingqiu.boot.model.SubscribeSettingResponse;
import cn.skylark.xiaolvxingqiu.boot.service.SubscribeSettingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notify")
public class NotifyController {

    private final SubscribeSettingService subscribeSettingService;
    private final UserContextProvider userContextProvider;

    public NotifyController(SubscribeSettingService subscribeSettingService,
                            UserContextProvider userContextProvider) {
        this.subscribeSettingService = subscribeSettingService;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping("/subscribe-setting")
    public ApiResponse<SubscribeSettingResponse> getSetting(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(subscribeSettingService.getByUserId(userId));
    }

    @PutMapping("/subscribe-setting")
    public ApiResponse<SubscribeSettingResponse> saveSetting(
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody(required = false) SubscribeSettingRequest request) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(subscribeSettingService.save(userId, request));
    }
}
