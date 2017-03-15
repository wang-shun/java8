package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorGroupEvent;
import lombok.Data;

/**
 * Created by terminus on 2017/3/15.
 */
@Data
public class DoctorTurnSeedGroupExportDto extends DoctorGroupEvent{

    /**
     * 转种猪后的猪号
     */
    private String pigCode;

    /**
     * 母亲猪 耳缺号
     */
    private String motherEarCode;

    /**
     * 耳缺号
     */
    private String earCode;

    /**
     * 转入日期
     */
    private String transInAt;

    /**
     * 出生日期
     */
    private String birthDate;

    private Long breedId;

    private String breedName;

    private Long geneticId;

    private String geneticName;

    private Long toBarnId;

    private String toBarnName;

    private Integer toBarnType;

}
