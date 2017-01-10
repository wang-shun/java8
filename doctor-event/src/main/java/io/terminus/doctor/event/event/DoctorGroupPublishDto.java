package io.terminus.doctor.event.event;

import io.terminus.common.utils.Dates;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 批量事件时，发布事件需要的数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/1/9
 */
@ToString
public class DoctorGroupPublishDto implements Serializable {
    private static final long serialVersionUID = 2917349097446538641L;

    @Getter @Setter
    private Long groupId;

    @Getter @Setter
    private Long eventId;

    @Getter @Setter
    private Date eventAt;

    /**
     * 猪舍类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    @Getter @Setter
    private Integer pigType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoctorGroupPublishDto that = (DoctorGroupPublishDto) o;

        return Dates.startOfDay(eventAt).equals(Dates.startOfDay(that.eventAt))
                && pigType.equals(that.pigType);
    }

    @Override
    public int hashCode() {
        int result = eventAt.hashCode();
        result = 31 * result + pigType.hashCode();
        return result;
    }
}
