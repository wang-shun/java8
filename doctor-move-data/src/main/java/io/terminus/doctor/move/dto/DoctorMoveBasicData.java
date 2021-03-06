package io.terminus.doctor.move.dto;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.model.DoctorBasicMaterial;
import io.terminus.doctor.basic.model.DoctorChangeReason;
import io.terminus.doctor.basic.model.DoctorCustomer;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.PrimaryUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Map;

/**
 * Created by xjn on 17/8/4.
 * 事件依赖的基础数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorMoveBasicData {

    private DoctorFarm doctorFarm;
    private Map<String, DoctorBarn> barnMap;
    private Map<Integer, Map<String, DoctorBasic>> basicMap;
    private Map<String, Long> subMap;
    private Map<String, DoctorChangeReason> changeReasonMap;
    private Map<String, DoctorCustomer> customerMap;
    private Map<String, DoctorBasicMaterial> vaccMap;
    private Map<String, DoctorPig> boarMap;
    private Map<String, DoctorGroup> groupMap;

    /**
     * 配怀舍为空时默认配怀舍
     */
    private DoctorBarn defaultPregBarn;

    /**
     * 产房为空时默认产房
     */
    private DoctorBarn defaultFarrowBarn;

    /**
     * 产房仔猪群为空时默认猪群
     */
    private DoctorGroup defaultFarrowGroup;

    /**
     * 默认操作人(猪场主账户)
     */
    private PrimaryUser defaultUser;
}
