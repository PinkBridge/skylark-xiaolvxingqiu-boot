package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.UserSubscribeSetting;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserSubscribeSettingMapper {

    @Select("SELECT user_id,enabled,push_time,template_id,auth_status,last_auth_at,last_push_date,last_push_status,last_push_result " +
            "FROM user_subscribe_setting WHERE user_id=#{userId} AND deleted=0 LIMIT 1")
    UserSubscribeSetting selectByUserId(@Param("userId") Long userId);

    @Insert("INSERT INTO user_subscribe_setting (user_id,enabled,push_time,template_id,auth_status,last_auth_at,last_push_date,last_push_status,last_push_result,deleted,created_at,updated_at) " +
            "VALUES (#{userId},#{enabled},#{pushTime},#{templateId},#{authStatus},#{lastAuthAt},NULL,NULL,NULL,0,NOW(),NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "enabled=VALUES(enabled),push_time=VALUES(push_time),template_id=VALUES(template_id),auth_status=VALUES(auth_status),last_auth_at=VALUES(last_auth_at),updated_at=NOW()")
    int upsert(UserSubscribeSetting setting);

    @Select("SELECT user_id,enabled,push_time,template_id,auth_status,last_auth_at,last_push_date,last_push_status,last_push_result " +
            "FROM user_subscribe_setting " +
            "WHERE deleted=0 AND enabled=1 AND auth_status='ACCEPT' AND push_time=#{pushTime} " +
            "AND (last_push_date IS NULL OR last_push_date<>#{today})")
    List<UserSubscribeSetting> selectDueUsersByTime(@Param("pushTime") String pushTime, @Param("today") LocalDate today);

    @Update("UPDATE user_subscribe_setting SET last_push_date=#{pushDate},last_push_status=#{status},last_push_result=#{result},updated_at=NOW() " +
            "WHERE user_id=#{userId} AND deleted=0")
    int updateDispatchResult(@Param("userId") Long userId,
                             @Param("pushDate") LocalDate pushDate,
                             @Param("status") String status,
                             @Param("result") String result);
}
