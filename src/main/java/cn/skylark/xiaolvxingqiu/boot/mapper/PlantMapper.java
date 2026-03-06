package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.Plant;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PlantMapper {

    @Select("SELECT p.id,p.user_id,p.garden_id,p.name,p.species,p.image_url AS image,p.cultivation_type,DATE_FORMAT(p.planting_date,'%Y-%m-%d') AS planting_date,p.note,p.health_status,p.is_favorite AS favorite," +
            "IF(f.id IS NULL,0,1) AS focused,f.reason AS focus_reason,f.photo_url AS focus_photo,DATE_FORMAT(f.created_at,'%Y-%m-%d %H:%i:%s') AS focus_at " +
            "FROM plant p LEFT JOIN plant_focus f ON p.user_id=f.user_id AND p.id=f.plant_id " +
            "WHERE p.user_id=#{userId} AND p.deleted=0 ORDER BY p.id DESC")
    List<Plant> selectByUserId(Long userId);

    @Select("SELECT p.id,p.user_id,p.garden_id,p.name,p.species,p.image_url AS image,p.cultivation_type,DATE_FORMAT(p.planting_date,'%Y-%m-%d') AS planting_date,p.note,p.health_status,p.is_favorite AS favorite," +
            "IF(f.id IS NULL,0,1) AS focused,f.reason AS focus_reason,f.photo_url AS focus_photo,DATE_FORMAT(f.created_at,'%Y-%m-%d %H:%i:%s') AS focus_at " +
            "FROM plant p LEFT JOIN plant_focus f ON p.user_id=f.user_id AND p.id=f.plant_id " +
            "WHERE p.user_id=#{userId} AND p.deleted=0 AND (p.health_status='healthy' OR p.health_status='健康') ORDER BY p.id DESC")
    List<Plant> selectHealthyByUserId(Long userId);

    @Select("SELECT p.id,p.user_id,p.garden_id,p.name,p.species,p.image_url AS image,p.cultivation_type,DATE_FORMAT(p.planting_date,'%Y-%m-%d') AS planting_date,p.note,p.health_status,p.is_favorite AS favorite," +
            "IF(f.id IS NULL,0,1) AS focused,f.reason AS focus_reason,f.photo_url AS focus_photo,DATE_FORMAT(f.created_at,'%Y-%m-%d %H:%i:%s') AS focus_at " +
            "FROM plant p LEFT JOIN plant_focus f ON p.user_id=f.user_id AND p.id=f.plant_id " +
            "WHERE p.user_id=#{userId} AND p.deleted=0 AND (p.health_status IN ('sick','dormant','生病','休眠')) ORDER BY p.id DESC")
    List<Plant> selectAbnormalByUserId(Long userId);

    @Select("SELECT p.id,p.user_id,p.garden_id,p.name,p.species,p.image_url AS image,p.cultivation_type,DATE_FORMAT(p.planting_date,'%Y-%m-%d') AS planting_date,p.note,p.health_status,p.is_favorite AS favorite," +
            "IF(f.id IS NULL,0,1) AS focused,f.reason AS focus_reason,f.photo_url AS focus_photo,DATE_FORMAT(f.created_at,'%Y-%m-%d %H:%i:%s') AS focus_at " +
            "FROM plant p LEFT JOIN plant_focus f ON p.user_id=f.user_id AND p.id=f.plant_id " +
            "WHERE p.user_id=#{userId} AND p.deleted=0 AND p.is_favorite=1 ORDER BY p.id DESC")
    List<Plant> selectFavoriteByUserId(Long userId);

    @Select("SELECT p.id,p.user_id,p.garden_id,p.name,p.species,p.image_url AS image,p.cultivation_type,DATE_FORMAT(p.planting_date,'%Y-%m-%d') AS planting_date,p.note,p.health_status,p.is_favorite AS favorite," +
            "IF(f.id IS NULL,0,1) AS focused,f.reason AS focus_reason,f.photo_url AS focus_photo,DATE_FORMAT(f.created_at,'%Y-%m-%d %H:%i:%s') AS focus_at " +
            "FROM plant p LEFT JOIN plant_focus f ON p.user_id=f.user_id AND p.id=f.plant_id " +
            "WHERE p.user_id=#{userId} AND p.id=#{id} AND p.deleted=0 LIMIT 1")
    Plant selectByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);

    @Select("SELECT p.id,p.user_id,p.garden_id,p.name,p.species,p.image_url AS image,p.cultivation_type,DATE_FORMAT(p.planting_date,'%Y-%m-%d') AS planting_date,p.note,p.health_status,p.is_favorite AS favorite," +
            "1 AS focused,f.reason AS focus_reason,f.photo_url AS focus_photo,DATE_FORMAT(f.created_at,'%Y-%m-%d %H:%i:%s') AS focus_at " +
            "FROM plant p INNER JOIN plant_focus f ON p.user_id=f.user_id AND p.id=f.plant_id " +
            "WHERE p.user_id=#{userId} AND p.deleted=0 ORDER BY f.created_at DESC,p.id DESC")
    List<Plant> selectFocusedByUserId(Long userId);

    @Insert("INSERT INTO plant (user_id,garden_id,name,species,image_url,cultivation_type,planting_date,note,health_status,is_favorite,deleted,created_at,updated_at) " +
            "VALUES (#{userId},#{gardenId},#{name},#{species},#{image},#{cultivationType},STR_TO_DATE(#{plantingDate},'%Y-%m-%d'),#{note},#{healthStatus},#{favorite},0,NOW(),NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Plant plant);

    @Update("UPDATE plant SET name=#{name},species=#{species},image_url=#{image},cultivation_type=#{cultivationType},planting_date=STR_TO_DATE(#{plantingDate},'%Y-%m-%d'),note=#{note},health_status=#{healthStatus},is_favorite=#{favorite},updated_at=NOW() " +
            "WHERE user_id=#{userId} AND id=#{id} AND deleted=0")
    int updateByUserIdAndId(Plant plant);

    @Update("UPDATE plant SET deleted=1,updated_at=NOW() WHERE user_id=#{userId} AND id=#{id} AND deleted=0")
    int softDelete(@Param("userId") Long userId, @Param("id") Long id);

    @Insert("INSERT INTO plant_focus (user_id,plant_id,reason,photo_url,created_at,updated_at) " +
            "VALUES (#{userId},#{plantId},#{reason},#{photoUrl},NOW(),NOW()) " +
            "ON DUPLICATE KEY UPDATE reason=VALUES(reason),photo_url=VALUES(photo_url),updated_at=NOW()")
    int upsertFocus(@Param("userId") Long userId,
                    @Param("plantId") Long plantId,
                    @Param("reason") String reason,
                    @Param("photoUrl") String photoUrl);

    @Delete("DELETE FROM plant_focus WHERE user_id=#{userId} AND plant_id=#{plantId}")
    int clearFocus(@Param("userId") Long userId, @Param("plantId") Long plantId);

    @Insert("INSERT INTO plant_status_log (user_id,plant_id,status,changed_at,created_at) " +
            "VALUES (#{userId},#{plantId},#{status},#{changedAt},NOW())")
    int insertStatusLog(@Param("userId") Long userId,
                        @Param("plantId") Long plantId,
                        @Param("status") String status,
                        @Param("changedAt") LocalDateTime changedAt);

    @Select("SELECT id,status,changed_at FROM plant_status_log " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} ORDER BY changed_at ASC,id ASC")
    List<cn.skylark.xiaolvxingqiu.boot.model.PlantStatusLogEntry> selectStatusLogsByPlant(@Param("userId") Long userId,
                                                                                            @Param("plantId") Long plantId);

    @Delete("DELETE FROM plant_status_log WHERE user_id=#{userId} AND plant_id=#{plantId}")
    int clearStatusLogs(@Param("userId") Long userId, @Param("plantId") Long plantId);
}
