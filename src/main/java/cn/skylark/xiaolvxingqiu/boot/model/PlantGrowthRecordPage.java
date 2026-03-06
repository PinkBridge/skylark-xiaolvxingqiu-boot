package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlantGrowthRecordPage {
    private Integer pageNo;
    private Integer pageSize;
    private Long total;
    private Boolean hasMore;
    private List<PlantGrowthRecord> list = new ArrayList<>();
}
