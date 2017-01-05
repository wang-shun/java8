package io.terminus.doctor.event.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群查询信息
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchedGroup implements Serializable {
    private static final long serialVersionUID = 2679721994015824531L;
    /**
     * id
     */
    private Long id;

    /**
     * 猪群号
     */
    private String groupCode;

    /**
     * 猪类 枚举9种
     * @see io.terminus.doctor.common.enums.PigType
     */
    private Integer pigType;
    private String pigTypeName;

    /**
     * 性别 0母 1公 2混合
     * @see io.terminus.doctor.event.model.DoctorGroupTrack.Sex
     */
    private Integer sex;

    /**
     * 状态枚举: 1:已建群, -1:已关闭
     * @see io.terminus.doctor.event.model.DoctorGroup.Status
     */
    private Integer status;

    /**
     * 猪场
     */
    private Long farmId;
    private String farmName;

    /**
     * 建群和闭群时间
     */
    private Date openAt;
    private Date closeAt;

    /**
     * 当前所处的猪舍
     */
    private Long currentBarnId;
    private String currentBarnName;

    /**
     * 猪的数量
     */
    private Integer quantity;

    /**
     * 品均日龄
     */
    private Integer avgDayAge;

    /**
     * 总重量和品均重量
     */
    private Double weight;
    private Double avgWeight;

    /**
     * 单价和总价
     */
    private Long price;
    private Long amount;

    /**
     * 销售数量
     */
    private Integer saleQty;
}
