package io.terminus.doctor.warehouse.search.material;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc: 搜索物料对象
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchedMaterial implements Serializable {
    private static final long serialVersionUID = 3072630215078013538L;

    private Long id;
    private String materialName; // 物料名称

    /**
     * 猪场
     */
    private Long farmId;
    private String farmName;

    /**
     * 物料类型
     * @see io.terminus.doctor.warehouse.enums.WareHouseType
     */
    private Integer type;
    private String typeName;

    /**
     * 价格
     */
    private Long price;
}
