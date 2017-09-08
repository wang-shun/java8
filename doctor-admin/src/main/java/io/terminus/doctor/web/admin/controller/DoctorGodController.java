package io.terminus.doctor.web.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.common.enums.SourceType;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.Params;
import io.terminus.doctor.common.utils.RespWithExHelper;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.search.SearchedPig;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.*;
import io.terminus.doctor.event.service.*;
import io.terminus.doctor.web.admin.dto.DoctorGroupEventDetail;
import io.terminus.doctor.web.admin.utils.TransFromUtil;
import io.terminus.doctor.web.admin.vo.PigAndPigGroup;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

import static java.util.stream.Collectors.toList;

/**
 * Created by sunbo@terminus.io on 2017/9/8.
 */
@RestController
@RequestMapping("api/doctor/admin/god")
public class DoctorGodController {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper();

    @RpcConsumer
    private DoctorGroupReadService doctorGroupReadService;
    @RpcConsumer
    private DoctorPigEventReadService doctorPigEventReadService;
    @RpcConsumer
    private DoctorPigEventWriteService doctorPigEventWriteService;

    @RpcConsumer
    private DoctorPigReadService doctorPigReadService;
    @RpcConsumer
    private DoctorPigWriteService doctorPigWriteService;

    @Autowired
    private TransFromUtil transFromUtil;


    @RequestMapping(method = RequestMethod.GET, value = "event")
    public Object eventPaging(@RequestParam Map<String, Object> params,
                              @RequestParam(required = false) Integer pageNo,
                              @RequestParam(required = false) Integer pageSize) {


        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        if (params.get("kind") == null || "".equals(params.get("kind"))) {
            params.put("kind", 1);
        }

        //execl导入事件和旧厂迁移事件
        List<Integer> eventSources = new ArrayList<>(2);
        eventSources.add(SourceType.IMPORT.getValue());
        eventSources.add(SourceType.MOVE.getValue());
        params.put("eventSources", eventSources);

        //只允许修改三个月之前的历史事件
        Date threeMonthAgo = DateUtils.addMonths(new Date(), -3);
        if (params.containsKey("endDate")) {
            Date endDate = DateUtil.toDateTime(params.get("endDate").toString());
            if (endDate.after(threeMonthAgo)) {
                params.put("endDate", DateUtil.toDateString(threeMonthAgo));
            }
        } else
            params.put("endDate", DateUtil.toDateString(threeMonthAgo));


        //针对  kind进行识别
        String kind = String.valueOf(params.get("kind"));
        Object result = null;
        switch (kind) {
            case "4":
                //猪群查询事件
                result = this.queryGroupEventsByCriteria(params, pageNo, pageSize);
                break;
            case "1":
                //母猪
                params.put("ordered", 0);
                result = this.queryPigEventsByCriteria(params, pageNo, pageSize);
                break;
            //公猪
            case "2":
                params.put("ordered", 0);
                result = this.queryPigEventsByCriteria(params, pageNo, pageSize);
                break;
            case "3":
                params.put("ordered", 0);
                result = this.queryPigEventsByCriteria(params, pageNo, pageSize);
                break;
            default:
                result = Paging.empty();
                break;
        }
        return result;


    }

    @RequestMapping(method = RequestMethod.DELETE, value = "event/{id}")
    public Boolean eventDelete(@PathVariable Long id) {

        //TODO 猪断奶事件 联动猪群断奶事件
        return RespWithExHelper.orInvalid(doctorPigEventWriteService.delete(id));
    }


    @RequestMapping(method = RequestMethod.PUT, value = "event")
    public Boolean eventEdit() {

        return true;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "report/refresh")
    public void refreshReport() {

    }


    @RequestMapping(method = RequestMethod.GET, value = "pig")
    public PigAndPigGroup pigAndGroupQuery(@RequestParam Long farmId,
                                           @RequestParam Integer pigType,//0母猪，1公猪,2猪群
                                           @RequestParam Long pigCode) {

        PigAndPigGroup vo = new PigAndPigGroup();

        if (pigType == 2) {
            Response<DoctorGroup> groupResponse = doctorGroupReadService.findGroupByFarmIdAndGroupCode(farmId, pigCode.toString());
            if (!groupResponse.isSuccess())
                throw new JsonResponseException(groupResponse.getError());
            if (null == groupResponse.getResult())
                throw new JsonResponseException("pig.group.not.found");

            DoctorGroup group = groupResponse.getResult();
            Response<DoctorGroupDetail> groupDetailResponse = doctorGroupReadService.findGroupDetailByGroupId(group.getId());
            if (!groupDetailResponse.isSuccess())
                throw new JsonResponseException(groupDetailResponse.getError());
            if (null == groupDetailResponse.getResult() || null == groupDetailResponse.getResult().getGroupTrack())
                throw new JsonResponseException("pig.group.track.not.found");

            DoctorGroupTrack groupTrack = groupDetailResponse.getResult().getGroupTrack();

            vo.setQuantity(groupTrack.getQuantity());//猪群猪的数量
            vo.setAvgDayAge(groupTrack.getAvgDayAge());//平均日龄
            vo.setOpenAt(group.getOpenAt());//猪群建群日期
            vo.setCurrentBarnId(group.getCurrentBarnId());//猪群猪舍
            vo.setUnweanQty(groupTrack.getUnweanQty());
            vo.setHealthyQty(groupTrack.getHealthyQty());
            vo.setWeakQty(groupTrack.getWeakQty());
            vo.setLiveQty(groupTrack.getLiveQty());

        } else {
            Map<String, Object> params = new HashMap<>();
            params.put("farmId", farmId);
            params.put("pigType", pigType);
            params.put("pigCode", pigCode);
            Response<Paging<SearchedPig>> pagingResponse = doctorPigReadService.pagingPig(params, 1, 1);
            if (!pagingResponse.isSuccess())
                throw new JsonResponseException(pagingResponse.getError());
            if (pagingResponse.getResult().getData().isEmpty())
                throw new JsonResponseException("pig.not.found");

            SearchedPig pig = pagingResponse.getResult().getData().get(0);

            Response<DoctorPigTrack> pigTrackResponse = doctorPigReadService.findPigTrackByPigId(pig.getId());
            if (!pigTrackResponse.isSuccess())
                throw new JsonResponseException(pigTrackResponse.getError());
            if (null == pigTrackResponse.getResult())
                throw new JsonResponseException("pig.track.not.found");
            DoctorPigTrack pigTrack = pigTrackResponse.getResult();

            vo.setStatus(pig.getStatus());
            if (vo.getStatus().intValue() == PigStatus.KongHuai.getKey().intValue()) {
                vo.setStatus(pig.getPregCheckResult());
            }
            vo.setBreedId(pig.getBreedId());//品种
            vo.setCurrentBarnId(pig.getCurrentBarnId());//舍号
            vo.setCurrentParity(pig.getCurrentParity());//胎次
            vo.setGeneticId(pig.getGeneticId());//品系

            vo.setBoarType(pig.getBoarType());//公猪类型
            vo.setIsRemoval(pigTrack.getIsRemoval());//是否离场
            vo.setFarrowQty(pigTrack.getFarrowQty());//分娩数量
            vo.setUnweanQty(pigTrack.getUnweanQty());//未断奶数量
            vo.setFarrowAvgWeight(pigTrack.getFarrowAvgWeight());//分娩均重(kg)
            vo.setWeanQty(pigTrack.getWeanQty());//断奶数量
            vo.setWeanAvgWeight(pigTrack.getWeanAvgWeight());//断奶均重(kg)
        }
        return vo;
    }

    @RequestMapping(method = RequestMethod.PUT, value = "pig")
    public void pigAndGroupEdit(@Valid PigAndPigGroup pigAndPigGroup, Errors errors) {
        if (errors.hasErrors())
            throw new JsonResponseException(errors.getFieldError().getDefaultMessage());

        if (pigAndPigGroup.getType() == 0) {//修改猪


        } else if (pigAndPigGroup.getType() == 1) {//修改猪群

        } else
            throw new JsonResponseException("god.pig.and.group.type.not.support");
    }


    public Paging<DoctorPigEvent> queryPigEventsByCriteria(@RequestParam Map<String, Object> params, @RequestParam(required = false) Integer pageNo, @RequestParam(required = false) Integer pageSize) {
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }

        if (StringUtils.isNotBlank((String) params.get("barnTypes"))) {
            params.put("barnTypes", Splitters.UNDERSCORE.splitToList((String) params.get("barnTypes")));
        }

        if (StringUtils.isNotBlank((String) params.get("pigCode"))) {
            params.put("pigCodeFuzzy", params.get("pigCode"));
            params.remove("pigCode");
        }

        Response<Paging<DoctorPigEvent>> pigEventPagingResponse = doctorPigEventReadService.queryPigEventsByCriteria(params, pageNo, pageSize);
        if (!pigEventPagingResponse.isSuccess()) {
            return Paging.empty();
        }
        transFromUtil.transFromExtraMap(pigEventPagingResponse.getResult().getData());
        return pigEventPagingResponse.getResult();
    }


    private Paging<DoctorGroupEventDetail> queryGroupEventsByCriteria(Map<String, Object> params, Integer pageNo, Integer pageSize) {
        if (params == null || params.isEmpty()) {
            return Paging.empty();
        }
        params = Params.filterNullOrEmpty(params);
        if (params.get("eventTypes") != null) {
            params.put("types", Splitters.COMMA.splitToList((String) params.get("eventTypes")));
            params.remove("eventTypes");
        }

        if (StringUtils.isNotBlank((String) params.get("pigTypes"))) {
            params.put("pigTypes", Splitters.UNDERSCORE.splitToList((String) params.get("pigTypes")));
        }

        if (StringUtils.isNotBlank((String) params.get("changeTypeIds"))) {
            params.put("changeTypeIds", Splitters.UNDERSCORE.splitToList((String) params.get("changeTypeIds")));
        }
        if (StringUtils.isNotBlank((String) params.get("groupCode"))) {
            params.put("groupCodeFuzzy", params.get("groupCode"));
            params.remove("groupCode");
        }
        Response<Paging<DoctorGroupEvent>> pagingResponse = doctorGroupReadService.queryGroupEventsByCriteria(params, pageNo, pageSize);
        if (!pagingResponse.isSuccess()) {
            return Paging.empty();
        }
        List<DoctorGroupEventDetail> groupEventDetailList = pagingResponse.getResult().getData().stream()
                .map(doctorGroupEvent -> {
                    DoctorGroupEventDetail detail = OBJECT_MAPPER.convertValue(doctorGroupEvent, DoctorGroupEventDetail.class);
                    Boolean isRollback = false;
                    Response<Boolean> booleanResponse = doctorGroupReadService.eventCanRollback(doctorGroupEvent.getId());
                    if (booleanResponse.isSuccess()) {
                        isRollback = booleanResponse.getResult();
                    }
                    detail.setIsRollback(isRollback);
                    return detail;
                }).collect(toList());
        return new Paging<>(pagingResponse.getResult().getTotal(), groupEventDetailList);
    }

}
