package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Desc: 猪舍表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-20
 */
@Slf4j
@Service
@RpcProvider
public class DoctorBarnReadServiceImpl implements DoctorBarnReadService {

    private final DoctorBarnDao doctorBarnDao;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorPigReadService doctorPigReadService;

    @Autowired
    public DoctorBarnReadServiceImpl(DoctorBarnDao doctorBarnDao,
                                     DoctorGroupReadService doctorGroupReadService,
                                     DoctorPigReadService doctorPigReadService) {
        this.doctorBarnDao = doctorBarnDao;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorPigReadService = doctorPigReadService;
    }

    @Override
    public Response<DoctorBarn> findBarnById(Long barnId) {
        try {
            return Response.ok(doctorBarnDao.findById(barnId));
        } catch (Exception e) {
            log.error("find barn by id failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBarn>> findBarnsByFarmId(Long farmId) {
        try {
            return Response.ok(doctorBarnDao.findByFarmId(farmId));
        } catch (Exception e) {
            log.error("find barn by farm id fail, farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBarn>> findBarnsByEnums(Long farmId, Integer pigType, Integer canOpenGroup, Integer status) {
        try {
            return Response.ok(doctorBarnDao.findByEnums(farmId, pigType, canOpenGroup, status));
        } catch (Exception e) {
            log.error("find barn by enums fail, farmId:{}, pigType:{}, canOpenGroup:{}, status:{}, cause:{}",
                    farmId, pigType, canOpenGroup, status, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<Integer> countPigByBarnId(Long barnId) {
        try {
            DoctorBarn barn = doctorBarnDao.findById(barnId);
            if (PigType.isGroup(barn.getPigType())) {
                DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
                searchDto.setFarmId(barn.getFarmId());
                searchDto.setCurrentBarnId(barnId);
                searchDto.setStatus(DoctorGroup.Status.CREATED.getValue());
                List<DoctorGroupDetail> groupDetails = RespHelper.orServEx(doctorGroupReadService.findGroupDetail(searchDto));
                return Response.ok(groupDetails.stream().mapToInt(g -> g.getGroupTrack().getQuantity()).sum());
            }

            //过滤已离场的猪
            List<DoctorPigInfoDto> pigInfoDtos = RespHelper.orServEx(doctorPigReadService.queryDoctorPigInfoByBarnId(barnId)).stream()
                    .filter(pig -> !pig.getStatus().equals(PigStatus.BOAR_LEAVE.getKey()))
                    .collect(Collectors.toList());
            return Response.ok(pigInfoDtos.size());
        } catch (Exception e) {
            log.error("coutn pigt by barn id failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("count.pig.fail");
        }
    }
}
