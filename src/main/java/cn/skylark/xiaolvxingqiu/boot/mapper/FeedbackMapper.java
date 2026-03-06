package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.Feedback;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeedbackMapper {

    @Insert("INSERT INTO feedback (user_id,content,contact,deleted,created_at,updated_at) " +
            "VALUES (#{userId},#{content},#{contact},0,NOW(),NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Feedback feedback);

    @Select("SELECT id,user_id,content,contact,created_at FROM feedback " +
            "WHERE user_id=#{userId} AND deleted=0 ORDER BY id DESC")
    List<Feedback> selectByUserId(@Param("userId") Long userId);
}
