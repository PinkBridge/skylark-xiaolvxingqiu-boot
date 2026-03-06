package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.CareActivityEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CareActivityMapper {

    @Select("SELECT id,user_id,plant_id,plan_id,rule_id,activity_type,scheduled_date,status,completed_at,record_json FROM care_activity " +
            "WHERE user_id=#{userId} AND rule_id=#{ruleId} AND scheduled_date=#{scheduledDate} AND deleted=0 LIMIT 1")
    CareActivityEntity selectByRuleAndDate(@Param("userId") Long userId, @Param("ruleId") Long ruleId, @Param("scheduledDate") LocalDate scheduledDate);

    @Insert("INSERT INTO care_activity (user_id,plant_id,plan_id,rule_id,activity_type,scheduled_date,status,deleted,created_at,updated_at) " +
            "VALUES (#{userId},#{plantId},#{planId},#{ruleId},#{activityType},#{scheduledDate},'PENDING',0,NOW(),NOW())")
    int insert(CareActivityEntity activity);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND a.scheduled_date=#{date} AND a.deleted=0 ORDER BY a.id ASC")
    List<CareActivityEntity> selectByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND a.scheduled_date=#{date} AND a.deleted=0 AND p.garden_id=#{gardenId} ORDER BY a.id ASC")
    List<CareActivityEntity> selectByDateWithGarden(@Param("userId") Long userId,
                                                    @Param("date") LocalDate date,
                                                    @Param("gardenId") Long gardenId);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND DATE_FORMAT(a.scheduled_date,'%Y-%m')=#{month} AND a.deleted=0 ORDER BY a.scheduled_date ASC,a.id ASC")
    List<CareActivityEntity> selectByMonth(@Param("userId") Long userId, @Param("month") String month);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND DATE_FORMAT(a.scheduled_date,'%Y-%m')=#{month} AND a.deleted=0 AND p.garden_id=#{gardenId} " +
            "ORDER BY a.scheduled_date ASC,a.id ASC")
    List<CareActivityEntity> selectByMonthWithGarden(@Param("userId") Long userId,
                                                     @Param("month") String month,
                                                     @Param("gardenId") Long gardenId);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND a.scheduled_date BETWEEN #{startDate} AND #{endDate} AND a.deleted=0 ORDER BY a.scheduled_date ASC,a.id ASC")
    List<CareActivityEntity> selectByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND a.scheduled_date BETWEEN #{startDate} AND #{endDate} AND a.deleted=0 AND p.garden_id=#{gardenId} " +
            "ORDER BY a.scheduled_date ASC,a.id ASC")
    List<CareActivityEntity> selectByDateRangeWithGarden(@Param("userId") Long userId,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate,
                                                         @Param("gardenId") Long gardenId);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND a.id=#{activityId} AND a.deleted=0 LIMIT 1")
    CareActivityEntity selectByUserIdAndId(@Param("userId") Long userId, @Param("activityId") Long activityId);

    @Update("UPDATE care_activity SET status='COMPLETED',completed_at=NOW(),record_json=#{recordJson},updated_at=NOW() " +
            "WHERE user_id=#{userId} AND id=#{activityId} AND deleted=0")
    int complete(@Param("userId") Long userId, @Param("activityId") Long activityId, @Param("recordJson") String recordJson);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND a.plant_id=#{plantId} AND a.deleted=0 AND a.status='COMPLETED' " +
            "ORDER BY a.completed_at DESC,a.id DESC LIMIT #{limit} OFFSET #{offset}")
    List<CareActivityEntity> selectCompletedByPlantPaged(@Param("userId") Long userId,
                                                         @Param("plantId") Long plantId,
                                                         @Param("offset") Integer offset,
                                                         @Param("limit") Integer limit);

    @Select("SELECT COUNT(1) FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED'")
    long countCompletedByPlant(@Param("userId") Long userId, @Param("plantId") Long plantId);

    @Select("SELECT a.id,a.user_id,a.plant_id,a.plan_id,a.rule_id,a.activity_type,a.scheduled_date,a.status,a.completed_at,a.record_json,p.name AS plant_name " +
            "FROM care_activity a INNER JOIN plant p ON a.plant_id=p.id " +
            "WHERE a.user_id=#{userId} AND a.plant_id=#{plantId} AND a.deleted=0 AND a.status='COMPLETED' AND a.activity_type='photo' " +
            "AND (JSON_EXTRACT(a.record_json,'$.photo') IS NOT NULL OR JSON_EXTRACT(a.record_json,'$.photos') IS NOT NULL) " +
            "ORDER BY a.completed_at DESC,a.id DESC LIMIT #{limit} OFFSET #{offset}")
    List<CareActivityEntity> selectCompletedPhotosByPlantPaged(@Param("userId") Long userId,
                                                               @Param("plantId") Long plantId,
                                                               @Param("offset") Integer offset,
                                                               @Param("limit") Integer limit);

    @Select("SELECT COUNT(1) FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' AND activity_type='photo' " +
            "AND (JSON_EXTRACT(record_json,'$.photo') IS NOT NULL OR JSON_EXTRACT(record_json,'$.photos') IS NOT NULL)")
    long countCompletedPhotosByPlant(@Param("userId") Long userId, @Param("plantId") Long plantId);

    @Select("SELECT COUNT(1) FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' " +
            "AND DATE(completed_at) BETWEEN #{startDate} AND #{endDate}")
    long countCompletedByPlantBetweenDates(@Param("userId") Long userId,
                                           @Param("plantId") Long plantId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    @Select("SELECT DATE_FORMAT(MAX(completed_at),'%Y-%m-%d') FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED'")
    String selectLatestCompletedDateByPlant(@Param("userId") Long userId, @Param("plantId") Long plantId);

    @Select("SELECT COUNT(1) FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' AND activity_type=#{activityType}")
    long countCompletedByPlantAndType(@Param("userId") Long userId,
                                      @Param("plantId") Long plantId,
                                      @Param("activityType") String activityType);

    @Select("SELECT 'morning' AS segment,COUNT(1) AS count FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' AND activity_type='water' " +
            "AND HOUR(completed_at) BETWEEN 5 AND 10 " +
            "UNION ALL " +
            "SELECT 'daytime' AS segment,COUNT(1) AS count FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' AND activity_type='water' " +
            "AND HOUR(completed_at) BETWEEN 11 AND 16 " +
            "UNION ALL " +
            "SELECT 'evening' AS segment,COUNT(1) AS count FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' AND activity_type='water' " +
            "AND HOUR(completed_at) BETWEEN 17 AND 21 " +
            "UNION ALL " +
            "SELECT 'night' AS segment,COUNT(1) AS count FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' AND activity_type='water' " +
            "AND (HOUR(completed_at) >= 22 OR HOUR(completed_at) <= 4)")
    List<cn.skylark.xiaolvxingqiu.boot.model.WateringTimeSegmentCount> selectWateringTimeDistribution(@Param("userId") Long userId,
                                                                                                       @Param("plantId") Long plantId);

    @Select("SELECT COUNT(1) FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' AND activity_type='water' " +
            "AND NOT ((HOUR(completed_at) BETWEEN 5 AND 10) OR (HOUR(completed_at) BETWEEN 17 AND 21))")
    long countWateringOutsideRecommendedTime(@Param("userId") Long userId, @Param("plantId") Long plantId);

    @Select("SELECT DATE_FORMAT(completed_at,'%Y-%m') AS month,COUNT(1) AS care_count FROM care_activity " +
            "WHERE user_id=#{userId} AND plant_id=#{plantId} AND deleted=0 AND status='COMPLETED' " +
            "AND DATE(completed_at) BETWEEN #{startDate} AND #{endDate} " +
            "GROUP BY DATE_FORMAT(completed_at,'%Y-%m') ORDER BY month ASC")
    List<cn.skylark.xiaolvxingqiu.boot.model.PlantMonthlyCount> selectCompletedMonthlyCountsByPlant(@Param("userId") Long userId,
                                                                                                     @Param("plantId") Long plantId,
                                                                                                     @Param("startDate") LocalDate startDate,
                                                                                                     @Param("endDate") LocalDate endDate);
}
