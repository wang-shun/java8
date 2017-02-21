package io.terminus.doctor.web.admin.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: admin 分配集团账号所需数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/17
 */
@Data
public class DoctorGroupUserAuth implements Serializable {
    private static final long serialVersionUID = 2447804682355892464L;

    /**
     * 用户id
     */
    private Long userId;

    private List<Long> farmIds;

    private List<Long> barnIds;

}