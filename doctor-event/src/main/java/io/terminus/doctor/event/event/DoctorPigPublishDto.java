package io.terminus.doctor.event.event;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.function.Function;

/**
 * Desc: 批量事件时，发布事件需要的数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/1/9
 */
@Data
public class DoctorPigPublishDto implements Serializable {
    private static final long serialVersionUID = -3312764468271438715L;

    private Long pigId;

    /**
     * 猪的类型(性别)
     * @see io.terminus.doctor.event.model.DoctorPig.PIG_TYPE
     */
    private Integer kind;

    private Long eventId;

    private Date eventAt;

    /**
     * 配种类型(如果是配种事件)
     * @see io.terminus.doctor.event.enums.DoctorMatingType
     */
    private Integer mateType;

    /**
     * 妊检结果(如果是妊检事件)
     * @see io.terminus.doctor.event.enums.PregCheckResult
     */
    private Integer pregCheckResult;

    //可指定的equals方法
    @SafeVarargs
    public final <T> boolean equalsByFunc(DoctorPigPublishDto that, Function<DoctorPigPublishDto, T>... funcs) {
        if (this == that) return true;
        if (that == null) return false;

        boolean isEqual = true;
        for (Function<DoctorPigPublishDto, T> func : funcs) {
            if (!Objects.equals(func.apply(this), func.apply(that))) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }
}
