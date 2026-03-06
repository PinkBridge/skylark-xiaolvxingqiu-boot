package cn.skylark.xiaolvxingqiu.boot.mapper;

import cn.skylark.xiaolvxingqiu.boot.model.UserCoinAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserCoinMapper {

    @Insert("INSERT IGNORE INTO user_coin_account (user_id,coin_balance,completed_activity_total,progress_activity_count,created_at,updated_at) " +
            "VALUES (#{userId},0,0,0,NOW(),NOW())")
    int insertAccountIfAbsent(@Param("userId") Long userId);

    @Select("SELECT user_id,coin_balance,completed_activity_total,progress_activity_count FROM user_coin_account WHERE user_id=#{userId} LIMIT 1")
    UserCoinAccount selectAccountByUserId(@Param("userId") Long userId);

    @Update("UPDATE user_coin_account SET coin_balance=#{coinBalance},completed_activity_total=#{completedActivityTotal},progress_activity_count=#{progressActivityCount},updated_at=NOW() " +
            "WHERE user_id=#{userId}")
    int updateAccount(UserCoinAccount account);

    @Insert("INSERT IGNORE INTO user_coin_txn (user_id,change_amount,reason,related_date,meta_json,created_at) " +
            "VALUES (#{userId},#{changeAmount},#{reason},#{relatedDate},#{metaJson},NOW())")
    int insertTxnIgnoreDuplicate(@Param("userId") Long userId,
                                 @Param("changeAmount") Long changeAmount,
                                 @Param("reason") String reason,
                                 @Param("relatedDate") String relatedDate,
                                 @Param("metaJson") String metaJson);
}
