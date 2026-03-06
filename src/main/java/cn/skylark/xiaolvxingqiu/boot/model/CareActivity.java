package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.util.Map;

@Data
public class CareActivity {
    private String id;
    private String date;
    private String time;
    private String name;
    private String plantName;
    private Boolean completed;
    private String icon;
    private Map<String, Object> record;
}
