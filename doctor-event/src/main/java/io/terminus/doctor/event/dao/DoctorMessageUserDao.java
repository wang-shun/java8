package io.terminus.doctor.event.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.msg.DoctorMessageUserDto;
import io.terminus.doctor.event.model.DoctorMessageUser;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by xiao on 16/10/11.
 */
@Repository
public class DoctorMessageUserDao extends MyBatisDao<DoctorMessageUser> {

    /**
     * 获取未读站内信的数量
     * @param userId    用户id
     * @return
     */
    public Long findNoReadCount(Long userId) {
        return getSqlSession().selectOne(sqlId("findNoReadCount"), userId);
    }

    /**
     * 根据条件获取businessId列表
     * @param doctorMessageUserDto
     * @return
     */
    public List<Long> findBusinessListByCriteria(DoctorMessageUserDto doctorMessageUserDto){
        return getSqlSession().selectList(sqlId("businessIdList"), doctorMessageUserDto);
    }

    /**
     * 根据消息id删除关联messageuser
     * @param messageId
     */
    public void deleteByMessageId(Long messageId) {
        getSqlSession().delete(sqlId("deleteByMessageId"), messageId);
    }

    /**
     * 根据消息id批量删除关联messageuser
     * @param messageIds
     */
    public void deletesByMessageIds(List<Long> messageIds) {
        getSqlSession().delete(sqlId("deletesByMessageIds"), messageIds);
    }
}
