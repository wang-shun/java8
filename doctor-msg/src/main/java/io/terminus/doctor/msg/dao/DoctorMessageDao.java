package io.terminus.doctor.msg.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.msg.dto.DoctorMessageSearchDto;
import io.terminus.doctor.msg.model.DoctorMessage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Code generated by terminus code gen
 * Desc: 猪场软件消息表Dao类
 * Date: 2016-05-30
 * Author: chk@terminus.io
 */
@Repository
public class DoctorMessageDao extends MyBatisDao<DoctorMessage> {

    /**
     * 获取未读站内信的数量
     * @param userId    用户id
     * @return
     */
    public Long findNoReadCount(Long userId) {
        return getSqlSession().selectOne(sqlId("findNoReadCount"), userId);
    }

    /**
     * 获取系统消息(最新)
     * @param templateId    模板id
     * @return
     */
    public DoctorMessage findLatestSysMessage(Long templateId) {
        return getSqlSession().selectOne(sqlId("findLatestSysMessage"), templateId);
    }

    /**
     * 获取预警消息(最新)
     * @param templateId    模板id
     * @param farmId        猪场id
     * @param roleId        角色id
     * @return
     */
    public DoctorMessage findLatestWarnMessage(Long templateId, Long farmId, Long roleId) {
        return getSqlSession().selectOne(sqlId("findLatestWarnMessage"),
                ImmutableMap.of("templateId", templateId, "farmId", farmId, "roleId", roleId));
    }

    /**
     * 获取预警消息(最新)
     * @param templateId    模板id
     * @param farmId        猪场id
     * @return
     */
    public DoctorMessage findLatestWarnMessage(Long templateId, Long farmId) {
        return getSqlSession().selectOne(sqlId("findLatestWarnMessage2"),
                ImmutableMap.of("templateId", templateId, "farmId", farmId));
    }

    /**
     * 根据条件获取businessId列表
     * @param doctorMessageSearchDto
     * @return
     */
    public List<Long> findBusinessListByCriteria(DoctorMessageSearchDto doctorMessageSearchDto){
        return getSqlSession().selectList(sqlId("businessIdList"), doctorMessageSearchDto);
    }

    /**
     * 获取发送消息
     * @param criteria
     * @return
     */
    public List<DoctorMessage> sendMessageList(Map<String, Object> criteria){
        return getSqlSession().selectList(sqlId("sendList"), criteria);
    }

}
