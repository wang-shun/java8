package io.terminus.doctor.event.service;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.enums.IsOrNot;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.*;
import io.terminus.doctor.event.enums.KongHuaiPregCheckResult;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.terminus.doctor.common.enums.PigType.*;

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
    private final DoctorGroupTrackDao doctorGroupTrackDao;
    private final DoctorPigTrackDao doctorPigTrackDao;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorPigReadService doctorPigReadService;

    // 猪舍可转入的性别初始化 map
    private static final Map<String, List<String>> doctorBarnAvailableSexMap = new HashMap<String, List<String>>();
    // 母猪舍可转入的性别的集合
    private static final List<String> sowAvailableSex = new ArrayList<String>();
    // 公猪舍可转入的性别的集合
    private static final List<String> boarAvailableSex = new ArrayList<String>();
    // 混合舍可转入的性别的集合
    private static final List<String> blendAvailableSex = new ArrayList<String>();

    static {
        sowAvailableSex.add("0");
        sowAvailableSex.add("2");

        boarAvailableSex.add("1");
        boarAvailableSex.add("2");

        blendAvailableSex.add("2");

        doctorBarnAvailableSexMap.put("0", sowAvailableSex);
        doctorBarnAvailableSexMap.put("1", boarAvailableSex);
        doctorBarnAvailableSexMap.put("2", blendAvailableSex);
    }

    @Autowired
    public DoctorBarnReadServiceImpl(DoctorBarnDao doctorBarnDao,
                                     DoctorGroupDao doctorGroupDao,
                                     DoctorGroupTrackDao doctorGroupTrackDao, DoctorPigTrackDao doctorPigTrackDao, DoctorGroupReadService doctorGroupReadService,
                                     DoctorPigReadService doctorPigReadService) {
        this.doctorBarnDao = doctorBarnDao;
        this.doctorGroupDao = doctorGroupDao;
        this.doctorGroupTrackDao = doctorGroupTrackDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
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
    public Response<List<IotBarnWithStorage>> findIotBarnWithStorage(Long farmId) {
        try {
            List<DoctorBarn> doctorBarns = doctorBarnDao.findByFarmId(farmId);
            DoctorGroupSearchDto doctorGroupSearchDto = new DoctorGroupSearchDto();
            doctorGroupSearchDto.setFarmId(farmId);
            return Response.ok(doctorBarns.stream().map(doctorBarn -> {
                IotBarnWithStorage iotBarnWithStorage = new IotBarnWithStorage();
                BeanMapper.copy(doctorBarn, iotBarnWithStorage);
                iotBarnWithStorage.setPigCount(0);
                iotBarnWithStorage.setPigGroupCount(0L);
                if (Objects.equal(doctorBarn.getStatus(), DoctorBarn.Status.USING.getValue())) {
                    if (PigType.PIG_TYPES.contains(doctorBarn.getPigType())) {
                        List<DoctorPigTrack> pigTracks = RespHelper.orServEx(doctorPigReadService.findActivePigTrackByCurrentBarnId(doctorBarn.getId()));
                        iotBarnWithStorage.setPigCount(pigTracks.size());
                    }
                    if (PigType.GROUP_TYPES.contains(doctorBarn.getPigType())) {
                        doctorGroupSearchDto.setCurrentBarnId(doctorBarn.getId());
                        iotBarnWithStorage.setPigGroupCount(RespHelper.orServEx(doctorGroupReadService.getGroupCount(doctorGroupSearchDto)));
                    }

                }
                return iotBarnWithStorage;
            }).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("find iot barn with storage failed,farmId:{}, cause:{}", farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.iot.barn.with.storage.failed");
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
    /*
        * 根据farmId和当前用户查猪舍
        * 冯雨晴 2019.9.18
        *
        * */
    @Override
    public Response<List<Map>> findBarnsByEnumss(Long farmId, List<Integer> pigTypes,Integer status, List<Long> barnIds) {

        List<Map> maps = doctorBarnDao.findByEnumss(farmId,pigTypes,status,barnIds);

        return Response.ok(maps);
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
            return Response.ok(doctorBarns.stream().filter(doctorBarn -> doctorBarn != null
                    && checkCanTransBarn(barnType, doctorBarn.getPigType())
                    && Objects.equal(doctorBarn.getStatus(), DoctorBarn.Status.USING.getValue())).collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("fail to find available barns,current group id:{},farm id:{},cause:{}",
                    groupId, farmId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.available.barns.failed");
        }
    }



    @Override
    public Response<List<DoctorBarn>> findAvailablePigBarns(Long farmId, Long groupId) {

        try {
            DoctorGroup existed = doctorGroupDao.findById(groupId);
            if (existed == null) {
                return Response.fail("doctor.group.not.exist");
            }

            //获取当前猪舍id
            Long currentBarnId = existed.getCurrentBarnId();

            /**
             * 当前所属猪舍的猪的类型
             */
            Integer barnType = doctorBarnDao.findById(currentBarnId).getPigType();

            /**
             * 要转入猪场的猪舍
             */
            Integer sex = doctorGroupTrackDao.findSex(groupId);
            List<DoctorBarn> doctorBarns = null;
            if(sex == 0){
                doctorBarns = doctorBarnDao.findByFarmId1(farmId);
            } else if(sex == 1){
                doctorBarns = doctorBarnDao.findByFarmId2(farmId);
            } else {
                doctorBarns = doctorBarnDao.findByFarmId3(farmId);
            }
//            doctorBarns = doctorBarnDao.findByFarmId(farmId);
            // 根据某些条件过滤之后的猪舍
            return Response.ok(doctorBarns.stream().filter(doctorBarn -> doctorBarn != null
                    && checkCanTransBarn(barnType, doctorBarn.getPigType())
                    && Objects.equal(doctorBarn.getStatus(), DoctorBarn.Status.USING.getValue())).collect(Collectors.toList()));

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

    @Override
    public Response<DoctorBarn> findBarnByFarmAndBarnName(@NotNull(message = "farmId.can.not.be.null") Long farmId, @NotNull(message = "barn.name.not.empty") String barnName) {
        try {
            return Response.ok(doctorBarnDao.findBarnByFarmAndBarnName(ImmutableMap.of("farmId", farmId, "name", barnName)));
        } catch (Exception e) {
            log.error("find barn by farm and barn name failed, farmId:{}, barnName:{}, cause:{}", farmId, barnName, Throwables.getStackTraceAsString(e));
            return Response.fail("find.barn.by.farm.and.barn.name.failed");
        }
    }

    @Override
    public Response<List<DoctorBarn>> selectBarns(Long orgId, Long farmId, String name, Integer count) {
        try {
            return Response.ok(doctorBarnDao.selectBarns(orgId, farmId, name, count));
        } catch (Exception e) {
            log.error("select barns failed, name, count,cause:{}",
                    name, count, Throwables.getStackTraceAsString(e));
            return Response.fail("select.barns.failed");
        }
    }

    @Override
    public Response<IotBarnInfo> findIotBarnInfo(Long barnId) {
        try {
            IotBarnInfo iotBarnInfo = new IotBarnInfo();
            DoctorBarn doctorBarn = doctorBarnDao.findById(barnId);
            iotBarnInfo.setCapacity(doctorBarn.getCapacity());
            iotBarnInfo.setBarnId(doctorBarn.getId());
            iotBarnInfo.setBarnName(doctorBarn.getName());
            iotBarnInfo.setStaffId(doctorBarn.getStaffId());
            iotBarnInfo.setStaffName(doctorBarn.getStaffName());
            Integer currentCount = 0;
            Map<String, Integer> map = Maps.newHashMap();
            iotBarnInfo.setCurrentPigs(currentCount);
            iotBarnInfo.setStatusPigs(map);

            //育肥。保育。后备
            if (doctorBarn.getPigType() == PigType.NURSERY_PIGLET.getValue()
                    || doctorBarn.getPigType() == PigType.RESERVE.getValue()
                    || doctorBarn.getPigType() == PigType.FATTEN_PIG.getValue()) {
                List<DoctorGroup> groups = doctorGroupDao.findByCurrentBarnId(barnId);

                if (groups.isEmpty()) {
                    return Response.ok(iotBarnInfo);
                }
                List<DoctorGroupTrack> tracks = doctorGroupTrackDao.findsByGroups(groups.stream().map(DoctorGroup::getId).collect(Collectors.toList()));
                currentCount = tracks.stream().mapToInt(DoctorGroupTrack::getQuantity).sum();
                iotBarnInfo.setCurrentPigs(currentCount);
                int totalAge = 0;
                for (DoctorGroupTrack groupTrack: tracks) {
                    totalAge += groupTrack.getQuantity() * groupTrack.getAvgDayAge();
                }
                int avgDayAge = 0;
                if (currentCount != 0) {
                    avgDayAge =new BigDecimal(totalAge).
                            divide(new BigDecimal(currentCount), BigDecimal.ROUND_HALF_UP).intValue();
                }
                map.put("平均日龄", avgDayAge);
                return Response.ok(iotBarnInfo);
            }

            //种猪
            List<DoctorPigTrack> pigTracks = doctorPigTrackDao.findByBarnId(doctorBarn.getId());
            if (!pigTracks.isEmpty()) {
                pigTracks = pigTracks.stream().filter(doctorPigTrack -> Objects.equal(doctorPigTrack.getIsRemoval(),
                        IsOrNot.NO.getKey())).collect(Collectors.toList());
                currentCount = pigTracks.size();
                List<DoctorPigStatusCount> pigStatusCountList = doctorPigTrackDao.getStatusPigForBarn(doctorBarn.getId());
                if (!pigStatusCountList.isEmpty()) {
                    int yang = 0;
                    for (DoctorPigStatusCount doctorPigStatusCount : pigStatusCountList) {
                        if (Objects.equal(doctorPigStatusCount.getStatus(), PigStatus.Entry.getKey())
                                || Objects.equal(doctorPigStatusCount.getStatus(), PigStatus.Mate.getKey())
                                || Objects.equal(doctorPigStatusCount.getStatus(), PigStatus.FEED.getKey())
                                || Objects.equal(doctorPigStatusCount.getStatus(), PigStatus.Wean.getKey())) {
                            PigStatus status = PigStatus.from(doctorPigStatusCount.getStatus());
                            map.put(status.getName(), doctorPigStatusCount.getCount());
                        }

                        if (Objects.equal(doctorPigStatusCount.getStatus(), PigStatus.Pregnancy.getKey())
                                || Objects.equal(doctorPigStatusCount.getStatus(), PigStatus.Farrow.getKey())) {
                            yang += doctorPigStatusCount.getCount();
                        }
                    }
                    if (yang != 0) {
                        map.put(PigStatus.Pregnancy.getName(), yang);
                    }

                    List<DoctorPigTrack> konghuaiTrack = doctorPigTrackDao.findByBarnIdAndStatus(doctorBarn.getId(), PigStatus.KongHuai.getKey());

                    int ying = 0;
                    int liuchan = 0;
                    int fanqing = 0;
                    for (DoctorPigTrack pigTrack : konghuaiTrack) {
                        KongHuaiPregCheckResult konghuai = KongHuaiPregCheckResult.from((Integer) pigTrack.getExtraMap().get("pregCheckResult"));
                        switch (konghuai) {
                            case YING:
                                ying++;
                                break;
                            case FANQING:
                                fanqing++;
                                break;
                            case LIUCHAN:
                                liuchan++;
                                break;
                        }
                    }
                    if (ying != 0) {
                        map.put(KongHuaiPregCheckResult.YING.getName(), ying);
                    }

                    if (fanqing != 0) {
                        map.put(KongHuaiPregCheckResult.FANQING.getName(), fanqing);
                    }

                    if (liuchan != 0) {
                        map.put(KongHuaiPregCheckResult.LIUCHAN.getName(), liuchan);
                    }
                }
            }

            //产房
            if (doctorBarn.getPigType() == PigType.DELIVER_SOW.getValue()) {

                List<DoctorGroup> groups = doctorGroupDao.findByCurrentBarnId(barnId);
                if (!groups.isEmpty()) {
                    DoctorGroup group = groups.get(0);
                    DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
                    currentCount += groupTrack.getQuantity();
                    map.put("仔猪", groupTrack.getQuantity());
                    map.put("日龄", groupTrack.getAvgDayAge());
                }
            }
            iotBarnInfo.setCurrentPigs(currentCount);
            return Response.ok(iotBarnInfo);
        } catch (Exception e) {
            log.error("find.iot.barn.info.faled,cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("find.Iot.Barn.Info.failed");
        }
    }

    /**
     * ysq
     * @param barnId
     * @return
     */
    @Override
    public String fingStaffName(Long barnId) {
        return doctorBarnDao.findStaffNameByBarnId(barnId);
    }

    //校验能否转入此舍(产房 => 产房(分娩母猪舍)/保育舍，保育舍 => 保育舍/育肥舍/育种舍，同类型可以互转)
    private Boolean checkCanTransBarn(Integer pigType, Integer barnType) {

        //产房 => 产房(分娩母猪舍)/保育舍
        return (Objects.equal(pigType, PigType.DELIVER_SOW.getValue()) && FARROW_ALLOW_TRANS.contains(barnType))
                //保育舍 => 保育舍/育肥舍/育种舍/后备舍(公母)
                || (Objects.equal(pigType, PigType.NURSERY_PIGLET.getValue()) && NURSERY_ALLOW_TRANS.contains(barnType))
                //育肥舍 => 育肥舍/后备舍(公母)
                || (Objects.equal(pigType, PigType.FATTEN_PIG.getValue()) && FATTEN_ALLOW_TRANS.contains(barnType))
                // 后备群 => 育肥舍/后备舍
                || (Objects.equal(pigType, PigType.RESERVE.getValue()) && (Objects.equal(barnType, PigType.RESERVE.getValue()) || Objects.equal(barnType, PigType.FATTEN_PIG.getValue())))
                //其他 => 同类型
                || (Objects.equal(pigType, barnType));

    }
}
