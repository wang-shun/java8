package io.terminus.doctor.event.dto;

import com.google.common.collect.Lists;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.event.model.DoctorGroup;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import static io.terminus.common.utils.Arguments.notEmpty;

/**
 * Desc: 猪群查询条件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorGroupSearchDto extends DoctorGroup implements Serializable {
    private static final long serialVersionUID = -7372130840721095447L;

    private Date startOpenAt;   //建群开始时间

    private Date endOpenAt;     //建群结束时间

    private Date startCloseAt;  //关群开始时间

    private Date endCloseAt;    //关群结束时间

    private List<Integer> pigTypes; //猪类list

    private String pigTypeCommas; //逗号分隔类型

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private List<Long> barnIdList;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private String barnIds;

    public void setBarnIds(String barnIds) {
        this.barnIds = barnIds;
        if (notEmpty(barnIds)) {
            barnIdList = Splitters.splitToLong(barnIds, Splitters.COMMA);
        } else {
            barnIdList = Lists.newArrayList();
        }
    }

    public List<Long> getBarnIdList() {
        if (notEmpty(barnIds)) {
            return Splitters.splitToLong(barnIds, Splitters.COMMA);
        }
        return Lists.newArrayList();
    }
}
