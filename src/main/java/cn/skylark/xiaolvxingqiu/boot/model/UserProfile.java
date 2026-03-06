package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserProfile {

    private String avatar;

    @NotBlank(message = "user name cannot be empty")
    private String name;

    private String gender;
    private String birthday;
    private String motto;
}
