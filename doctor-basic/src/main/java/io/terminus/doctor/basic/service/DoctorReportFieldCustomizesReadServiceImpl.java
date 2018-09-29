package io.terminus.doctor.basic.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dao.DoctorReportFieldCustomizesDao;
import io.terminus.doctor.basic.dao.DoctorReportFieldsDao;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.model.DoctorReportFieldCustomizes;
import io.terminus.doctor.common.utils.RespHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    @Autowired
    private DoctorReportFieldsReadService doctorReportFieldsReadService;

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
    public Response<List<Long>> getSelected(Long typeId, Long farmId) {
        try {
            return Response.ok(doctorReportFieldCustomizesDao.
                    list(DoctorReportFieldCustomizes.builder()
                            .farmId(farmId)
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
    public Response<List<DoctorReportFieldTypeDto>> getSelected(Long farmId) {
        try {
            Map<Long, List<DoctorReportFieldCustomizes>> typeCustomizes = doctorReportFieldCustomizesDao.
                    list(DoctorReportFieldCustomizes.builder()
                            .farmId(farmId)
                            .build()).stream().collect(Collectors.groupingBy(DoctorReportFieldCustomizes::getTypeId));
            List<DoctorReportFieldTypeDto> result = new ArrayList<>(typeCustomizes.size());

            typeCustomizes.forEach((k, v) -> {
                DoctorReportFieldTypeDto fieldDto = new DoctorReportFieldTypeDto();
                fieldDto.setId(k);
                fieldDto.setFields(v.stream().map(c -> {
                    DoctorReportFieldTypeDto.DoctorReportFieldDto child = new DoctorReportFieldTypeDto.DoctorReportFieldDto();
                    child.setHidden(false);
                    child.setId(c.getFieldId());
                    return child;
                }).collect(Collectors.toList()));
                result.add(fieldDto);
            });

            return Response.ok(result);
        } catch (Exception e) {
            log.error("failed to list doctor report field customizes, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.list.fail");
        }
    }

    @Override
    public Response<List<DoctorReportFieldTypeDto>> getAllWithSelected(Long farmId, Integer type) {
        try {
            Map<Long, List<DoctorReportFieldCustomizes>> selectedTypeCustomizes = doctorReportFieldCustomizesDao.
                    list(DoctorReportFieldCustomizes.builder()
                            .farmId(farmId).type(type)
                            .build()).stream().collect(Collectors.groupingBy(DoctorReportFieldCustomizes::getTypeId));


            List<DoctorReportFieldTypeDto> types = RespHelper.or500(doctorReportFieldsReadService.listAll());

            types.forEach(t -> {
                if (!selectedTypeCustomizes.containsKey(t.getId())) {
                    //这个类型没有一个需要显示的字段
                    t.getFields().stream().forEach(f -> { //全部设置为隐藏
                        f.setHidden(true);
                    });

                } else {
                    List<DoctorReportFieldCustomizes> selectedFields = selectedTypeCustomizes.get(t.getId());
                    for (DoctorReportFieldTypeDto.DoctorReportFieldDto fieldDto : t.getFields()) {
                        fieldDto.setHidden(true);//默认设置成隐藏
                        for (DoctorReportFieldCustomizes selected : selectedFields) {
                            if (fieldDto.getId().longValue() == selected.getFieldId().longValue()) {
                                fieldDto.setHidden(false);//如果设置成显示就显示
                            }
                        }
                    }
                }
            });


            return Response.ok(types);
        } catch (Exception e) {
            log.error("failed to list doctor report field customizes, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("doctor.report.field.customizes.list.fail");
        }
    }
}
