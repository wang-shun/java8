package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.PageInfo;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.common.utils.BeanMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorEventModifyRequestDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dto.DoctorEventModifyRequestDto;
import io.terminus.doctor.event.model.DoctorEventModifyRequest;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by xjn on 17/3/11.
 * 编辑事件请求读事件
 */
@Slf4j
@Service
@RpcProvider
public class DoctorEventModifyRequestReadServiceImpl implements DoctorEventModifyRequestReadService {
    @Autowired
    private DoctorEventModifyRequestDao modifyRequestDao;
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorGroupEventDao doctorGroupEventDao;

    private JsonMapperUtil JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;
    @Override
    public Response<DoctorEventModifyRequest> findById(@NotNull(message = "requestId.not.null") Long requestId) {
        try {
            return Response.ok(modifyRequestDao.findById(requestId));
        } catch (Exception e) {
            log.info("find by id failed, requestId:{}, cause:{}", requestId, Throwables.getStackTraceAsString(e));
            return Response.fail("find.by.id.failed");
        }
    }

    @Override
    public Response<Paging<DoctorEventModifyRequestDto>> pagingRequest(DoctorEventModifyRequest modifyRequest, Integer pageNo, Integer pageSize) {
        try {
            PageInfo pageInfo = PageInfo.of(pageNo, pageSize);
            Paging<DoctorEventModifyRequest> paging = modifyRequestDao.paging(pageInfo.getOffset(), pageInfo.getLimit(), BeanMapper.convertObjectToMap(modifyRequest));
            List<DoctorEventModifyRequestDto> list = paging.getData().stream()
                    .map(request -> {
                        return buildDoctorEventModifyRequestDto(request);
                    }).collect(Collectors.toList());
            return Response.ok(new Paging<>(paging.getTotal(), list));
        } catch (Exception e) {
            log.info("paging request failed, modifyRequest:{}, cause:{}", modifyRequest, Throwables.getStackTraceAsString(e));
            return Response.fail("paging.request.failed");
        }
    }

    @Override
    public Response<DoctorEventModifyRequestDto> findDtoById(@NotNull(message = "requestId.not.null") Long requestId) {
        Response<DoctorEventModifyRequestDto> result = new Response<>();
        DoctorEventModifyRequest doctorEventModifyRequest = modifyRequestDao.findById(requestId);
        if(!Objects.isNull(doctorEventModifyRequest)){
            result.setResult(buildDoctorEventModifyRequestDto(doctorEventModifyRequest));
        }
        return result;
    }

    public DoctorEventModifyRequestDto buildDoctorEventModifyRequestDto(DoctorEventModifyRequest request) {
        DoctorEventModifyRequestDto dto = BeanMapper.map(request, DoctorEventModifyRequestDto.class);
        if (Objects.equals(request.getType(), DoctorEventModifyRequest.TYPE.PIG.getValue())) {
            dto.setOldPigEvent(doctorPigEventDao.findEventById(request.getEventId()));
            dto.setNewPigEvent(JSON_MAPPER.fromJson(request.getContent(), DoctorPigEvent.class));
        } else {
            dto.setOldGroupEvent(doctorGroupEventDao.findEventById(request.getEventId()));
            dto.setNewGroupEvent(JSON_MAPPER.fromJson(request.getContent(), DoctorGroupEvent.class));
        }
        return dto;
    }
}
