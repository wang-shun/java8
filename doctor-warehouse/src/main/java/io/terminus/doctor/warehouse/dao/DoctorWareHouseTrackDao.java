package io.terminus.doctor.warehouse.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.warehouse.model.DoctorWareHouseTrack;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public class DoctorWareHouseTrackDao extends MyBatisDao<DoctorWareHouseTrack>{

    public List<DoctorWareHouseTrack> queryByWareHouseId(List<Long> ids){
        return this.getSqlSession().selectList(sqlId("queryByWareHouseIds"),ids);
    }
}
