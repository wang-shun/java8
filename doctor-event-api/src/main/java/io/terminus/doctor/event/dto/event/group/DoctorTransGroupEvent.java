package io.terminus.doctor.event.dto.event.group;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群转群事件
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/26
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorTransGroupEvent extends BaseGroupEvent implements Serializable {
    private static final long serialVersionUID = 6839821016584201716L;

    /**
     * 转群日期
     */
    private Date transGroupAt;

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
     * @see io.terminus.doctor.event.enums.IsCreateGroup
     */
    private Integer isCreateGroup;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    private Integer source;

    private Long breedId;

    private String breedName;

    private Integer boarQty;

    private Integer sowQty;
}
