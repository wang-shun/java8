package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;

import java.io.Serializable;

/**
 * 新增，编辑时传入提示信息
 */
@Data
public class InventoryDto implements Serializable{

    private static final long serialVersionUID = -6212793767515747713L;

    private Long id;

    private String desc;

}
