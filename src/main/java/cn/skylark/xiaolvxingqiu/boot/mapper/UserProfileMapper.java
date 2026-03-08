package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.UserProfile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserProfileMapper {

    @Select("SELECT user_id,avatar,name,gender,birthday,motto,phone FROM user_profile WHERE user_id=#{userId} LIMIT 1")
    UserProfile selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO user_profile (user_id,avatar,name,gender,birthday,motto,phone,created_at,updated_at) " +
            "VALUES (#{userId},#{avatar},#{name},#{gender},#{birthday},#{motto},#{phone},NOW(),NOW()) " +
            "ON DUPLICATE KEY UPDATE avatar=VALUES(avatar),name=VALUES(name),gender=VALUES(gender),birthday=VALUES(birthday),motto=VALUES(motto),phone=VALUES(phone),updated_at=NOW()")
    int upsert(UserProfile profile);
}
