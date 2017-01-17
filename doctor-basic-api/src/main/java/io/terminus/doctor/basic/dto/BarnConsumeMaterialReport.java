package io.terminus.doctor.basic.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/11/18.
 * 猪舍领用物资的统计报表
 */
@Data
public class BarnConsumeMaterialReport implements Serializable {
    private static final long serialVersionUID = -3546759732300641223L;

    private Long barnId;
    private String barnName;

    private Long materialId;
    private String materialName;

    private Double count;
}
