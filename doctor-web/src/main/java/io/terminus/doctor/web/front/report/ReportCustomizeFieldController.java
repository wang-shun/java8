package io.terminus.doctor.web.front.report;

import com.fasterxml.jackson.annotation.JsonView;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesReadService;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesWriteService;
import io.terminus.doctor.basic.service.DoctorReportFieldsReadService;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by sunbo@terminus.io on 2017/12/27.
 */
@RestController
@RequestMapping("/api/doctor/report/")
public class ReportCustomizeFieldController {


    @RpcConsumer
    private DoctorReportFieldsReadService doctorReportFieldsReadService;
    @RpcConsumer
    private DoctorReportFieldCustomizesReadService doctorReportFieldCustomizesReadService;
    @RpcConsumer
    private DoctorReportFieldCustomizesWriteService doctorReportFieldCustomizesWriteService;

    /**
     * 获取字段
     */
    @RequestMapping(method = RequestMethod.GET, value = "field")
    public Response<List<DoctorReportFieldTypeDto>> getField() {
        return doctorReportFieldsReadService.listAll();
    }

    /**
     * 获取指定类型下启用的字段
     */
    @RequestMapping(method = RequestMethod.GET, value = "field/{farmId}/{id}/customize")
    public Response<List<Long>> getSelectedField(@PathVariable("id") Long typeId, @PathVariable Long farmId) {
        return doctorReportFieldCustomizesReadService.getSelected(typeId, farmId);
    }

    /**
     * 获取所有类型下启用的字段
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "field/{farmId}/customize")
    public Response<List<DoctorReportFieldTypeDto>> getSelectedField(@PathVariable Long farmId) {
        return doctorReportFieldCustomizesReadService.getSelected(farmId);
    }

    /**
     * 获取所有类型下所有字段，并标记可显示的字段
     */
    @RequestMapping(method = RequestMethod.GET, value = "field/{farmId}/customize/all")
    public Response<List<DoctorReportFieldTypeDto>> getAllFieldWithSelected(@PathVariable Long farmId,Integer type) {
        return doctorReportFieldCustomizesReadService.getAllWithSelected(farmId,type);
    }


    /**
     * 设置指定类型的需要显示的字段
     */
    @RequestMapping(method = RequestMethod.PUT, value = "field/{farmId}/{id}/customize/{type}")
    public void selectField(@PathVariable Long farmId,@PathVariable Integer type, @PathVariable("id") Long typeId, @RequestBody List<Long> fieldIds) {

        DoctorReportFieldTypeDto fieldDto = new DoctorReportFieldTypeDto();
        fieldDto.setId(typeId);
        fieldDto.setFields(fieldIds.stream().map(id -> {
            DoctorReportFieldTypeDto.DoctorReportFieldDto child = new DoctorReportFieldTypeDto.DoctorReportFieldDto();
            child.setId(id);
            return child;
        }).collect(Collectors.toList()));
        doctorReportFieldCustomizesWriteService.customize(farmId, fieldDto,type);
    }

    /**
     * 设置所有类型的需要显示的字段
     */
    @RequestMapping(method = RequestMethod.PUT, value = "field/{farmId}/customize/{type}")
    public void selectField(@PathVariable Long farmId,@PathVariable Integer type, @RequestBody @Valid List<DoctorReportFieldTypeDto> fieldDto, Errors errors) {
        if (errors.hasErrors())
            throw new ServiceException(errors.getFieldError().getDefaultMessage());
        doctorReportFieldCustomizesWriteService.customize(farmId, fieldDto,type);
    }

}
