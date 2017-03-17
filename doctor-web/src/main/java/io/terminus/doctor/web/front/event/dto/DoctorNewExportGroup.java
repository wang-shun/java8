package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorNewExportGroup extends DoctorGroupEvent {

    private static final long serialVersionUID = -6774030780276956182L;

    /**
     * 来源 1 本场, 2 外购
     * @see io.terminus.doctor.event.enums.PigSource
     */
    private String source;

    /**
     * 猪场id
     */
    @NotNull(message = "farmId.not.null")
    private Long farmId;

    /**
     * 猪群号
     */
    @NotEmpty(message = "groupCode.not.empty")
    private String groupCode;

    /**
     * 猪舍id
     */
    @NotNull(message = "barnId.not.null")
    private Long barnId;

    /**
     * 猪舍名称
     */
    @NotEmpty(message = "barnName.not.empty")
    private String barnName;

    /**
     * 猪类 枚举9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    private String pigTypeName;

    /**
     * 性别 1:混合 2:母 3:公
     * @see io.terminus.doctor.event.model.DoctorGroupTrack.Sex
     */
    @NotNull(message = "sex.not.null")
    private String sexName;

    /**
     * 品种id
     */
    private Long breedId;

    /**
     * 品种name
     */
    private String breedName;

    /**
     * 品系id
     */
    private Long geneticId;

    /**
     * 品系name
     */
    private String geneticName;

    /**
     * 当前状态
     */
    private String currentStatus;
}
