package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PlantStatusLogEntry {
    private Long id;
    private String status;
    private LocalDateTime changedAt;
}
