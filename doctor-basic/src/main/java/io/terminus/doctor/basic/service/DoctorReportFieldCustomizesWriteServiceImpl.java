package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorReportFieldCustomizesDao;
import io.terminus.doctor.basic.dao.DoctorReportFieldsDao;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.model.DoctorReportFieldCustomizes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 17:11:01
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorReportFieldCustomizesWriteServiceImpl implements DoctorReportFieldCustomizesWriteService {

    @Autowired
    private DoctorReportFieldCustomizesDao doctorReportFieldCustomizesDao;

    private DoctorReportFieldsDao doctorReportFieldsDao;

    @Override
    public Response<Long> create(DoctorReportFieldCustomizes doctorReportFieldCustomizes) {
        try {
            doctorReportFieldCustomizesDao.create(doctorReportFieldCustomizes);
            return Response.ok(doctorReportFieldCustomizes.getId());
        } catch (Exception e) {
            log.error("failed to create doctor report field customizes, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.create.fail");
        }
    }

    @Override
    public Response<Boolean> update(DoctorReportFieldCustomizes doctorReportFieldCustomizes) {
        try {
            return Response.ok(doctorReportFieldCustomizesDao.update(doctorReportFieldCustomizes));
        } catch (Exception e) {
            log.error("failed to update doctor report field customizes, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.update.fail");
        }
    }

    @Override
    public Response<Boolean> delete(Long id) {
        try {
            return Response.ok(doctorReportFieldCustomizesDao.delete(id));
        } catch (Exception e) {
            log.error("failed to delete doctor report field customizes by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.delete.fail");
        }
    }

    @Override
    @Transactional
    public Response<Boolean> customize(Long farmId, DoctorReportFieldTypeDto fieldDto,Integer type) {
        try {

            //删除原有的绑定关系
            doctorReportFieldCustomizesDao.deleteByFarmAndType(farmId, Collections.singletonList(fieldDto.getId()),type);


            fieldDto.getFields().stream().forEach(f -> {
                DoctorReportFieldCustomizes customizes = new DoctorReportFieldCustomizes();
                customizes.setTypeId(fieldDto.getId());
                customizes.setFarmId(farmId);
                customizes.setFieldId(f.getId());
                customizes.setType(type);
                doctorReportFieldCustomizesDao.create(customizes);
            });


            return Response.ok(true);
        } catch (Exception e) {
            log.error("failed to update doctor report field customizes, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.update.fail");
        }
    }

    @Override
    @Transactional
    public Response<Boolean> customize(Long farmId, List<DoctorReportFieldTypeDto> fieldDto,Integer type) {

        try {
            log.error("farmId="+farmId+"===========type="+type);
            //删除原有的绑定关系
            doctorReportFieldCustomizesDao.deleteByFarmAndType(farmId, fieldDto.stream().map(DoctorReportFieldTypeDto::getId).collect(Collectors.toList()),type);


            fieldDto.stream().forEach(f -> {
                f.getFields().stream().forEach(fi -> {
                    DoctorReportFieldCustomizes customizes = new DoctorReportFieldCustomizes();
                    customizes.setTypeId(f.getId());
                    customizes.setFarmId(farmId);
                    customizes.setFieldId(fi.getId());
                    customizes.setType(type);
                    doctorReportFieldCustomizesDao.create(customizes);
                });
            });

            return Response.ok(true);
        } catch (Exception e) {
            log.error("failed to update doctor report field customizes, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.update.fail");
        }
    }
}