package io.terminus.doctor.basic.dao;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.basic.model.DoctorWareHouseTrack;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-17
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Repository
public class DoctorWareHouseTrackDao extends MyBatisDao<DoctorWareHouseTrack>{

    public List<DoctorWareHouseTrack> queryByWareHouseId(List<Long> ids){
        return this.getSqlSession().selectList(sqlId("queryByWareHouseIds"),ids);
    }

    public void updateAll(DoctorWareHouseTrack track){
        sqlSession.update(sqlId("updateAll"), track);
    }
}
