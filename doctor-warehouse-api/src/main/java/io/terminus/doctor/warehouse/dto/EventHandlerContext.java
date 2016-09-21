package io.terminus.doctor.warehouse.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/9/21.
 * 仓库事件各 handler 传递数据的 dto
 */
@Data
public class EventHandlerContext implements Serializable{
    private static final long serialVersionUID = -4198568369651329197L;

    private Long materialInWareHouseId;
    private Long eventId;
    private Long wareHouseTypeId;
    private Double lotNumber;
    private Long consumeAvgId;
    private Long doctorFarmWareHouseTypeId;

    /**
     * 用于事件回滚
     */
    private DoctorWarehouseSnapshotDto snapshot = new DoctorWarehouseSnapshotDto();

}
