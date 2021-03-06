package io.terminus.doctor.web.front.report;

import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.BaseUser;
import io.terminus.common.model.Response;
import io.terminus.doctor.basic.dto.DoctorReportFieldTypeDto;
import io.terminus.doctor.common.exception.InvalidException;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.dto.report.daily.DoctorFarmLiveStockDto;
import io.terminus.doctor.event.enums.DateDimension;
import io.terminus.doctor.event.enums.OrzDimension;
import io.terminus.doctor.event.service.DoctorDailyReportV2Service;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2018/1/3.
 */
@Api("猪场看板报表")
@Slf4j
@RestController
@RequestMapping("/api/doctor/report/board/{farmId}/")
public class ReportBoardController {

    @RpcConsumer
    private DoctorDailyReportV2Service doctorDailyReportV2Service;
    @RpcConsumer
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    @Autowired
    private ReportBoardHelper helper;

    @ApiOperation("看板日报表")
    @RequestMapping(method = RequestMethod.GET, value = "daily")
    public Response<List<DoctorReportFieldTypeDto>> dailyBoard(@ApiParam("猪场名称")
                                                     @PathVariable Long farmId,
                                                                 @ApiParam("查询日期")
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                                 Integer type) {
        Long farmIds = farmId;
        if(type == 1){
            BaseUser baseUser = UserUtil.getCurrentUser();
            if (baseUser == null) {
                throw new JsonResponseException("user.not.login");
            }
            Response<DoctorUserDataPermission> dataPermissionResponse = doctorUserDataPermissionReadService.findDataPermissionByUserId(baseUser.getId());
            List<Long> groupIdsList = dataPermissionResponse.getResult().getGroupIdsList();
            if(groupIdsList == null || groupIdsList.size() == 0 || (groupIdsList.contains(0L) && groupIdsList.size()==1)){
                throw new JsonResponseException("你没有可查看集团的权限");
            }
            if(groupIdsList.contains(0L)){
                groupIdsList.remove(0L);
            }
            log.error("groupIdsList.size()="+groupIdsList.size());
            if(groupIdsList.size()==0){
                throw new JsonResponseException("你没有可查看集团的权限");
            }else {
                if (!groupIdsList.contains(farmIds)) {
                    farmIds = groupIdsList.get(0);
                }
            }
        }
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmIds, type,
                date, DateDimension.DAY.getValue());
        return Response.ok(helper.fieldWithHidden(dimensionCriteria,type));
    }


    @ApiOperation("看板周报表")
    @RequestMapping(method = RequestMethod.GET, value = "weekly")
    public List<DoctorReportFieldTypeDto> weeklyBoard(@ApiParam("猪场名称")
                                                      @PathVariable Long farmId,
                                                      @ApiParam("查询日期， 本周第一天")
                                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                      Integer type) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, type,
                date, DateDimension.WEEK.getValue());
        return helper.fieldWithHidden(dimensionCriteria,type);
    }

    @ApiOperation("看板月报表")
    @RequestMapping(method = RequestMethod.GET, value = "monthly")
    public List<DoctorReportFieldTypeDto> monthlyBoard(@ApiParam("猪场名称")
                                                       @PathVariable Long farmId,
                                                       @ApiParam("查询日期， 本月第一天")
                                                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                       Integer type) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, type,
                date, DateDimension.MONTH.getValue());
        return helper.fieldWithHidden(dimensionCriteria,type);
    }

    @ApiOperation("看板季报表")
    @RequestMapping(method = RequestMethod.GET, value = "quarterly")
    public List<DoctorReportFieldTypeDto> quarterlyBoard(@ApiParam("猪场名称")
                                                         @PathVariable Long farmId,
                                                         @ApiParam("查询日期， 本季第一天")
                                                         @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                         Integer type) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, type,
                date, DateDimension.QUARTER.getValue());
        return helper.fieldWithHidden(dimensionCriteria,type);
    }

    @ApiOperation("看板年报表")
    @RequestMapping(method = RequestMethod.GET, value = "yearly")
    public List<DoctorReportFieldTypeDto> yearlyBoard(@ApiParam("猪场名称")
                                                      @PathVariable Long farmId,
                                                      @ApiParam("查询日期， 本年第一天")
                                                      @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date,
                                                      Integer type) {
        DoctorDimensionCriteria dimensionCriteria = new DoctorDimensionCriteria(farmId, type,
                date, DateDimension.YEAR.getValue());
        return helper.fieldWithHidden(dimensionCriteria,type);
    }

    @RequestMapping(value = "/live/stock")
    public DoctorFarmLiveStockDto realTimeLiveStock(@PathVariable Long farmId,Integer type){
        Long farmIds = farmId;
        if(type == 1){
            BaseUser baseUser = UserUtil.getCurrentUser();
            if (baseUser == null) {
                throw new JsonResponseException("user.not.login");
            }
            Response<DoctorUserDataPermission> dataPermissionResponse = doctorUserDataPermissionReadService.findDataPermissionByUserId(baseUser.getId());
            List<Long> groupIdsList = dataPermissionResponse.getResult().getGroupIdsList();
            if(groupIdsList.contains(0L)){
                groupIdsList.remove(0L);
            }
            if(groupIdsList.size()==0){
                throw new InvalidException("你没有可查看集团的权限");
            }else {
                if (!groupIdsList.contains(farmIds)) {
                    farmIds = groupIdsList.get(0);
                }
            }
        }
        return RespHelper.or500(doctorDailyReportV2Service.findFarmsLiveStock1(Lists.newArrayList(farmIds),type)).get(0);
    }
}
