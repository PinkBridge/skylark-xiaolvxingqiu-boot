package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.AiPlantCollection;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AiPlantCollectionMapper {

    @Insert("INSERT INTO ai_plant_collection (user_id,name,description,image_url,recognized_image_url,score,source,deleted,created_at,updated_at) " +
            "VALUES (#{userId},#{name},#{description},#{imageUrl},#{recognizedImageUrl},#{score},#{source},0,NOW(),NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AiPlantCollection item);

    @Select("SELECT id,user_id,name,description,image_url,recognized_image_url,score,source,DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') AS created_at " +
            "FROM ai_plant_collection WHERE user_id=#{userId} AND deleted=0 ORDER BY id DESC")
    List<AiPlantCollection> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT id,user_id,name,description,image_url,recognized_image_url,score,source,DATE_FORMAT(created_at,'%Y-%m-%d %H:%i:%s') AS created_at " +
            "FROM ai_plant_collection WHERE user_id=#{userId} AND id=#{id} AND deleted=0 LIMIT 1")
    AiPlantCollection selectByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    @Update("UPDATE ai_plant_collection SET deleted=1,updated_at=NOW() WHERE user_id=#{userId} AND id=#{id} AND deleted=0")
    int softDelete(@Param("userId") Long userId, @Param("id") Long id);
}
