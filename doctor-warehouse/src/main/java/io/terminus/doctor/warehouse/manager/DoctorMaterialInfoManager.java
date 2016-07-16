package io.terminus.doctor.warehouse.manager;

import com.google.common.collect.ImmutableMap;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInWareHouseDao;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialInWareHouse;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
import org.springframework.stereotype.Component;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yaoqijun.
 * Date:2016-07-16
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Component
public class DoctorMaterialInfoManager {

    private final DoctorMaterialInfoDao doctorMaterialInfoDao;

    private final DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao;

    public DoctorMaterialInfoManager(
            DoctorMaterialInfoDao doctorMaterialInfoDao,
            DoctorMaterialInWareHouseDao doctorMaterialInWareHouseDao){
        this.doctorMaterialInfoDao = doctorMaterialInfoDao;
        this.doctorMaterialInWareHouseDao = doctorMaterialInWareHouseDao;
    }

    /**
     *  修改对应的物料信息内容
     * @param doctorMaterialInfo
     */
    public void updateMaterialInfo(DoctorMaterialInfo doctorMaterialInfo){

        // update materialInfo
        checkState(doctorMaterialInfoDao.update(doctorMaterialInfo),"update.materialInfo.fail");

        checkState(
                doctorMaterialInWareHouseDao.updateMaterialInfoByMaterialId(ImmutableMap.of(
                "materialId", doctorMaterialInfo.getId(),
                "materialName", doctorMaterialInfo.getMaterialName(),
                "unitGroupName", doctorMaterialInfo.getUnitGroupName(),
                "unitName", doctorMaterialInfo.getUnitName()
        )),"update.materialInfoInWarehouse.fail");
    }
}
