package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.model.UserProfile;
import cn.skylark.xiaolvxingqiu.boot.service.AppDataService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final AppDataService appDataService;

    public ProfileController(AppDataService appDataService) {
        this.appDataService = appDataService;
    }

    @GetMapping
    public ApiResponse<UserProfile> getProfile() {
        return ApiResponse.success(appDataService.getUserProfile());
    }

    @PutMapping
    public ApiResponse<Void> updateProfile(@Validated @RequestBody UserProfile userProfile) {
        appDataService.updateUserProfile(userProfile);
        return ApiResponse.success();
    }
}
