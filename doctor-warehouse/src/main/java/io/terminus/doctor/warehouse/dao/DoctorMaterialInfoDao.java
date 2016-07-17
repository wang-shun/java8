package io.terminus.doctor.warehouse.dao;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import org.springframework.stereotype.Repository;

import java.util.List;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorMaterialInfoDao extends MyBatisDao<DoctorMaterialInfo>{

    public List<DoctorMaterialInfo> findByFarmId(Long farmId){
        return this.getSqlSession().selectList(sqlId("findByFarmId"), farmId);
    }

    public List<DoctorMaterialInfo> findByFarmIdType(Long farmId, Integer type){
        return this.getSqlSession().selectList(sqlId("findByFarmIdType"), ImmutableMap.of("farmId",farmId,"type",type));
    }

    /**
     * 创建或者修改原料生产信息
     * @param doctorMaterialInfo
     * @return
     */
    public Boolean createOrUpdate(DoctorMaterialInfo doctorMaterialInfo){
        DoctorMaterialInfo exist =  this.findById(doctorMaterialInfo.getId());
        if(isNull(exist)){
            doctorMaterialInfo.setCreatorId(exist.getCreatorId());
            doctorMaterialInfo.setCreatorName(exist.getCreatorName());
            return this.create(doctorMaterialInfo);
        }else {
            return this.update(doctorMaterialInfo);
        }
    }

    /**
     * 物料的当前最大的id, 这个是dump搜素引擎用的
     *
     * @return 当前最大的id
     */
    public Long maxId() {
        Long count = getSqlSession().selectOne(sqlId("maxId"));
        return MoreObjects.firstNonNull(count, 0L);
    }

    /**
     * 查询id小于lastId内且更新时间大于since的limit个猪, 这个是dump搜素引擎用的
     *
     * @param lastId lastId 最大的猪id
     * @param since  起始更新时间
     * @param limit  个数
     */
    public List<DoctorMaterialInfo> listSince(Long lastId, String since, int limit) {
        return getSqlSession().selectList(sqlId("listSince"),
                ImmutableMap.of("lastId", lastId, "limit", limit, "since", since));
    }
}
