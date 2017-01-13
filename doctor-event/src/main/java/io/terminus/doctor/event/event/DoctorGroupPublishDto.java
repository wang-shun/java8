package io.terminus.doctor.event.event;

import com.google.common.collect.Lists;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
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
@Data
public class DoctorGroupPublishDto implements Serializable {
    private static final long serialVersionUID = 2917349097446538641L;

    private Long groupId;

    private Long eventId;

    private Date eventAt;

    /**
     * 猪舍类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;

    //可指定的equals方法
    private boolean equalsBy(DoctorGroupPublishDto that, List<Function<DoctorGroupPublishDto, ?>> funcs) {
        if (this == that) return true;
        if (that == null) return false;

        boolean isEqual = true;
        for (Function<DoctorGroupPublishDto, ?> func : funcs) {
            if (!Objects.equals(func.apply(this), func.apply(that))) {
                isEqual = false;
                break;
            }
        }
        return isEqual;
    }

    //contains
    private boolean containsBy(List<DoctorGroupPublishDto> dtos, List<Function<DoctorGroupPublishDto, ?>> funcs) {
        for (DoctorGroupPublishDto element : dtos) {
            if (this.equalsBy(element, funcs)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤掉相同的事件
     */
    @SafeVarargs
    static List<DoctorGroupPublishDto> filterBy(List<DoctorGroupPublishDto> pigs, Function<DoctorGroupPublishDto, ?>... func) {
        if (CollectionUtils.isEmpty(pigs)) {
            return Collections.emptyList();
        }
        List<Function<DoctorGroupPublishDto, ?>> funcs = Lists.newArrayList(func);
        List<DoctorGroupPublishDto> results = Lists.newArrayList(pigs.get(0));

        pigs.stream().filter(pig -> !pig.containsBy(results, funcs)).forEach(results::add);
        return results;
    }
}
