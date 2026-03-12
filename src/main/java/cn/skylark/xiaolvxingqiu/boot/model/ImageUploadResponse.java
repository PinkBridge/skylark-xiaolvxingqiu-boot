package cn.skylark.xiaolvxingqiu.boot.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageUploadResponse {

    private String url;
    private String key;
    private String signedUrl;
    private Long expireAtEpochSecond;
}
