package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 添加pig事件读取信息
 */
@Service
@Slf4j
public class DoctorPigEventReadServiceImpl implements DoctorPigEventReadService{

    private final DoctorPigEventDao doctorPigEventDao;

    public DoctorPigEventReadServiceImpl(DoctorPigEventDao doctorPigEventDao){
        this.doctorPigEventDao = doctorPigEventDao;
    }

    @Override
    public Response<Map<String, List<Integer>>> queryPigEventByPigStatus(List<Integer> statusList) {
        // TODO 母猪状态流转图 前段显示的方式，数据内容的封装
        return null;
    }

    @Override
    public Response<Paging<DoctorPigEvent>> queryPigDoctorEvents(Long farmId, Long pigId,
                                                                 Integer pageNo, Integer pageSize,
                                                                 Date beginDate, Date endDate) {
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            if(isNull(farmId) || isNull(pigId)){
                return Response.fail("query.pigDoctorEvents.fail");
            }
            Map<String,Object> criteria = Maps.newHashMap();
            criteria.put("farmId", farmId);
            criteria.put("pigId", pigId);
            criteria.put("beginDate",beginDate);
            criteria.put("endDate", endDate);
            return Response.ok(doctorPigEventDao.paging(pageInfo.getOffset(),pageInfo.getLimit(), criteria));
        }catch (Exception e){
            log.error("query pig doctor events fail, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("query.pigEvent.fail");
        }
    }
}
