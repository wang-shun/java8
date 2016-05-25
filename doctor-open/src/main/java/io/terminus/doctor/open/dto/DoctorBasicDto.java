package io.terminus.doctor.open.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBasicDto implements Serializable {
    private static final long serialVersionUID = 7618953726708026762L;

    /**
     * 公司概况
     */
    private DoctorOrgBasicDto orgBasic;

    /**
     * 猪场概况
     */
    private List<DoctorFarmBasicDto> farmsBasic;
}
