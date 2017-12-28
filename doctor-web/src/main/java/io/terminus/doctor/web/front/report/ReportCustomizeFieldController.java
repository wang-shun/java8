package io.terminus.doctor.web.front.report;

import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorReportFieldDto;
import io.terminus.doctor.basic.model.DoctorReportFields;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesReadService;
import io.terminus.doctor.basic.service.DoctorReportFieldCustomizesWriteService;
import io.terminus.doctor.basic.service.DoctorReportFieldsReadService;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.pampas.engine.utils.FileLoader;
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
    public Response<List<DoctorReportFieldDto>> getField() {
        return doctorReportFieldsReadService.listAll();
    }

    /**
     * 获取指定类型下启用的字段
     */
    @RequestMapping(method = RequestMethod.GET, value = "field/{id}/customize")
    public Response<List<Long>> getSelectedField(@PathVariable("id") Long typeId) {
        return doctorReportFieldCustomizesReadService.getSelected(typeId);
    }

    /**
     * 获取所有类型下启用的字段
     *
     * @return
     */
    @RequestMapping(method = RequestMethod.GET, value = "field/customize")
    public Response<List<DoctorReportFieldDto>> getSelectedField() {
        return doctorReportFieldCustomizesReadService.getSelected();
    }


    /**
     * 设置指定类型的需要显示的字段
     */
    @RequestMapping(method = RequestMethod.PUT, value = "field/{id}/customize")
    public void selectField(@PathVariable("id") Long typeId, @RequestBody List<Long> fieldIds) {

        DoctorReportFieldDto fieldDto = new DoctorReportFieldDto();
        fieldDto.setId(typeId);
        fieldDto.setFields(fieldIds.stream().map(id -> {
            DoctorReportFieldDto child = new DoctorReportFieldDto();
            child.setId(id);
            return child;
        }).collect(Collectors.toList()));
        doctorReportFieldCustomizesWriteService.customize(fieldDto);
    }

    /**
     * 设置所有类型的需要显示的字段
     */
    @RequestMapping(method = RequestMethod.PUT, value = "field/customize")
    public void selectField(@RequestBody @Valid List<DoctorReportFieldDto> fieldDto, Errors errors) {
        if (errors.hasErrors())
            throw new ServiceException(errors.getFieldError().getDefaultMessage());

        doctorReportFieldCustomizesWriteService.customize(fieldDto);
    }

}
