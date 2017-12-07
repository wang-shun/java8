package io.terminus.doctor.basic.manager;

import io.terminus.doctor.basic.dao.DoctorWarehouseMaterialApplyDao;
import io.terminus.doctor.basic.dto.warehouseV2.WarehouseStockOutDto;
import io.terminus.doctor.basic.enums.WarehouseMaterialApplyType;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialApply;
import io.terminus.doctor.basic.model.warehouseV2.DoctorWarehouseMaterialHandle;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Created by sunbo@terminus.io on 2017/8/21.
 */
@Component
public class DoctorWarehouseMaterialApplyManager {


    @Autowired
    private DoctorWarehouseMaterialApplyDao doctorWarehouseMaterialApplyDao;


    @Transactional
    public void creates(List<DoctorWarehouseMaterialApply> materialApplies) {
        materialApplies.forEach(apply ->
                doctorWarehouseMaterialApplyDao.create(apply)
        );
    }

    @Transactional
    public void create(DoctorWarehouseMaterialApply materialApply) {
        creates(Collections.singletonList(materialApply));
    }

    //    @Transactional(propagation = Propagation.NESTED)
    public void apply(DoctorWarehouseMaterialHandle handle, WarehouseStockOutDto.WarehouseStockOutDetail outDetail) {
        DoctorWarehouseMaterialApply materialApply = new DoctorWarehouseMaterialApply();
        materialApply.setWarehouseId(handle.getWarehouseId());
        materialApply.setFarmId(handle.getFarmId());
        materialApply.setWarehouseName(handle.getWarehouseName());
        materialApply.setWarehouseType(handle.getWarehouseType());
        materialApply.setMaterialId(handle.getMaterialId());
        materialApply.setMaterialName(handle.getMaterialName());

        materialApply.setType(handle.getWarehouseType());
        materialApply.setUnit(handle.getUnit());
        materialApply.setQuantity(handle.getQuantity());
        materialApply.setUnitPrice(handle.getUnitPrice());
        materialApply.setApplyDate(handle.getHandleDate());
        materialApply.setApplyYear(handle.getHandleYear());
        materialApply.setApplyMonth(handle.getHandleMonth());
        materialApply.setMaterialHandleId(handle.getId());
        materialApply.setPigBarnId(outDetail.getApplyPigBarnId());
        materialApply.setPigBarnName(outDetail.getApplyPigBarnName());

        if (null != outDetail.getApplyPigGroupId()) { //猪群和母猪领用
            if (outDetail.getApplyPigGroupId() == -1) {
                materialApply.setPigGroupId(-1L);
                materialApply.setPigGroupName("母猪");
                materialApply.setApplyType(WarehouseMaterialApplyType.SOW.getValue());
            } else {
                materialApply.setPigGroupId(outDetail.getApplyPigGroupId());
                materialApply.setPigGroupName(outDetail.getApplyPigGroupName());
                materialApply.setApplyType(WarehouseMaterialApplyType.GROUP.getValue());
            }
            DoctorWarehouseMaterialApply barnApply = new DoctorWarehouseMaterialApply();
            BeanUtils.copyProperties(materialApply, barnApply);
            barnApply.setApplyType(WarehouseMaterialApplyType.BARN.getValue());
            barnApply.setPigGroupId(null);
            barnApply.setPigGroupName(null);
            doctorWarehouseMaterialApplyDao.create(barnApply);
        } else { //猪舍领用
            materialApply.setApplyType(WarehouseMaterialApplyType.BARN.getValue());
        }
        
        materialApply.setApplyStaffName(outDetail.getApplyStaffName());
        materialApply.setApplyStaffId(outDetail.getApplyStaffId());
        doctorWarehouseMaterialApplyDao.create(materialApply);
    }

}
