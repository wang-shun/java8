package io.terminus.doctor.web.core.service.impl;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.Arguments;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.report.daily.DoctorFarmLiveStockDto;
import io.terminus.doctor.event.service.DoctorCommonReportReadService;
import io.terminus.doctor.event.service.DoctorDailyReportV2Service;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.doctor.web.core.dto.DoctorBasicDto;
import io.terminus.doctor.web.core.dto.DoctorFarmBasicDto;
import io.terminus.doctor.web.core.dto.DoctorStatisticDto;
import io.terminus.doctor.web.core.service.DoctorStatisticReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/3
 */
@Slf4j
@Service
public class DoctorStatisticReadServiceImpl implements DoctorStatisticReadService {

    @RpcConsumer
    private DoctorFarmReadService doctorFarmReadService;

    @RpcConsumer
    private DoctorOrgReadService doctorOrgReadService;

    @RpcConsumer
    private DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService;
    @RpcConsumer
    private DoctorCommonReportReadService doctorCommonReportReadService;

    @RpcConsumer
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    @RpcConsumer
    private DoctorDailyReportV2Service doctorDailyReportV2Service;


    @Override
    public Response<DoctorFarmBasicDto> getFarmStatistic(Long farmId) {
        try {
            return Response.ok(buildFarmBasic(farmId));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get farm statistic failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.farm.statistic.fail");
        }
    }

    @Override
    public Response<DoctorBasicDto> getOrgStatistic(Long userId) {
        try {
            //查询有权限的公司与猪场 // TODO: 2017/2/16 多公司 暂时先返回第一个
            List<DoctorOrg> orgs = RespHelper.orServEx(doctorOrgReadService.findOrgsByUserId(userId));
            if (!notEmpty(orgs)) {
                log.info("no orgs find ,userId = {}", userId);
                return Response.ok(new DoctorBasicDto(new DoctorOrg(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            }
            DoctorOrg org = orgs.get(0);

            DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
            List<Long> permissionFarmIdsList = permission.getFarmIdsList();

            List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(org.getId()));
            List<Long> farmIdList = farms.stream().filter(doctorFarm -> permissionFarmIdsList.contains(doctorFarm.getId()))
                    .map(DoctorFarm::getId).collect(Collectors.toList());

            if (Arguments.isNullOrEmpty(farmIdList)) {
                return Response.ok(new DoctorBasicDto(org, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            }

            List<DoctorFarmLiveStockDto> stats = RespHelper.orServEx(doctorDailyReportV2Service.findFarmsLiveStock(farmIdList));

            List<DoctorFarmBasicDto> farmBasicDtos = stats.stream().map(this::buildFarmBasic).collect(Collectors.toList());

            return Response.ok(new DoctorBasicDto(org, getStatistics(stats), farmBasicDtos, orgs));
        } catch (ServiceException e) {
                return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get org statistic failed, userId:{}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.org.statistic.fail");
        }
    }

    @Override
    public Response<DoctorBasicDto> getOrgStatisticByOrg(Long userId, Long orgId) {
        try {
            log.error("orgId============="+orgId);
            //如果该账号为集团账号，则返回为空（孔景军）
            Integer userType = doctorOrgReadService.getUserType(userId);
            /*if(userType !=null && userType == 1){
                return null;
            }*/
            //查询有权限的公司
            List<DoctorOrg> orgs = RespHelper.orServEx(doctorOrgReadService.findOrgsByUserId(userId));
            if (!notEmpty(orgs)) {
                log.info("no orgs find ,userId = {}", userId);
                return Response.ok(new DoctorBasicDto(new DoctorOrg(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            }
            if(Arguments.isNull(orgId)){
                orgId = orgs.get(0).getId();
            }
            //校验orgId
            DoctorUserDataPermission permission = RespHelper.orServEx(doctorUserDataPermissionReadService.findDataPermissionByUserId(userId));
            if (permission == null || !permission.getOrgIdsList().contains(orgId)) {
                return Response.fail("user.not.permission.org");
            }
            DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(orgId));

            List<Long> permissionFarmIdsList = permission.getFarmIdsList();

            List<DoctorFarm> farms = RespHelper.orServEx(doctorFarmReadService.findFarmsByOrgId(org.getId()));
            List<Long> farmIdList = farms.stream().filter(doctorFarm -> permissionFarmIdsList.contains(doctorFarm.getId()))
                    .map(DoctorFarm::getId).collect(Collectors.toList());

            if (Arguments.isNullOrEmpty(farmIdList)) {
                return Response.ok(new DoctorBasicDto(org, Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));
            }

            List<DoctorFarmLiveStockDto> stats = RespHelper.orServEx(doctorDailyReportV2Service.findFarmsLiveStock(farmIdList));

            List<DoctorFarmBasicDto> farmBasicDtos = stats.stream().map(this::buildFarmBasic).collect(Collectors.toList());

            return Response.ok(new DoctorBasicDto(org, getStatistics(stats), farmBasicDtos, orgs));
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get org statistic failed, userId:{}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.org.statistic.fail");
        }
    }

    private DoctorFarmBasicDto buildFarmBasic(DoctorFarmLiveStockDto stockDto) {
        DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(stockDto.getFarmId()));
        //查询猪只统计, 按照类型拼下list
        return new DoctorFarmBasicDto(farm, getStatistics(Lists.newArrayList(MoreObjects.firstNonNull(stockDto, new DoctorFarmLiveStockDto()))));
    }

    private DoctorFarmBasicDto buildFarmBasic(Long farmId) {
        DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(farmId));
        //查询猪只统计, 按照类型拼下list
        DoctorFarmLiveStockDto stat = RespHelper.orServEx(doctorDailyReportV2Service.findFarmsLiveStock(Lists.newArrayList(farmId))).get(0);
        return new DoctorFarmBasicDto(farm, getStatistics(Lists.newArrayList(MoreObjects.firstNonNull(stat, new DoctorFarmLiveStockDto()))));
    }

    //通过猪类统计表计算出统计结果
    private List<DoctorStatisticDto> getStatistics(List<DoctorFarmLiveStockDto> stats) {
        return Lists.newArrayList(
                new DoctorStatisticDto(DoctorStatisticDto.PigType.HOUBEI.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getHoubei(), 0))),        //后备猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.BOAR.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getBoar(), 0))),          //公猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.SOW.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getSow(), 0))),          //母猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.PEIHUAI.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getPeihuai(), 0))),        //配怀猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.DELIVER_SOW.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getDeliverSow(), 0))),        //产房母猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FARROW_PIGLET.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getFarrow(), 0))),       //产房仔猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.NURSERY_PIGLET.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getNursery(), 0))),      //保育猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FATTEN_PIG.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getFatten(), 0)))       //育肥猪

        );
    }
}
