package io.terminus.doctor.basic.dto;

import io.terminus.doctor.basic.model.DoctorFarmWareHouseType;
import io.terminus.doctor.basic.model.DoctorMaterialConsumeAvg;
import io.terminus.doctor.basic.model.DoctorWareHouseTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 仓库事件发生前的快照表 doctor_warehouse_snapshots 的 before_event 字段, 里面存的是json, 此dto便是此json对应的类
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorWarehouseSnapshotDto implements Serializable {
    private static final long serialVersionUID = -1895187392688297850L;

    private DoctorMaterialConsumeAvg materialConsumeAvg;
    private DoctorWareHouseTrack wareHouseTrack;
    private DoctorFarmWareHouseType farmWareHouseType;
}