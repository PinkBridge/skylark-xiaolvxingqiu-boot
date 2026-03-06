package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.Feedback;
import cn.skylark.xiaolvxingqiu.boot.service.FeedbackService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final UserContextProvider userContextProvider;

    public FeedbackController(FeedbackService feedbackService, UserContextProvider userContextProvider) {
        this.feedbackService = feedbackService;
        this.userContextProvider = userContextProvider;
    }

    @PostMapping
    public ApiResponse<Feedback> submitFeedback(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                                @Validated @RequestBody Feedback feedback) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(feedbackService.submit(userId, feedback));
    }

    @GetMapping
    public ApiResponse<List<Feedback>> listFeedback(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(feedbackService.listByUserId(userId));
    }
}
