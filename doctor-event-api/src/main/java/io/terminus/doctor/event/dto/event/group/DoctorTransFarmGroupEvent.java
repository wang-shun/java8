package io.terminus.doctor.event.dto.event.group;

import io.terminus.doctor.event.enums.IsOrNot;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Desc: 转场事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorTransFarmGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = 6461145733173225452L;

    private Long fromFarmId;

    private String fromFarmName;

    private Long toFarmId;

    private String toFarmName;

    private Long fromBarnId;

    private String fromBarnName;

    private Long toBarnId;

    private String toBarnName;

    private Long fromGroupId;

    private String fromGroupCode;

    private Long toGroupId;

    private String toGroupCode;

    /**
     * 是否新建猪群 0:否 1:是
     * @see IsOrNot
     */
    private Integer isCreateGroup;

    private Long breedId;

    private String breedName;

    private Integer boarQty;

    private Integer sowQty;
}
