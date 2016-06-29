package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

/**
 * Desc: 防疫事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorAntiepidemicGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = -4905325320735230119L;

    /**
     *  疫苗id
     */
    private Long vaccinId;

    /**
     *  疫苗名称
     */
    private String vaccinName;

    /**
     *  防疫结果: 1:阳性 -1:阴性
     *  @see io.terminus.doctor.event.dto.event.group.DoctorAntiepidemicGroupEvent.VaccinResult
     */
    private Integer vaccinResult;

    /**
     * 防疫项目id
     */
    private Long vaccinItemId;

    /**
     * 防疫项目名称
     */
    private String vaccinItemName;

    /**
     *  防疫人员id
     */
    private Integer vaccinStaffId;

    /**
     *  防疫人员名称
     */
    private String vaccinStaffName;

    /**
     * 防疫猪只数
     */
    private Integer quantity;

    public enum VaccinResult {
        POSITIVE(1, "阳性"),
        NEGATIVE(-1, "阴性");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        VaccinResult(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }
    }
}
