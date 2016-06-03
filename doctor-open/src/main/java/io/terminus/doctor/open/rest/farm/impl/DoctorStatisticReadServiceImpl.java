package io.terminus.doctor.open.rest.farm.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.model.DoctorPigTypeStatistic;
import io.terminus.doctor.event.service.DoctorPigTypeStatisticReadService;
import io.terminus.doctor.open.dto.DoctorBasicDto;
import io.terminus.doctor.open.dto.DoctorFarmBasicDto;
import io.terminus.doctor.open.dto.DoctorStatisticDto;
import io.terminus.doctor.open.rest.farm.service.DoctorStatisticReadService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    private final DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService;

    @Autowired
    private DoctorStatisticReadServiceImpl(DoctorFarmReadService doctorFarmReadService,
                                           DoctorPigTypeStatisticReadService doctorPigTypeStatisticReadService) {
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorPigTypeStatisticReadService = doctorPigTypeStatisticReadService;
    }

    @Override
    public Response<DoctorFarmBasicDto> getFarmStatistic(Long farmId) {
        try {
            DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(farmId));

            //查询猪只统计, 按照类型拼下list
            DoctorPigTypeStatistic stat = RespHelper.orServEx(doctorPigTypeStatisticReadService.findPigTypeStatisticByFarmId(farmId));
            return Response.ok(new DoctorFarmBasicDto(farm, getStatistics(Lists.newArrayList(stat))));
        } catch (Exception e) {
            log.error("get farm statistic failed, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.farm.statistic.fail");
        }
    }

    @Override
    public Response<DoctorBasicDto> getOrgStatistic(Long userId) {
        try {
            // TODO: 16/6/3 查询此用户的 公司权限 和 猪场权限
            Long orgId = 1L;
            List<Long> farmIds = Lists.newArrayList();

            //获取公司信息
            DoctorOrg org = RespHelper.orServEx(doctorFarmReadService.findOrgById(orgId));

            //获取猪场统计
            List<DoctorFarmBasicDto> farmBasicDtos = farmIds.stream()
                    .map(farmId -> RespHelper.orServEx(getFarmStatistic(farmId)))
                    .collect(Collectors.toList());

            //查询公司统计
            List<DoctorPigTypeStatistic> stats = RespHelper.orServEx(doctorPigTypeStatisticReadService.findPigTypeStatisticsByOrgId(orgId));

            return Response.ok(new DoctorBasicDto(org, getStatistics(stats), farmBasicDtos));
        } catch (Exception e) {
            log.error("get org statistic failed, userId:{}, cause:{}", userId, Throwables.getStackTraceAsString(e));
            return Response.fail("get.org.statistic.fail");
        }
    }

    //通过猪类统计表计算出统计结果
    private List<DoctorStatisticDto> getStatistics(List<DoctorPigTypeStatistic> stats) {
        return Lists.newArrayList(
                new DoctorStatisticDto(DoctorStatisticDto.PigType.SOW.getCutDesc(),
                        (int) CountUtil.sumInt(stats, DoctorPigTypeStatistic::getSow)),          //母猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FARROW_PIGLET.getCutDesc(),
                        (int) CountUtil.sumInt(stats, DoctorPigTypeStatistic::getFarrow)),       //产房仔猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.NURSERY_PIGLET.getCutDesc(),
                        (int) CountUtil.sumInt(stats, DoctorPigTypeStatistic::getNursery)),      //保育猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.FATTEN_PIG.getCutDesc(),
                        (int) CountUtil.sumInt(stats, DoctorPigTypeStatistic::getFatten)),       //育肥猪
                new DoctorStatisticDto(DoctorStatisticDto.PigType.BREEDING_PIG.getCutDesc(),
                        (int) CountUtil.sumInt(stats, DoctorPigTypeStatistic::getBoar) +
                                (int) CountUtil.sumInt(stats, DoctorPigTypeStatistic::getSow) )  //育种猪 = 公 + 母
        );
    }
}
