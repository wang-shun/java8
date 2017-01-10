package io.terminus.doctor.basic.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MaterialCountAmount implements Serializable{
    private static final long serialVersionUID = 8845899857608972994L;

    /**
     * 数量
     */
    private Long count;
    /**
     * 总计金额
     */
    private Double amount;

    private Long materialId;
    private String materialName;
    private String unitName;
}
