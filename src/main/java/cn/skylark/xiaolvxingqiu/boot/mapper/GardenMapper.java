package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.Garden;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GardenMapper {

    @Select("SELECT id,user_id,name,DATE_FORMAT(established_date,'%Y-%m-%d') AS established_date,thumb_url,cover_url,description,is_default,created_at,updated_at " +
            "FROM garden WHERE user_id=#{userId} AND deleted=0 ORDER BY updated_at DESC")
    List<Garden> selectByUserId(Long userId);

    @Select("SELECT id,user_id,name,DATE_FORMAT(established_date,'%Y-%m-%d') AS established_date,thumb_url,cover_url,description,is_default,created_at,updated_at " +
            "FROM garden WHERE user_id=#{userId} AND id=#{gardenId} AND deleted=0 LIMIT 1")
    Garden selectByUserIdAndId(@Param("userId") Long userId, @Param("gardenId") Long gardenId);

    @Select("SELECT id,user_id,name,DATE_FORMAT(established_date,'%Y-%m-%d') AS established_date,thumb_url,cover_url,description,is_default,created_at,updated_at " +
            "FROM garden WHERE user_id=#{userId} AND deleted=0 AND is_default=1 LIMIT 1")
    Garden selectDefaultByUserId(Long userId);

    @Select("SELECT id,user_id,name,DATE_FORMAT(established_date,'%Y-%m-%d') AS established_date,thumb_url,cover_url,description,is_default,created_at,updated_at " +
            "FROM garden WHERE user_id=#{userId} AND deleted=0 ORDER BY updated_at DESC LIMIT 1")
    Garden selectFirstByUserId(Long userId);

    @Select("SELECT COUNT(1) FROM garden WHERE user_id=#{userId} AND deleted=0")
    Integer countByUserId(Long userId);

    @Insert("INSERT INTO garden (user_id,name,established_date,thumb_url,cover_url,description,is_default,deleted,created_at,updated_at) " +
            "VALUES (#{userId},#{name},STR_TO_DATE(#{establishedDate},'%Y-%m-%d'),#{thumbUrl},#{coverUrl},#{description},#{isDefault},0,NOW(),NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Garden garden);

    @Update("UPDATE garden SET name=#{name},established_date=STR_TO_DATE(#{establishedDate},'%Y-%m-%d'),thumb_url=#{thumbUrl},cover_url=#{coverUrl},description=#{description},updated_at=NOW() " +
            "WHERE user_id=#{userId} AND id=#{id} AND deleted=0")
    int updateByUserIdAndId(Garden garden);

    @Update("UPDATE garden SET deleted=1,is_default=0,updated_at=NOW() WHERE user_id=#{userId} AND id=#{gardenId} AND deleted=0")
    int softDelete(@Param("userId") Long userId, @Param("gardenId") Long gardenId);

    @Update("UPDATE garden SET is_default=0,updated_at=NOW() WHERE user_id=#{userId} AND deleted=0")
    int clearDefaultByUserId(Long userId);

    @Update("UPDATE garden SET is_default=1,updated_at=NOW() WHERE user_id=#{userId} AND id=#{gardenId} AND deleted=0")
    int setDefault(@Param("userId") Long userId, @Param("gardenId") Long gardenId);
}
