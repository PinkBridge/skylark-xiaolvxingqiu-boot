package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlantAlbumPage {
    private Integer pageNo;
    private Integer pageSize;
    private Long total;
    private Boolean hasMore;
    private List<PlantAlbumItem> list = new ArrayList<>();
}
