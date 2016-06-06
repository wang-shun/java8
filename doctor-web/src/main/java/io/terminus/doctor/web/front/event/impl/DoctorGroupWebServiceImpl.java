package io.terminus.doctor.web.front.event.impl;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.basic.model.DoctorBreed;
import io.terminus.doctor.basic.model.DoctorGenetic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.event.group.input.DoctorAntiepidemicGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorChangeGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorCloseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorDiseaseGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorLiveStockGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorMoveInGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorNewGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransFarmGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTransGroupInput;
import io.terminus.doctor.event.dto.event.group.input.DoctorTurnSeedGroupInput;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.service.DoctorBarnReadService;
import io.terminus.doctor.event.service.DoctorGroupReadService;
import io.terminus.doctor.event.service.DoctorGroupWriteService;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorOrg;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorOrgReadService;
import io.terminus.doctor.web.front.event.service.DoctorGroupWebService;
import io.terminus.pampas.common.UserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.terminus.common.utils.Arguments.isEmpty;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
public class DoctorGroupWebServiceImpl implements DoctorGroupWebService {

    private final DoctorGroupWriteService doctorGroupWriteService;
    private final DoctorFarmReadService doctorFarmReadService;
    private final DoctorBasicReadService doctorBasicReadService;
    private final DoctorBarnReadService doctorBarnReadService;
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorOrgReadService doctorOrgReadService;

    @Autowired
    public DoctorGroupWebServiceImpl(DoctorGroupWriteService doctorGroupWriteService,
                                     DoctorFarmReadService doctorFarmReadService,
                                     DoctorBasicReadService doctorBasicReadService,
                                     DoctorBarnReadService doctorBarnReadService,
                                     DoctorGroupReadService doctorGroupReadService,
                                     DoctorOrgReadService doctorOrgReadService) {
        this.doctorGroupWriteService = doctorGroupWriteService;
        this.doctorFarmReadService = doctorFarmReadService;
        this.doctorBasicReadService = doctorBasicReadService;
        this.doctorBarnReadService = doctorBarnReadService;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorOrgReadService = doctorOrgReadService;
    }

    @Override
    public Response<Long> createNewGroup(DoctorNewGroupInput newGroupInput) {
        try {
            //1.校验猪群号是否重复
            checkGroupCodeExist(newGroupInput.getFarmId(), newGroupInput.getGroupCode());

            //2.构造猪群信息
            return doctorGroupWriteService.createNewGroup(getNewGroup(newGroupInput), newGroupInput);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create new group failed, newGroupInput:{}, cause:{}", newGroupInput, Throwables.getStackTraceAsString(e));
            return Response.fail("group.event.create.fail");
        }
    }

    //构造新建猪群信息
    private DoctorGroup getNewGroup(DoctorNewGroupInput newGroupInput) {
        newGroupInput.setIsAuto(IsOrNot.NO.getValue());

        newGroupInput.setBarnName(getBarnName(newGroupInput.getBarnId()));
        newGroupInput.setBreedName(getBreedName(newGroupInput.getBreedId()));
        newGroupInput.setGeneticName(getGeneticName(newGroupInput.getGeneticId()));

        //事件录入人信息
        newGroupInput.setCreatorId(UserUtil.getUserId());
        newGroupInput.setCreatorName(UserUtil.getCurrentUser().getName());

        DoctorGroup group = BeanMapper.map(newGroupInput, DoctorGroup.class);

        //设置猪场公司信息
        DoctorFarm farm = RespHelper.orServEx(doctorFarmReadService.findFarmById(group.getFarmId()));
        group.setFarmName(farm.getName());

        DoctorOrg org = RespHelper.orServEx(doctorOrgReadService.findOrgById(farm.getOrgId()));
        group.setOrgId(org.getId());
        group.setOrgName(org.getName());
        return group;
    }

    @Override
    public Response<Boolean> createGroupEvent(Long groupId, Integer eventType, Map<String, Object> params) {
        try {
            //1.校验猪群是否存在
            DoctorGroupDetail groupDetail = checkGroupExist(groupId);

            //2.校验能否操作此事件
            checkEventTypeIllegal(groupId, eventType);

            //3.put一些字段
            putFields(params);

            //4.根据不同的事件类型调用不同的录入接口
            GroupEventType groupEventType = checkNotNull(GroupEventType.from(eventType));
            switch (groupEventType) {
                case MOVE_IN:
                    RespHelper.or500(doctorGroupWriteService.groupEventMoveIn(groupDetail, BeanMapper.map(params, DoctorMoveInGroupInput.class)));
                    break;
                case CHANGE:
                    RespHelper.or500(doctorGroupWriteService.groupEventChange(groupDetail, BeanMapper.map(params, DoctorChangeGroupInput.class)));
                    break;
                case TRANS_GROUP:
                    RespHelper.or500(doctorGroupWriteService.groupEventTransGroup(groupDetail, BeanMapper.map(params, DoctorTransGroupInput.class)));
                    break;
                case TURN_SEED:
                    RespHelper.or500(doctorGroupWriteService.groupEventTurnSeed(groupDetail, BeanMapper.map(params, DoctorTurnSeedGroupInput.class)));
                    break;
                case LIVE_STOCK:
                    RespHelper.or500(doctorGroupWriteService.groupEventLiveStock(groupDetail, BeanMapper.map(params, DoctorLiveStockGroupInput.class)));
                    break;
                case DISEASE:
                    RespHelper.or500(doctorGroupWriteService.groupEventDisease(groupDetail, BeanMapper.map(params, DoctorDiseaseGroupInput.class)));
                    break;
                case ANTIEPIDEMIC:
                    RespHelper.or500(doctorGroupWriteService.groupEventAntiepidemic(groupDetail, BeanMapper.map(params, DoctorAntiepidemicGroupInput.class)));
                    break;
                case TRANS_FARM:
                    RespHelper.or500(doctorGroupWriteService.groupEventTransFarm(groupDetail, BeanMapper.map(params, DoctorTransFarmGroupInput.class)));
                    break;
                case CLOSE:
                    RespHelper.or500(doctorGroupWriteService.groupEventClose(groupDetail, BeanMapper.map(params, DoctorCloseGroupInput.class)));
                    break;
                default:
                    return Response.fail("event.type.error");
            }
            return Response.ok(Boolean.TRUE);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("create group event failed, groupId:{}, eventType:{}, params:{}, cause:{}",
                    groupId, eventType, params, Throwables.getStackTraceAsString(e));
            return Response.fail("create.group.event.fail");
        }
    }

    @Override
    public Response<String> generateGroupCode(String barnName) {
        if (isEmpty(barnName)) {
            return Response.ok();
        }
        return Response.ok(barnName + "(" +DateUtil.toDateString(new Date()) + ")");
    }

    //校验猪群是否存在
    private DoctorGroupDetail checkGroupExist(Long groupId) {
        return checkNotNull(RespHelper.or500(doctorGroupReadService.findGroupDetailByGroupId(groupId)), "group.not.exist");
    }

    //校验猪群号是否重复
    private void checkGroupCodeExist(Long farmId, String groupCode) {
        List<DoctorGroup> groups = RespHelper.or500(doctorGroupReadService.findGroupsByFarmId(farmId));
        if (groups.stream().map(DoctorGroup::getGroupCode).collect(Collectors.toList()).contains(groupCode)) {
            throw new ServiceException("group.code.exist");
        }
    }

    //校验事件类型是否合法
    private void checkEventTypeIllegal(Long groupId, Integer eventType) {
        List<Integer> eventTypes = RespHelper.or500(doctorGroupReadService.findEventTypesByGroupIds(Lists.newArrayList(groupId)));
        if (!eventTypes.contains(eventType)) {
            throw new ServiceException("event.type.illegal");
        }
    }

    //获取猪舍名称
    private String getBarnName(Long barnId) {
        return barnId == null ? null : RespHelper.or(doctorBarnReadService.findBarnById(barnId), new DoctorBarn()).getName();
    }

    //获取品种名称
    private String getBreedName(Long breedId) {
        return breedId == null ? null : RespHelper.or(doctorBasicReadService.findBreedById(breedId), new DoctorBreed()).getName();
    }

    //获取品系名称
    private String getGeneticName(Long geneticId) {
        return geneticId == null ? null : RespHelper.or(doctorBasicReadService.findGeneticById(geneticId), new DoctorGenetic()).getName();
    }

    //put一些关联字段
    private void putFields(Map<String, Object> params) {
        //手工录入, 记录下创建人
        params.put("isAuto", IsOrNot.NO.getValue());
        params.put("creatorId", UserUtil.getUserId());
        params.put("creatorName", UserUtil.getCurrentUser().getName());

        //id关联字段
        params.put("barnName", getBarnName(Params.get(params, "barnId")));
        params.put("breedName", getBreedName(Params.get(params, "breedName")));
        params.put("geneticName", getGeneticName(Params.get(params, "geneticId")));
        Params.filterNullOrEmpty(params);
    }
}
