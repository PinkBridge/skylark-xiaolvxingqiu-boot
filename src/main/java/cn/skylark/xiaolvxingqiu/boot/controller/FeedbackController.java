package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.model.Feedback;
import cn.skylark.xiaolvxingqiu.boot.service.AppDataService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final AppDataService appDataService;

    public FeedbackController(AppDataService appDataService) {
        this.appDataService = appDataService;
    }

    @PostMapping
    public ApiResponse<Feedback> submitFeedback(@Validated @RequestBody Feedback feedback) {
        return ApiResponse.success(appDataService.saveFeedback(feedback));
    }

    @GetMapping
    public ApiResponse<List<Feedback>> listFeedback() {
        return ApiResponse.success(appDataService.listFeedback());
    }
}
