package io.terminus.doctor.event.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc: 猪群统计
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/3
 */
@Data
public class DoctorGroupCount implements Serializable {
    private static final long serialVersionUID = -7198222555477065587L;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 产房仔猪
     */
    private long farrowCount;

    /**
     * 保育猪
     */
    private long nurseryCount;

    /**
     * 育肥猪
     */
    private long fattenCount;

    /**
     * 后备猪
     */
    private long houbeiCount;

}
