package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorBasicMaterialDao;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.common.enums.DataEventType;
import io.terminus.doctor.common.event.DataEvent;
import io.terminus.zookeeper.pubsub.Publisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Desc: 基础物料表写服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-16
 */
@Slf4j
@Service
@RpcProvider
public class DoctorBasicMaterialWriteServiceImpl implements DoctorBasicMaterialWriteService {

    private final DoctorBasicMaterialDao doctorBasicMaterialDao;

    @Autowired(required = false)
    private Publisher publisher;

    @Autowired
    public DoctorBasicMaterialWriteServiceImpl(DoctorBasicMaterialDao doctorBasicMaterialDao) {
        this.doctorBasicMaterialDao = doctorBasicMaterialDao;
    }

    @Override
    public Response<Long> createBasicMaterial(DoctorBasicMaterial basicMaterial) {
        try {
            doctorBasicMaterialDao.create(basicMaterial);
            publishMaterialInfo(DataEventType.MaterialInfoCreateEvent.getKey(), ImmutableMap.of("materialInfoCreatedId", basicMaterial.getId()));
            return Response.ok(basicMaterial.getId());
        } catch (Exception e) {
            log.error("create basicMaterial failed, basicMaterial:{}, cause:{}", basicMaterial, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.create.fail");
        }
    }

    @Override
    public Response<Boolean> updateBasicMaterial(DoctorBasicMaterial basicMaterial) {
        try {
            doctorBasicMaterialDao.update(basicMaterial);
            publishMaterialInfo(DataEventType.MaterialInfoCreateEvent.getKey(), ImmutableMap.of("materialInfoCreatedId", basicMaterial.getId()));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("update basicMaterial failed, basicMaterial:{}, cause:{}", basicMaterial, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.update.fail");
        }
    }

    @Override
    public Response<Boolean> deleteBasicMaterialById(Long basicMaterialId) {
        try {
            doctorBasicMaterialDao.delete(basicMaterialId);
            publishMaterialInfo(DataEventType.MaterialInfoCreateEvent.getKey(), ImmutableMap.of("materialInfoCreatedId", basicMaterialId));
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("delete basicMaterial failed, basicMaterialId:{}, cause:{}", basicMaterialId, Throwables.getStackTraceAsString(e));
            return Response.fail("basicMaterial.delete.fail");
        }
    }

    private <T> void publishMaterialInfo(Integer eventType, T data){
        if(!Objects.isNull(publisher)){
            try{
                publisher.publish(DataEvent.toBytes(eventType, data));
            }catch (Exception e){
                log.error("material info publisher error, eventType:{} data:{} cause:{}", eventType, data, Throwables.getStackTraceAsString(e));
            }
        }
    }
}
