package io.terminus.doctor.event.service;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dto.DoctorBarnCountForPigTypeDto;
import io.terminus.doctor.event.dto.DoctorBarnDto;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    private final DoctorGroupDao doctorGroupDao;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorPigReadService doctorPigReadService;

    @Autowired
    public DoctorBarnReadServiceImpl(DoctorBarnDao doctorBarnDao,
                                     DoctorGroupDao doctorGroupDao,
                                     DoctorGroupReadService doctorGroupReadService,
                                     DoctorPigReadService doctorPigReadService) {
        this.doctorBarnDao = doctorBarnDao;
        this.doctorGroupDao = doctorGroupDao;
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
    public Response<List<DoctorBarn>> findBarnsByFarmIds(List<Long> farmIds) {
        try {
            return Response.ok(doctorBarnDao.findByFarmIds(farmIds));
        } catch (Exception e) {
            log.error("find barn by farm id fail, farmIds:{}, cause:{}", farmIds, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<List<DoctorBarn>> findBarnsByEnums(Long farmId, List<Integer> pigTypes, Integer canOpenGroup, Integer status, List<Long> barnIds) {
        try {
            return Response.ok(doctorBarnDao.findByEnums(farmId, pigTypes, canOpenGroup, status, barnIds));
        } catch (Exception e) {
            log.error("find barn by enums fail, farmId:{}, pigTypes:{}, canOpenGroup:{}, status:{}, barnIds:{}, cause:{}",
                    farmId, pigTypes, canOpenGroup, status, barnIds, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }

    @Override
    public Response<Integer> countPigByBarnId(Long barnId) {
        try {
            DoctorBarn barn = doctorBarnDao.findById(barnId);

            //先统计猪群数量
            DoctorGroupSearchDto searchDto = new DoctorGroupSearchDto();
            searchDto.setFarmId(barn.getFarmId());
            searchDto.setCurrentBarnId(barnId);
            searchDto.setStatus(DoctorGroup.Status.CREATED.getValue());
            List<DoctorGroupDetail> groupDetails = RespHelper.orServEx(doctorGroupReadService.findGroupDetail(searchDto));
            Integer groupCount = groupDetails.stream().mapToInt(g -> g.getGroupTrack().getQuantity()).sum();

            //过滤已离场的猪
            List<DoctorPigTrack> pigTracks = RespHelper.orServEx(doctorPigReadService.findActivePigTrackByCurrentBarnId(barnId));
            return Response.ok(groupCount + pigTracks.size());
        } catch (Exception e) {
            log.error("count pig by barn id failed, barnId:{}, cause:{}", barnId, Throwables.getStackTraceAsString(e));
            return Response.fail("count.pig.fail");
        }
    }

    @Override
    public Response<List<DoctorBarn>> findAvailableBarns(Long farmId, Long groupId) {
        try {
            DoctorGroup existed = doctorGroupDao.findById(groupId);
            if (existed == null) {
                return Response.fail("doctor.group.not.exist");
            }
            //获取当前猪舍id
            Long currentBarnId = existed.getCurrentBarnId();
            /**
             * 当前所属猪舍
             */
            Integer barnType = doctorBarnDao.findById(currentBarnId).getPigType();
            /**
             * 要转入猪场的猪舍
             */
            List<DoctorBarn> doctorBarns = doctorBarnDao.findByFarmId(farmId);
            List<DoctorBarn> availableBarns = Lists.newArrayList();

            for (DoctorBarn doctorBarn : doctorBarns) {
                if (Objects.equal(doctorBarn.getPigType(), PigType.DELIVER_SOW.getValue()) &&
                        !(Objects.equal(barnType, PigType.NURSERY_PIGLET.getValue()) || Objects.equal(barnType, PigType.DELIVER_SOW.getValue()))) {
                    continue;
                }

                if (Objects.equal(doctorBarn.getPigType(), PigType.NURSERY_PIGLET.getValue()) &&
                        !(Objects.equal(barnType, PigType.FATTEN_PIG.getValue()) || Objects.equal(barnType, PigType.NURSERY_PIGLET.getValue()))) {
                    continue;
                }
                availableBarns.add(doctorBarn);
            }

            return Response.ok(availableBarns);
        } catch (Exception e) {
            log.error("fail to find available barns,current group id:{},farm id:{},cause:{}",
                    groupId, farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.available.barns.failed");
        }
    }

    @Override
    public Response<DoctorBarnCountForPigTypeDto> countForTypes(Map<String, Object> criteria) {
        try {
            DoctorBarnCountForPigTypeDto dto = doctorBarnDao.countForTypes(criteria);
            Long allCount = dto.getReserveCount() + dto.getBoarCount() + dto.getPregSowCount()
                    + dto.getDeliverSowCount() + dto.getMateSowCount() + dto.getFattenPigCount() + dto.getNurseryPigletCount();
            dto.setAllCount(allCount);
            return Response.ok(dto);
        } catch (Exception e) {
            log.error("count.for.types.failed, cause by :{}", Throwables.getStackTraceAsString(e));
            return Response.fail("count for types failed");
        }
    }
    @Override
    public Response<Paging<DoctorBarn>> pagingBarn(DoctorBarnDto barnDto, Integer pageNo, Integer size) {
        try {
            PageInfo page = PageInfo.of(pageNo, size);
            return Response.ok(doctorBarnDao.paging(page.getOffset(), page.getLimit(), barnDto));
        } catch (Exception e) {
            log.error("paging barn failed, barnDto:{}, pageNo:{}, size:{}, cause:{}", barnDto, pageNo, size, Throwables.getStackTraceAsString(e));
            return Response.fail("barn.find.fail");
        }
    }
}
