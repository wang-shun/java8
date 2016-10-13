package io.terminus.doctor.msg.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.msg.model.DoctorMessageRule;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Code generated by terminus code gen
 * Desc: 猪场软件消息规则表Dao类
 * Date: 2016-05-30
 * Author: chk@terminus.io
 */
@Repository
public class DoctorMessageRuleDao extends MyBatisDao<DoctorMessageRule> {

    /**
     * 获取猪场和规则关系
     * @param templateId    模板id
     * @param farmId        猪场id
     * @return
     */
    public DoctorMessageRule findByTplAndFarm(Long templateId, Long farmId) {
        return getSqlSession().selectOne(sqlId("findByTplAndFarm"), ImmutableMap.of("farmId", farmId, "templateId", templateId));
    }

    /**
     * 根据模板id获取与猪场绑定的信息列表
     * @param tplId 模板id
     * @return
     */
    public List<DoctorMessageRule> findByTpl(Long tplId) {
        return getSqlSession().selectList(sqlId("findByTpl"), tplId);
    }

    /**
     * 根据模板id获取与猪场绑定的信息列表
     * @param tplId 模板id
     * @return
     */
    public List<DoctorMessageRule> findNormalByTpl(Long tplId) {
        return getSqlSession().selectList(sqlId("findNormalByTpl"), tplId);
    }

    /**
     * 根据猪场id查询
     * @param farmId    猪场id
     * @return
     */
    public List<DoctorMessageRule> findByFarmId(Long farmId) {
        return getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    /**
     * 根据 farmId和templateName 查询
     * @param farmId
     * @param templateName
     * @return
     */
    public List<DoctorMessageRule> findByFarmIdAndTemplateName(Long farmId,String templateName) {
        return getSqlSession().selectList(sqlId("findByFarmIdAndTemplateName"), ImmutableMap.of("farmId",farmId,"templateName",templateName));
    }
}
