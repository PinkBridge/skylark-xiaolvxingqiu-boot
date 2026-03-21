package cn.skylark.xiaolvxingqiu.boot.controller;

import cn.skylark.xiaolvxingqiu.boot.common.ApiResponse;
import cn.skylark.xiaolvxingqiu.boot.config.UserContextProvider;
import cn.skylark.xiaolvxingqiu.boot.model.CsChatRequest;
import cn.skylark.xiaolvxingqiu.boot.model.CsChatResponse;
import cn.skylark.xiaolvxingqiu.boot.service.CsChatService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cs")
public class CsController {

    private final UserContextProvider userContextProvider;
    private final CsChatService csChatService;

    public CsController(UserContextProvider userContextProvider, CsChatService csChatService) {
        this.userContextProvider = userContextProvider;
        this.csChatService = csChatService;
    }

    @PostMapping("/chat")
    public ApiResponse<CsChatResponse> chat(@RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
                                            @Validated @RequestBody CsChatRequest request) {
        userContextProvider.resolveUserId(headerUserId);
        return ApiResponse.success(csChatService.chat(request));
    }
}
