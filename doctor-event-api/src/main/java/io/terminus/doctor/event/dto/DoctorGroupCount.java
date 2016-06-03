package io.terminus.doctor.event.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc: 猪群统计
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/3
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorGroupCount implements Serializable {
    private static final long serialVersionUID = -7198222555477065587L;

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
}
