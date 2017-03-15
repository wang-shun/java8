package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.Data;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorMoveInGroupExportDto extends DoctorGroupEvent{

    private static final long serialVersionUID = -6419739828741563815L;

    /**
     * 猪群转移类型
     * @see io.terminus.doctor.event.dto.event.group.DoctorMoveInGroupEvent.InType
     */
    private Integer inType;

    /**
     * 猪群转移类型名
     */
    private String inTypeName;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    private String source;

    /**
     * 性别 1:混合 2:母 3:公
     * @see io.terminus.doctor.event.model.DoctorGroupTrack.Sex
     */
    private String sex;

    private Long breedId;

    private String breedName;

    /**
     * 群间转移来源猪舍信息
     */
    private Long fromBarnId;

    private String fromBarnName;

    private Integer fromBarnType;

    /**
     * 群间转移来源猪群信息
     */
    private Long fromGroupId;

    private String fromGroupCode;

    /**
     * 母猪id
     */
    private Long sowPigId;

    /**
     * 母猪胎次
     */
    private Integer sowParity;

    private Integer boarQty;

    private Integer sowQty;

    /**
     * 金额(分)
     */
    private Long amount;
}
