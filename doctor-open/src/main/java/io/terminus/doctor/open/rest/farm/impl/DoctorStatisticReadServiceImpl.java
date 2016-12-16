package io.terminus.doctor.open.rest.farm.impl;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticReadService;
import io.terminus.doctor.open.dto.DoctorBasicDto;
import io.terminus.doctor.open.dto.DoctorFarmBasicDto;
import io.terminus.doctor.open.dto.DoctorStatisticDto;
import io.terminus.doctor.open.rest.farm.service.DoctorStatisticReadService;
import io.terminus.doctor.open.util.OPRespHelper;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import io.terminus.pampas.openplatform.exceptions.OPClientException;
import io.terminus.pampas.openplatform.exceptions.OPServerException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static lombok.patcher.Symbols.isEmpty;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/3
 */
@Slf4j
@Service
public class DoctorStatisticReadServiceImpl implements DoctorStatisticReadService {

    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorOrgReadService doctorOrgReadService;
    private final DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService;
    private final DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;

    @Autowired
    private DoctorStatisticReadServiceImpl(DoctorFarmReadService doctorFarmReadService,
                                           DoctorOrgReadService doctorOrgReadService,
                                           DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService, DoctorUserDataPermissionReadService doctorUserDataPermissionReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorOrgReadService = doctorOrgReadService;
        this.doctorPigTypeStatisticReadService = doctorPigTypeStatisticReadService;
        this.doctorUserDataPermissionReadService = doctorUserDataPermissionReadService;
    }

    @Override
    public Response<DoctorFarmBasicDto> getFarmStatistic(Long farmId) {
        try {
            DoctorFarm farm = OPRespHelper.orOPEx(doctorFarmReadService.findFarmById(farmId));

            //查询猪只统计, 按照类型拼下list
            DoctorPigTypeStatistic stat = OPRespHelper.orOPEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(farmId));
            return Response.ok(new DoctorFarmBasicDto(farm, getStatistics(Lists.newArrayList(MoreObjects.firstNonNull(stat, new DoctorPigTypeStatistic())))));
        } catch (OPServerException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get farm statistic failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.farm.statistic.fail");
        }
    }

    @Override
    public Response<DoctorBasicDto> getOrgStatistic(Long userId) {
        try {
            //查询有权限的公司与猪场
            DoctorOrg org = OPRespHelper.orOPEx(doctorOrgReadService.findOrgByUserId(userId));
            List<DoctorFarm> farms = OPRespHelper.orOPEx(doctorFarmReadService.findFarmsByUserId(userId));

            //查询公司统计
            List<DoctorPigTypeStatistic> stats = OPRespHelper.orOPEx(doctorPigTypeStatisticReadService.findPigTypeStatisticsByOrgId(org.getId()));

            //获取猪场统计
            List<DoctorFarmBasicDto> farmBasicDtos = farms.stream()
                    .map(farm -> {
                        DoctorPigTypeStatistic stat = OPRespHelper.orOPEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(farm.getId()));
                        return new DoctorFarmBasicDto(farm, getStatistics(Lists.newArrayList(MoreObjects.firstNonNull(stat, new DoctorPigTypeStatistic()))));
                    })
                    .collect(Collectors.toList());

            return Response.ok(new DoctorBasicDto(org, getStatistics(stats), farmBasicDtos));
        } catch (OPServerException e) {
                return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get org statistic failed, userId:{}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.org.statistic.fail");
        }
    }

    @Override
    public Response<DoctorBasicDto> getOrgStatisticByOrg(Long userId,Long orgId) {
        try {
            //校验orgId
            Response<DoctorUserDataPermission> dataPermissionResponse=doctorUserDataPermissionReadService.findDataPermissionByUserId(userId);
            DoctorUserDataPermission doctorUserDataPermission=OPRespHelper.orOPEx(dataPermissionResponse);
            if (!doctorUserDataPermission.getOrgIdsList().contains(orgId)){
                return Response.fail("user.not.permission.org");
            }
            List<Long> farmList=doctorUserDataPermission.getFarmIdsList();
            //查询有权限的公司与猪场
            DoctorOrg org = OPRespHelper.orOPEx(doctorOrgReadService.findOrgById(orgId));
            List<DoctorFarm> farms=OPRespHelper.orOPEx(doctorFarmReadService.findFarmsByOrgId(org.getId()));
            if (farms!=null){
                farms.stream().filter(t-> farmList.contains(t.getId()));
            }
//            List<DoctorFarm> farms = OPRespHelper.orOPEx(doctorFarmReadService.findFarmsByUserId(userId));

            //查询公司统计
            List<DoctorPigTypeStatistic> stats = OPRespHelper.orOPEx(doctorPigTypeStatisticReadService.findPigTypeStatisticsByOrgId(org.getId()));

            //获取猪场统计
            List<DoctorFarmBasicDto> farmBasicDtos = farms.stream()
                    .map(farm -> {
                        DoctorPigTypeStatistic stat = OPRespHelper.orOPEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(farm.getId()));
                        return new DoctorFarmBasicDto(farm, getStatistics(Lists.newArrayList(MoreObjects.firstNonNull(stat, new DoctorPigTypeStatistic()))));
                    })
                    .collect(Collectors.toList());

            return Response.ok(new DoctorBasicDto(org, getStatistics(stats), farmBasicDtos));
        } catch (OPServerException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("get org statistic failed, userId:{}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.org.statistic.fail");
        }
    }

    //通过猪类统计表计算出统计结果
    private List<DoctorStatisticDto> getStatistics(List<DoctorPigTypeStatistic> stats) {
        return Lists.newArrayList(
                new DoctorStatisticDto(DoctorStatisticDto.PigType.SOW.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getSow(), 0))),          //母猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.BOAR.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getBoar(), 0))),          //公猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FARROW_PIGLET.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getFarrow(), 0))),       //产房仔猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.NURSERY_PIGLET.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getNursery(), 0))),      //保育猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FATTEN_PIG.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getFatten(), 0))),       //育肥猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.HOUBEI.getCutDesc(),
                        (int) CountUtil.sumInt(stats, stat -> MoreObjects.firstNonNull(stat.getHoubei(), 0)))        //后备猪
        );
    }
}
