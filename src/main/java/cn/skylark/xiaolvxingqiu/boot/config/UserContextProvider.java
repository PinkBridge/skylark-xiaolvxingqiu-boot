package cn.skylark.xiaolvxingqiu.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserContextProvider {

    @Value("${app.auth.use-mock-user:true}")
    private boolean useMockUser;

    @Value("${app.auth.mock-user-id:10001}")
    private Long mockUserId;

    public Long resolveUserId(Long headerUserId) {
        if (useMockUser) {
            return mockUserId;
        }
        if (headerUserId == null) {
            throw new IllegalArgumentException("missing header: X-User-Id");
        }
        return headerUserId;
    }
}
