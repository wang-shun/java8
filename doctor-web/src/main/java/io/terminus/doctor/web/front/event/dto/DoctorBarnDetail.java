package io.terminus.doctor.web.front.event.dto;

import io.terminus.common.model.Paging;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * Desc: 猪舍详情(猪与猪群的分页)
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/1
 */
@Data
public class DoctorBarnDetail implements Serializable {
    private static final long serialVersionUID = -910287440715525982L;

    /**
     * 详情类型, 前台根据此状态判断显示哪个页面
     * @see io.terminus.doctor.common.enums.PigSearchType
     */
    private Integer type;

    /**
     * 猪的分页
     */
    private Paging<DoctorPigInfoDto> pigPaging;

    /**
     * 猪群: 猪类
     */
    private Integer groupType;

    /**
     * 猪群分页
     */
    private Paging<DoctorGroupDetail> groupPaging;

    /**
     * 猪: 猪的状态
     */
    private Set<Integer> statuses;
}
