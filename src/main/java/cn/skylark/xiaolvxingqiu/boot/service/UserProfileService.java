package cn.skylark.xiaolvxingqiu.boot.service;

import cn.skylark.xiaolvxingqiu.boot.mapper.UserProfileMapper;
import cn.skylark.xiaolvxingqiu.boot.model.UserProfile;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {

    private static final String DEFAULT_AVATAR = "https://cdn.uviewui.com/uview/example/button.png";
    private static final String DEFAULT_NAME = "微信用户";
    private static final String DEFAULT_GENDER = "unknown";
    private static final String DEFAULT_MOTTO = "每一份生命都值得尊重和呵护!";

    private final UserProfileMapper userProfileMapper;

    public UserProfileService(UserProfileMapper userProfileMapper) {
        this.userProfileMapper = userProfileMapper;
    }

    public UserProfile getOrDefault(Long userId) {
        UserProfile profile = userProfileMapper.selectByUserId(userId);
        if (profile == null) {
            profile = new UserProfile();
            profile.setUserId(userId);
            profile.setAvatar(DEFAULT_AVATAR);
            profile.setName(DEFAULT_NAME);
            profile.setGender(DEFAULT_GENDER);
            profile.setBirthday("");
            profile.setMotto(DEFAULT_MOTTO);
            profile.setPhone("");
            userProfileMapper.upsert(profile);
            return profile;
        }
        normalizeDefaults(userId, profile);
        return profile;
    }

    public void updateProfile(Long userId, UserProfile request) {
        UserProfile merged = mergeProfile(userId, request);
        userProfileMapper.upsert(merged);
    }

    public UserProfile mergeWechatProfile(Long userId, String avatar, String name, String gender, String phone) {
        UserProfile current = getOrDefault(userId);
        if (avatar != null && !avatar.trim().isEmpty()) current.setAvatar(avatar.trim());
        if (name != null && !name.trim().isEmpty()) current.setName(name.trim());
        if (gender != null && !gender.trim().isEmpty()) current.setGender(gender.trim());
        if (phone != null && !phone.trim().isEmpty()) current.setPhone(phone.trim());
        normalizeDefaults(userId, current);
        userProfileMapper.upsert(current);
        return current;
    }

    private UserProfile mergeProfile(Long userId, UserProfile request) {
        UserProfile current = getOrDefault(userId);
        if (request.getAvatar() != null) current.setAvatar(request.getAvatar());
        if (request.getName() != null) current.setName(request.getName());
        if (request.getGender() != null) current.setGender(request.getGender());
        if (request.getBirthday() != null) current.setBirthday(request.getBirthday());
        if (request.getMotto() != null) current.setMotto(request.getMotto());
        if (request.getPhone() != null) current.setPhone(request.getPhone());
        normalizeDefaults(userId, current);
        return current;
    }

    private void normalizeDefaults(Long userId, UserProfile profile) {
        profile.setUserId(userId);
        profile.setAvatar(blankToDefault(profile.getAvatar(), DEFAULT_AVATAR));
        profile.setName(blankToDefault(profile.getName(), DEFAULT_NAME));
        profile.setGender(blankToDefault(profile.getGender(), DEFAULT_GENDER));
        profile.setBirthday(profile.getBirthday() == null ? "" : profile.getBirthday());
        profile.setMotto(blankToDefault(profile.getMotto(), DEFAULT_MOTTO));
        profile.setPhone(profile.getPhone() == null ? "" : profile.getPhone().trim());
    }

    private String blankToDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) return fallback;
        return value.trim();
    }
}
