package io.terminus.doctor.web.front.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-06-02
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorSowEventCreateService {

    /**
     * 母猪事件信息创建
     * @param doctorBasicInputInfoDto
     * @param sowInfoDtoJson
     * @return
     */
    Response<Long> sowEventCreate(DoctorBasicInputInfoDto doctorBasicInputInfoDto, String sowInfoDtoJson);

    /**
     * 母猪事件信息批量创建方式
     * @param dtoList
     * @param sowInfoDtoJson
     * @return
     */
    Response<Long> sowEventsCreate(List<DoctorBasicInputInfoDto> dtoList, String sowInfoDtoJson);

    /**
     * 普通事件信息创建
     * @param dtoList
     * @param sowInfoDtoJson
     * @return
     */
    Response<Boolean> casualEventsCreate(List<DoctorBasicInputInfoDto> dtoList, String sowInfoDtoJson);

    /**
     * Vaccination Medical Consume 事件领用信息 TODO 信息结果录入错误
     * @param paramsJson
     * @return
     */
//    Response<Boolean> sowPigsEventCreateByConsume(String paramsJson);
}
