package io.terminus.doctor.web.front.report;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportSow;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * author:xjn
 * email:xiaojiannan@terminus.io
 * date:18/5/14
 */
@Api("APP端报表")
@RestController
public class AppReportController {

    @ApiOperation("查询母猪区详情报表")
    @RequestMapping(value = "/query/report/sow/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportSowDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @ApiOperation("查询公猪区详情报表")
    @RequestMapping(value = "/query/report/boar/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportBoarDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @ApiOperation("查询产房区详情报表")
    @RequestMapping(value = "/query/report/Deliver/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportDeliverDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @ApiOperation("查询效率指标详情报表")
    @RequestMapping(value = "/query/report/efficiency/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportEfficiencyDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @ApiOperation("查询育肥区详情报表")
    @RequestMapping(value = "/query/report/fatten/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportFattenDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @ApiOperation("查询物料详情报表")
    @RequestMapping(value = "/query/report/material/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportMaterialDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @ApiOperation("查询配怀区详情报表")
    @RequestMapping(value = "/query/report/mating/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportMatingDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @ApiOperation("查询保育区详情报表")
    @RequestMapping(value = "/query/report/nursery/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportNurseryDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

    @ApiOperation("查询后备区详情报表")
    @RequestMapping(value = "/query/report/reserve/detail", method = RequestMethod.GET)
    public List<DoctorReportSow> queryReportReserveDetail(@ModelAttribute DoctorDimensionCriteria dimensionCriteria) {
        return null;
    }

}
