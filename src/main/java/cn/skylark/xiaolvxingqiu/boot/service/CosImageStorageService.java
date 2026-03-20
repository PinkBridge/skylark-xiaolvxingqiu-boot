package cn.skylark.xiaolvxingqiu.boot.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.CannedAccessControlList;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.net.URL;

@Service
public class CosImageStorageService {

    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${app.cos.bucket:}")
    private String bucket;

    @Value("${app.cos.region:}")
    private String region;

    @Value("${app.cos.secret-id:}")
    private String secretId;

    @Value("${app.cos.secret-key:}")
    private String secretKey;

    @Value("${app.cos.public-domain:}")
    private String publicDomain;

    @Value("${app.cos.base-path:uploads}")
    private String basePath;

    @Value("${app.cos.max-image-size-mb:10}")
    private Integer maxImageSizeMb;

    @Value("${app.cos.private-read:false}")
    private boolean privateRead;

    @Value("${app.cos.sign-expire-seconds:86400}")
    private Long signExpireSeconds;

    public UploadResult uploadImage(Long userId, MultipartFile file) {
        validateInput(file);
        COSClient cosClient = null;
        try {
            COSCredentials cred = new BasicCOSCredentials(secretId.trim(), secretKey.trim());
            ClientConfig clientConfig = new ClientConfig(new Region(region.trim()));
            clientConfig.setHttpProtocol(HttpProtocol.https);
            cosClient = new COSClient(cred, clientConfig);

            String ext = detectExt(file);
            String objectKey = buildObjectKey(userId, ext);
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(resolveContentType(file.getContentType(), ext));
            PutObjectRequest request = new PutObjectRequest(bucket.trim(), objectKey, file.getInputStream(), metadata);
            if (!privateRead) {
                // Keep object directly viewable when using public URL mode.
                request.setCannedAcl(CannedAccessControlList.PublicRead);
            }
            cosClient.putObject(request);
            String publicUrl = buildPublicUrl(objectKey);
            String signedUrl = buildSignedUrl(cosClient, objectKey);
            long expireAtEpochSecond = System.currentTimeMillis() / 1000 + safeSignExpireSeconds();
            // If bucket is private, client should use signed URL; otherwise prefer public URL.
            String accessUrl = privateRead ? signedUrl : publicUrl;
            return new UploadResult(accessUrl, objectKey, signedUrl, expireAtEpochSecond);
        } catch (IOException e) {
            throw new IllegalArgumentException("读取图片失败");
        } catch (Exception e) {
            throw new IllegalArgumentException("上传图片到 COS 失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    public UploadResult resolveObjectUrl(String objectKey, boolean preferSigned) {
        String normalizedKey = trimSlash(objectKey);
        if (isBlank(normalizedKey)) {
            throw new IllegalArgumentException("二维码对象 key 未配置");
        }
        if (isBlank(bucket) || isBlank(region) || isBlank(secretId) || isBlank(secretKey) || isBlank(publicDomain)) {
            throw new IllegalArgumentException("COS 配置不完整，请先配置 bucket/region/secret-id/secret-key/public-domain");
        }
        COSClient cosClient = null;
        try {
            COSCredentials cred = new BasicCOSCredentials(secretId.trim(), secretKey.trim());
            ClientConfig clientConfig = new ClientConfig(new Region(region.trim()));
            clientConfig.setHttpProtocol(HttpProtocol.https);
            cosClient = new COSClient(cred, clientConfig);
            String publicUrl = buildPublicUrl(normalizedKey);
            String signedUrl = buildSignedUrl(cosClient, normalizedKey);
            long expireAtEpochSecond = System.currentTimeMillis() / 1000 + safeSignExpireSeconds();
            String accessUrl = preferSigned && !isBlank(signedUrl)
                    ? signedUrl
                    : (privateRead ? signedUrl : publicUrl);
            return new UploadResult(accessUrl, normalizedKey, signedUrl, expireAtEpochSecond);
        } catch (Exception e) {
            throw new IllegalArgumentException("获取 COS 访问地址失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    private void validateInput(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片文件");
        }
        if (isBlank(bucket) || isBlank(region) || isBlank(secretId) || isBlank(secretKey) || isBlank(publicDomain)) {
            throw new IllegalArgumentException("COS 配置不完整，请先配置 bucket/region/secret-id/secret-key/public-domain");
        }
        long maxSizeBytes = Math.max(1, safeMaxImageSizeMb()) * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            throw new IllegalArgumentException("图片过大，请上传不超过 " + safeMaxImageSizeMb() + "MB 的图片");
        }
        String ext = detectExt(file);
        if (isBlank(ext)) {
            throw new IllegalArgumentException("仅支持 jpg/jpeg/png/webp/gif 图片");
        }
    }

    private String detectExt(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (isSupportedExt(ext)) return ext;
        }
        String contentType = file.getContentType();
        if (contentType == null) return "";
        if (contentType.contains("jpeg")) return "jpg";
        if (contentType.contains("png")) return "png";
        if (contentType.contains("webp")) return "webp";
        if (contentType.contains("gif")) return "gif";
        return "";
    }

    private boolean isSupportedExt(String ext) {
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) || "webp".equals(ext) || "gif".equals(ext);
    }

    private String resolveContentType(String contentType, String ext) {
        if (!isBlank(contentType)) return contentType;
        if ("png".equals(ext)) return "image/png";
        if ("gif".equals(ext)) return "image/gif";
        if ("webp".equals(ext)) return "image/webp";
        return "image/jpeg";
    }

    private String buildObjectKey(Long userId, String ext) {
        String cleanBase = trimSlash(basePath);
        String day = LocalDate.now().format(DAY_FORMATTER);
        String uid = userId == null ? "unknown" : String.valueOf(userId);
        String safeExt = "jpeg".equals(ext) ? "jpg" : ext;
        return cleanBase + "/u" + uid + "/" + day + "/" + UUID.randomUUID().toString().replace("-", "") + "." + safeExt;
    }

    private String buildPublicUrl(String objectKey) {
        return "https://" + trimSlash(publicDomain) + "/" + trimSlash(objectKey);
    }

    private String buildSignedUrl(COSClient cosClient, String objectKey) {
        Date expiration = new Date(System.currentTimeMillis() + safeSignExpireSeconds() * 1000L);
        URL signed = cosClient.generatePresignedUrl(bucket.trim(), objectKey, expiration);
        return signed == null ? "" : signed.toString();
    }

    private int safeMaxImageSizeMb() {
        if (maxImageSizeMb == null || maxImageSizeMb <= 0) return 10;
        return maxImageSizeMb;
    }

    private long safeSignExpireSeconds() {
        if (signExpireSeconds == null || signExpireSeconds <= 0) return 86400L;
        return signExpireSeconds;
    }

    private String trimSlash(String value) {
        if (value == null) return "";
        String v = value.trim();
        while (v.startsWith("/")) v = v.substring(1);
        while (v.endsWith("/")) v = v.substring(0, v.length() - 1);
        return v;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static class UploadResult {
        private final String url;
        private final String key;
        private final String signedUrl;
        private final Long expireAtEpochSecond;

        public UploadResult(String url, String key, String signedUrl, Long expireAtEpochSecond) {
            this.url = url;
            this.key = key;
            this.signedUrl = signedUrl;
            this.expireAtEpochSecond = expireAtEpochSecond;
        }

        public String getUrl() {
            return url;
        }

        public String getKey() {
            return key;
        }

        public String getSignedUrl() {
            return signedUrl;
        }

        public Long getExpireAtEpochSecond() {
            return expireAtEpochSecond;
        }
    }
}
