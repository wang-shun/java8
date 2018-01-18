package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorReportFieldsDao;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.enums.DoctorReportFieldType;
import io.terminus.doctor.basic.model.DoctorReportFields;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2017-12-27 16:19:39
 * Created by [ your name ]
 */
@Slf4j
@Service
@RpcProvider
public class DoctorReportFieldsReadServiceImpl implements DoctorReportFieldsReadService {

    @Autowired
    private DoctorReportFieldsDao doctorReportFieldsDao;

    @Override
    public Response<DoctorReportFields> findById(Long id) {
        try {
            return Response.ok(doctorReportFieldsDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor report fields by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.fields.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorReportFields>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorReportFieldsDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor report fields by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.fields.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorReportFields>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorReportFieldsDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor report fields, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.fields.list.fail");
        }
    }

    @Override
    @Cacheable(value = "", key = "'doctor_report_field_all'")
    public Response<List<DoctorReportFieldTypeDto>> listAll() {

        try {
            List<DoctorReportFields> allTypes = doctorReportFieldsDao.list(DoctorReportFields.builder()
                    .type(DoctorReportFieldType.TYPE.getValue())
                    .build());

            Map<Long, List<DoctorReportFields>> allFields = doctorReportFieldsDao.list(DoctorReportFields.builder()
                    .type(DoctorReportFieldType.FIELD.getValue())
                    .build()).stream().collect(Collectors.groupingBy(DoctorReportFields::getFId));


            List<DoctorReportFieldTypeDto> result = new ArrayList<>(allTypes.size());

            allTypes.stream().forEach(type -> {
                DoctorReportFieldTypeDto fieldDto = new DoctorReportFieldTypeDto();
                fieldDto.setId(type.getId());
                fieldDto.setName(type.getName());
                fieldDto.setReportField(type.getReportField());


                if (allFields.containsKey(type.getId())) {
                    fieldDto.setFields(allFields.get(type.getId()).stream().map(field -> {
                        DoctorReportFieldTypeDto.DoctorReportFieldDto child = new DoctorReportFieldTypeDto.DoctorReportFieldDto();
                        child.setId(field.getId());
                        child.setName(field.getName());
                        child.setReportField(field.getReportField());
                        return child;
                    }).collect(Collectors.toList()));
                } else {
                    fieldDto.setFields(Collections.emptyList());
                }

                result.add(fieldDto);
            });
            return Response.ok(result);
        } catch (Exception e) {
            log.error("failed to list doctor report fields, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.fields.list.fail");
        }
    }
}
