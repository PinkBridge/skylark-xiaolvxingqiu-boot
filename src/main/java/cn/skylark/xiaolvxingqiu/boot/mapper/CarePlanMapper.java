package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.CarePlanEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CarePlanMapper {

    @Select("SELECT id,user_id,plant_id,enabled,seasonal_mode FROM care_plan WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 LIMIT 1")
    CarePlanEntity selectByUserIdAndPlantId(@Param("userId") Long userId, @Param("plantId") Long plantId);

    @Insert("INSERT INTO care_plan (user_id,plant_id,enabled,seasonal_mode,deleted,created_at,updated_at) VALUES (#{userId},#{plantId},#{enabled},#{seasonalMode},0,NOW(),NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CarePlanEntity plan);

    @Update("UPDATE care_plan SET enabled=#{enabled},seasonal_mode=#{seasonalMode},updated_at=NOW() WHERE id=#{id} AND user_id=#{userId} AND deleted=0")
    int updateConfig(CarePlanEntity plan);

    @Select("SELECT DISTINCT user_id FROM care_plan WHERE deleted=0 AND enabled=1")
    List<Long> selectEnabledUserIds();

    @Select("SELECT plant_id FROM care_plan WHERE id=#{planId} AND deleted=0 LIMIT 1")
    Long selectPlantIdByPlanId(@Param("planId") Long planId);
}
