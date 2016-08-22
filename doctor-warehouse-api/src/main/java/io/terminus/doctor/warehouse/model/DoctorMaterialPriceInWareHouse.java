package io.terminus.doctor.warehouse.model;

import io.terminus.doctor.warehouse.dto.DoctorMaterialConsumeProviderDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.Date;

/**
 * 陈增辉
 * Desc: 仓库中各物料每次入库的剩余量Model类
 * Date: 2016-08-15
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMaterialPriceInWareHouse implements Serializable {

    private static final long serialVersionUID = 6333326277652074639L;

    private Long id;
    
    /**
     * 冗余仓库信息
     */
    private Long farmId;
    
    /**
     * 猪场姓名
     */
    private String farmName;
    
    /**
     * 仓库信息
     */
    private Long wareHouseId;
    
    /**
     * 仓库名称
     */
    private String wareHouseName;
    
    /**
     * 物料Id
     */
    private Long materialId;
    
    /**
     * 物料名称
     */
    private String materialName;
    
    /**
     * 仓库类型, 冗余
     */
    private Integer type;
    
    /**
     * 入库事件id
     */
    private Long providerId;
    
    /**
     * 本次入库单价，单位为“分”
     */
    private Long unitPrice;
    
    /**
     * 本次入库量的剩余量，比如本次入库100个，那么就是这100个的剩余量，减少到0时删除
     */
    private Double remainder;
    
    /**
     * 入库时间，冗余字段
     */
    private Date providerTime;
    
    /**
     * 扩展
     */
    private String extra;
    
    /**
     * 创建人id
     */
    private Long creatorId;
    
    /**
     * 创建人Id
     */
    private Long updatorId;
    
    private Date createdAt;
    
    private Date updatedAt;

    public static DoctorMaterialPriceInWareHouse buildFromDto(DoctorMaterialConsumeProviderDto dto, Long materialConsumeProviderId) {
        return DoctorMaterialPriceInWareHouse.builder()
                .farmId(dto.getFarmId()).farmName(dto.getFarmName())
                .wareHouseId(dto.getWareHouseId()).wareHouseName(dto.getWareHouseName())
                .materialId(dto.getMaterialTypeId()).materialName(dto.getMaterialName())
                .type(dto.getType())
                .providerId(materialConsumeProviderId).unitPrice(dto.getUnitPrice()).remainder(dto.getCount()).providerTime(DateTime.now().toDate())
                .creatorId(dto.getStaffId()).updatorId(dto.getStaffId())
                .build();
    }
}
