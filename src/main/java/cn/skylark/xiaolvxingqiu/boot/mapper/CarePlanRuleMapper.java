package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.CarePlanRuleEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CarePlanRuleMapper {

    @Select("SELECT id,user_id,plan_id,activity_type,season,enabled,interval_days,next_due_date FROM care_plan_rule WHERE user_id=#{userId} AND plan_id=#{planId} AND deleted=0 ORDER BY id ASC")
    List<CarePlanRuleEntity> selectByPlanId(@Param("userId") Long userId, @Param("planId") Long planId);

    @Update("UPDATE care_plan_rule SET deleted=1,updated_at=NOW() WHERE user_id=#{userId} AND plan_id=#{planId} AND deleted=0")
    int softDeleteByPlanId(@Param("userId") Long userId, @Param("planId") Long planId);

    @Insert("INSERT INTO care_plan_rule (user_id,plan_id,activity_type,season,enabled,interval_days,next_due_date,deleted,created_at,updated_at) " +
            "VALUES (#{userId},#{planId},#{activityType},#{season},#{enabled},#{intervalDays},#{nextDueDate},0,NOW(),NOW())")
    int insert(CarePlanRuleEntity rule);

    @Select("SELECT r.id,r.user_id,r.plan_id,r.activity_type,r.season,r.enabled,r.interval_days,r.next_due_date " +
            "FROM care_plan_rule r INNER JOIN care_plan p ON r.plan_id=p.id " +
            "WHERE r.user_id=#{userId} AND r.deleted=0 AND r.enabled=1 AND r.next_due_date<=#{targetDate} " +
            "AND p.deleted=0 AND p.enabled=1")
    List<CarePlanRuleEntity> selectDueRulesByUserAndDate(@Param("userId") Long userId, @Param("targetDate") LocalDate targetDate);

    @Update("UPDATE care_plan_rule SET next_due_date=#{nextDueDate},updated_at=NOW() WHERE id=#{ruleId} AND deleted=0")
    int updateNextDueDate(@Param("ruleId") Long ruleId, @Param("nextDueDate") LocalDate nextDueDate);
}
