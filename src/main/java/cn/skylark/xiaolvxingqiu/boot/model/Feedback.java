package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class Feedback {
    private Long id;

    @NotBlank(message = "feedback content cannot be empty")
    private String content;

    private String contact;
    private LocalDateTime createdAt;
}
