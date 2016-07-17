package io.terminus.doctor.basic.search.material;

import io.terminus.doctor.common.enums.WareHouseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc: 索引物料对象
 *      @see io.terminus.doctor.warehouse.model.DoctorMaterialInfo
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndexedMaterial implements Serializable {

    private static final long serialVersionUID = -533681543350876240L;

    private Long id;
    private String materialName; // 物料名称

    /**
     * 猪场
     */
    private Long farmId;
    private String farmName;

    /**
     * 物料类型
     * @see WareHouseType
     */
    private Integer type;
    private String typeName;

    /**
     * 价格
     */
    private Long price;
}
