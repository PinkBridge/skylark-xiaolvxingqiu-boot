package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

@Data
public class CareTask {
    private String id;
    private String name;
    private String plantName;
    private Integer offset;
    private String timeText;
    private String icon;
    private Boolean completed;
}
