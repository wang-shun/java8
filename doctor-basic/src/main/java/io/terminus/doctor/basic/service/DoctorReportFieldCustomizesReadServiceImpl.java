package io.terminus.doctor.basic.service;

import io.terminus.doctor.basic.dao.DoctorReportFieldCustomizesDao;
import io.terminus.doctor.basic.dto.DoctorReportFieldDto;
import io.terminus.doctor.basic.model.DoctorReportFieldCustomizes;

import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.boot.rpc.common.annotation.RpcProvider;

import com.google.common.base.Throwables;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesReadService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.util.ArrayList;
import java.util.Map;
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
public class DoctorReportFieldCustomizesReadServiceImpl implements DoctorReportFieldCustomizesReadService {

    @Autowired
    private DoctorReportFieldCustomizesDao doctorReportFieldCustomizesDao;

    @Override
    public Response<DoctorReportFieldCustomizes> findById(Long id) {
        try {
            return Response.ok(doctorReportFieldCustomizesDao.findById(id));
        } catch (Exception e) {
            log.error("failed to find doctor report field customizes by id:{}, cause:{}", id, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorReportFieldCustomizes>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        try {
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            return Response.ok(doctorReportFieldCustomizesDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), criteria));
        } catch (Exception e) {
            log.error("failed to paging doctor report field customizes by pageNo:{} pageSize:{}, cause:{}", pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.paging.fail");
        }
    }

    @Override
    public Response<List<DoctorReportFieldCustomizes>> list(Map<String, Object> criteria) {
        try {
            return Response.ok(doctorReportFieldCustomizesDao.list(criteria));
        } catch (Exception e) {
            log.error("failed to list doctor report field customizes, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.list.fail");
        }
    }

    @Override
    public Response<List<Long>> getSelected(Long typeId) {
        try {
            return Response.ok(doctorReportFieldCustomizesDao.
                    list(DoctorReportFieldCustomizes.builder()
                            .typeId(typeId)
                            .build())
                    .stream()
                    .map(DoctorReportFieldCustomizes::getFieldId)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("failed to list doctor report field customizes, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.list.fail");
        }
    }

    @Override
    public Response<List<DoctorReportFieldDto>> getSelected() {
        Map<Long, List<DoctorReportFieldCustomizes>> typeCustomizes = doctorReportFieldCustomizesDao.
                list(DoctorReportFieldCustomizes.builder()
                        .build()).stream().collect(Collectors.groupingBy(DoctorReportFieldCustomizes::getTypeId));
        List<DoctorReportFieldDto> result = new ArrayList<>(typeCustomizes.size());

        typeCustomizes.forEach((k, v) -> {
            DoctorReportFieldDto fieldDto = new DoctorReportFieldDto();
            fieldDto.setId(k);
            fieldDto.setFields(v.stream().map(c -> {
                DoctorReportFieldDto child = new DoctorReportFieldDto();
                child.setId(c.getFieldId());
                return child;
            }).collect(Collectors.toList()));
            result.add(fieldDto);
        });

        return Response.ok(result);
    }
}
