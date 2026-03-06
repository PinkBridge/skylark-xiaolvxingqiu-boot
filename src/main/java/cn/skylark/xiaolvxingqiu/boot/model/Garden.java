package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Garden {

    private Long id;
    private Long userId;
    private String name;
    private String establishedDate;
    private String thumbUrl;
    private String coverUrl;
    private String description;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
