package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.enums.RollbackType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 回滚发事件携带的信息
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/9/21
 */
@Data
public class DoctorRollbackDto implements Serializable {
    private static final long serialVersionUID = -22978774710787544L;

    private List<RollbackType> rollbackTypes;  //回滚类型

    private Long esBarnId;      //es搜索猪舍id

    private Long esPigId;       //es搜索猪id

    private Long esGroupId;     //es搜索猪群id
}
