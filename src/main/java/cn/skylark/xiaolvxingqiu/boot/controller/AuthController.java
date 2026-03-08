package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.WechatPhoneAuthRequest;
import cn.skylark.xiaolvxingqiu.boot.model.WechatPhoneAuthResponse;
import cn.skylark.xiaolvxingqiu.boot.service.WechatMiniAuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final WechatMiniAuthService wechatMiniAuthService;
    private final UserContextProvider userContextProvider;

    public AuthController(WechatMiniAuthService wechatMiniAuthService, UserContextProvider userContextProvider) {
        this.wechatMiniAuthService = wechatMiniAuthService;
        this.userContextProvider = userContextProvider;
    }

    @PostMapping("/wechat/phone")
    public ApiResponse<WechatPhoneAuthResponse> authWechatPhone(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                                @Validated @RequestBody WechatPhoneAuthRequest request) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(wechatMiniAuthService.authPhoneAndSaveProfile(userId, request));
    }
}
