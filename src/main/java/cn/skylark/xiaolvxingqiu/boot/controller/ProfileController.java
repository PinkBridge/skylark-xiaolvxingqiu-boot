package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.UserProfile;
import cn.skylark.xiaolvxingqiu.boot.service.UserProfileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final UserProfileService userProfileService;
    private final UserContextProvider userContextProvider;

    public ProfileController(UserProfileService userProfileService, UserContextProvider userContextProvider) {
        this.userProfileService = userProfileService;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping
    public ApiResponse<UserProfile> getProfile(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(userProfileService.getOrDefault(userId));
    }

    @PutMapping
    public ApiResponse<Void> updateProfile(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                           @Validated @RequestBody UserProfile userProfile) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        userProfileService.updateProfile(userId, userProfile);
        return ApiResponse.success();
    }
}
