package io.terminus.doctor.web.front.event.dto;

import io.terminus.common.model.Paging;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

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
     * @see io.terminus.doctor.web.front.event.dto.DoctorBarnDetail.Type
     */
    private Integer type;

    /**
     * 猪的分页
     */
    private Paging<DoctorPigInfoDto> pigPaging;

    /**
     * 猪群分页
     */
    private Paging<DoctorGroupDetail> groupPaging;

    /**
     * 猪: 猪的状态, 猪群: 猪类
     */
    private List<Integer> status;

    public enum Type {

        BOAR(1, "公猪"),
        SOW(2, "母猪"),
        GROUP(3, "猪群");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        Type(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }
    }
}
