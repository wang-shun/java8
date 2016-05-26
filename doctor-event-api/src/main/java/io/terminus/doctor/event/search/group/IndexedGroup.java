package io.terminus.doctor.event.search.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群(索引对象)
 *      @see io.terminus.doctor.event.model.DoctorGroup
 *      @see io.terminus.doctor.event.model.DoctorGroupTrack
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexedGroup implements Serializable {
    private static final long serialVersionUID = 8463331809080229435L;

    private Long id;

    /**
     * 猪群号
     */
    private String groupCode;
    private String batchNo;

    /**
     * 猪类
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;
    private String pigTypeName;

    private Integer sex;
    private Integer status;

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
     * 建立和关闭时间
     */
    private Date openAt;
    private Date closeAt;

    /**
     * 初次进舍
     */
    private Long initBarnId;
    private String initBarnName;

    /**
     * 当前舍
     */
    private Long currentBarnId;
    private String currentBarnName;

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
     * 猪只数
     */
    private Integer quantity;

    /**
     * 均龄
     */
    private Double avgDayAge;

    /**
     * 重量和均重
     */
    private Double weight;
    private Double avgWeight;

    /**
     * 单价和总价
     */
    private Long price;
    private Long amount;

    /**
     * 销售只数
     */
    private Integer saleQty;

    private Date updatedAt;
}
