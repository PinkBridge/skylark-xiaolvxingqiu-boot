package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.UserWechatIdentity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserWechatIdentityMapper {

    @Select("SELECT id,user_id,openid,unionid FROM user_wechat_identity WHERE openid=#{openid} LIMIT 1")
    UserWechatIdentity selectByOpenid(@Param("openid") String openid);

    @Insert("INSERT INTO user_wechat_identity (user_id,openid,unionid,created_at,updated_at) " +
            "VALUES (#{userId},#{openid},#{unionid},NOW(),NOW())")
    int insert(UserWechatIdentity identity);
}
