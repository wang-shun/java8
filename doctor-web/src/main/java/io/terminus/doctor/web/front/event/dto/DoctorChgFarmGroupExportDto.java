package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.Data;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorChgFarmGroupExportDto extends DoctorGroupEvent{

    private Long fromFarmId;

    private String fromFarmName;

    private Long toFarmId;

    private String toFarmName;

    private Long fromBarnId;

    private String fromBarnName;

    private Long toBarnId;

    private String toBarnName;

    /**
     * 转入猪舍的猪类
     */
    private Integer toBarnType;

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
