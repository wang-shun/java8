package io.terminus.doctor.event.event;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.model.DoctorPig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
    private boolean equalsBy(DoctorPigPublishDto that, List<Function<DoctorPigPublishDto, ?>> funcs) {
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

    //contains
    private boolean containsBy(List<DoctorPigPublishDto> dtos, List<Function<DoctorPigPublishDto, ?>> funcs) {
        for (DoctorPigPublishDto element : dtos) {
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
    static List<DoctorPigPublishDto> filterBy(List<DoctorPigPublishDto> pigs, Function<DoctorPigPublishDto, ?>... func) {
        if (CollectionUtils.isEmpty(pigs)) {
            return Collections.emptyList();
        }
        List<Function<DoctorPigPublishDto, ?>> funcs = Lists.newArrayList(func);
        List<DoctorPigPublishDto> results = Lists.newArrayList(pigs.get(0));

        pigs.stream().filter(pig -> !pig.containsBy(results, funcs)).forEach(results::add);
        return results;
    }
}
