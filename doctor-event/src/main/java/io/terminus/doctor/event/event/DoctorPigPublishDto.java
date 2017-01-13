package io.terminus.doctor.event.event;

import io.terminus.doctor.event.model.DoctorPig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Desc: 批量事件时，发布事件需要的数据
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/1/9
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DoctorPigPublishDto implements Serializable {
    private static final long serialVersionUID = -3312764468271438715L;

    private Long pigId;

    /**
     * 猪的类型(性别)
     * @see DoctorPig.PigSex
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
    private final boolean equalsByFunc(DoctorPigPublishDto that, Function<DoctorPigPublishDto, ?>... funcs) {
        if (this == that) return true;
        if (that == null) return false;

        boolean isEqual = true;
        for (Function<DoctorPigPublishDto, ?> func : funcs) {
            if (!Objects.equals(func.apply(this), func.apply(that))) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }

    @SafeVarargs
    final boolean containsBy(List<DoctorPigPublishDto> dtos, Function<DoctorPigPublishDto, ?>... funcs) {
        for (DoctorPigPublishDto element : dtos) {
            if (this.equalsByFunc(element, funcs)) {
                return true;
            }
        }
        return false;
    }
}
