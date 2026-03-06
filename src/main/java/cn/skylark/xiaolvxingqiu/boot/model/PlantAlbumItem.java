package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PlantAlbumItem {
    private String id;
    private String date;
    private String desc;
    private List<String> images = new ArrayList<>();
}
