package io.terminus.doctor.msg.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.msg.dto.DoctorMessageUserDto;
import io.terminus.doctor.msg.model.DoctorMessageUser;

import java.util.List;

/**
 * Created by xiao on 16/10/11.
 */
public interface DoctorMessageUserReadService {
    /**
     * 根据id获取doctormessageUser
     * @param id
     * @return
     */
    Response<DoctorMessageUser> findDoctorMessageUserById(Long id);

    /**
     * 分页查询
     * @param doctorMessageUserDto
     * @return
     */
    Response<Paging<DoctorMessageUser>> paging(DoctorMessageUserDto doctorMessageUserDto, Integer pageNo, Integer pageSize);

    /**
     * 获取未读站内信的数量
     * @param userId    用户id
     * @return
     */
    Response<Long> findNoReadCount(Long userId);

    /**
     * 根据条件获取列表
     * @param doctorMessageUserDto
     * @return
     */
    Response<List<DoctorMessageUser>> findDoctorMessageUsersByCriteria(DoctorMessageUserDto doctorMessageUserDto);

    /**
     * 根据条件获取businessId列表
     * @param doctorMessageUserDto
     * @return
     */
    Response<List<Long>> findBusinessListByCriteria(DoctorMessageUserDto doctorMessageUserDto);

    /**
     * 获取未发送的短信消息
     */
    Response<Paging<DoctorMessageUser>> findMsgMessage(Integer pageNo, Integer pageSize);

    /**
     * 获取未发送的email消息
     */
    Response<Paging<DoctorMessageUser>> findEmailMessage(Integer pageNo, Integer pageSize);

    /**
     * 获取未发送的app推送消息
     */
    Response<Paging<DoctorMessageUser>> findAppPushMessage(Integer pageNo, Integer pageSize);
}
