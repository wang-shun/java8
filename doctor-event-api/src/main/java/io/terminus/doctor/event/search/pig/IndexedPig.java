package io.terminus.doctor.event.search.pig;

import io.terminus.doctor.event.enums.PigStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪(索引对象)
 *      @see io.terminus.doctor.event.model.DoctorPig
 *      @see io.terminus.doctor.event.model.DoctorPigTrack
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexedPig implements Serializable {
    private static final long serialVersionUID = 1121581546502813499L;

    private Long id;
    private String pigCode;
    // 搜索pigCode
    private String pigCodeSearch;

    /**
     * 公司
     */
    private Long orgId;
    private String orgName;

    /**
     * 猪场
     */
    private Long farmId;
    private String farmName;

    /**
     * 猪类型
     * @see io.terminus.doctor.event.model.DoctorPig.PIG_TYPE
     */
    private Integer pigType;
    private String pigTypeName;

    /**
     * 父亲和母亲
     */
    private String pigFatherCode;
    private String pigMotherCode;

    /**
     * 来源
     */
    private Integer source;

    /**
     * 出生
     */
    private Date birthDate;
    private Double birthWeight;

    /**
     * 进场
     */
    private Date inFarmDate;
    private Integer inFarmDayAge;

    /**
     * 初始化猪舍
     */
    private Long initBarnId;
    private String initBarnName;

    /**
     * 品种
     */
    private Long breedId;
    private String breedName;

    /**
     * 品系
     */
    private Long geneticId;
    private String geneticName;

    /**
     * 状态
     * @see PigStatus
     */
    private Integer status;
    private String statusName;

    /**
     * 当前猪舍
     */
    private Long currentBarnId;
    private String currentBarnName;

    /**
     * 重量
     */
    private Double weight;

    /**
     * 出场时间
     */
    private Date outFarmDate;

    /**
     * 当前胎次
     */
    private Integer currentParity;

    private Date updatedAt;

    /**
     * 是否离场
     */
    private Integer isRemoval;
}
