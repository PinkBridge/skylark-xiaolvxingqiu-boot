package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.UserCoinAccountSummary;
import cn.skylark.xiaolvxingqiu.boot.service.UserCoinService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/coin")
public class CoinController {

    private final UserCoinService userCoinService;
    private final UserContextProvider userContextProvider;

    public CoinController(UserCoinService userCoinService, UserContextProvider userContextProvider) {
        this.userCoinService = userCoinService;
        this.userContextProvider = userContextProvider;
    }

    @GetMapping("/account")
    public ApiResponse<UserCoinAccountSummary> getAccount(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        Long userId = userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(userCoinService.getAccountSummary(userId));
    }

    @PostMapping("/settle")
    public ApiResponse<Map<String, Object>> settle(@RequestParam(value = "date", required = false) String date) {
        LocalDate targetDate = (date == null || date.trim().isEmpty()) ? LocalDate.now() : LocalDate.parse(date);
        int settledUsers = userCoinService.settleByDate(targetDate);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("date", targetDate.toString());
        result.put("settledUsers", settledUsers);
        return ApiResponse.success(result);
    }
}
