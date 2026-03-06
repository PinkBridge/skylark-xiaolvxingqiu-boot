package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Data
public class Plant {

    private Long id;
    private Long userId;
    private Long gardenId;

    @NotBlank(message = "plant name cannot be empty")
    private String name;

    private String species;
    private String image;
    private String cultivationType;
    private String plantingDate;
    private String note;

    private String healthStatus;
    private String statusLabel;
    private Integer days;
    private Boolean favorite;
    private Boolean focused;
    private String focusReason;
    private String focusPhoto;
    private String focusAt;
    private List<String> todayCareTasks = new ArrayList<>();
}
