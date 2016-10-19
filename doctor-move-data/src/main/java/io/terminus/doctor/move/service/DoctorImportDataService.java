package io.terminus.doctor.move.service;

import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.stereotype.Service;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/19
 */
@Slf4j
@Service
public class DoctorImportDataService {

    /**
     * 根据shit导入所有的猪场数据
     */
    public void importAll(DoctorImportSheet shit) {

    }

    public DoctorFarm importOrgFarmUser(Sheet shit) {
        return new DoctorFarm();
    }

    public void importBarn(DoctorFarm farm, Sheet shit) {
        
    }

    public void importBreed(DoctorFarm farm, Sheet shit) {

    }

    public void importBoar(DoctorFarm farm, Sheet shit) {

    }

    public void importGroup(DoctorFarm farm, Sheet shit) {

    }

    public void importSow(DoctorFarm farm, Sheet shit) {

    }

    public void importWarehouse(DoctorFarm farm, Sheet shit) {

    }

    public void importMedicine(DoctorFarm farm, Sheet shit) {

    }

    public void importVacc(DoctorFarm farm, Sheet shit) {

    }

    public void importMaterial(DoctorFarm farm, Sheet shit) {

    }

    public void importFeed(DoctorFarm farm, Sheet shit) {

    }

    public void importConsume(DoctorFarm farm, Sheet shit) {

    }
}
