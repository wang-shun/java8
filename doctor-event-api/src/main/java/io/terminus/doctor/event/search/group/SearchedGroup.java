package io.terminus.doctor.event.search.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群查询信息
 *      @see IndexedGroup
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
     * 猪群好
     */
    private String groupCode;
    private String batchNo;

    /**
     * 猪群类型
     */
    private Integer pigType;
    private String pigTypeName;

    /**
     * 性别和状态
     */
    private Integer sex;
    private Integer status;

    /**
     * 猪场
     */
    private Long farmId;
    private String farmName;

    /**
     * 建群和闭群事件
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
    private Double avgDayAge;

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
