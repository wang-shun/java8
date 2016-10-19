package io.terminus.doctor.move.service;

import com.google.common.collect.Lists;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.move.dto.DoctorImportSheet;
import io.terminus.doctor.move.util.ImportExcelUtils;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016/10/19
 */
@Slf4j
@Service
public class DoctorImportDataService {

    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Autowired
    private DoctorMoveBasicService doctorMoveBasicService;

    /**
     * 根据shit导入所有的猪场数据
     */
    public void importAll(DoctorImportSheet shit) {
        DoctorFarm farm = new DoctorFarm();
        Map<String, Long> userMap = doctorMoveBasicService.getSubMap(farm.getOrgId());
    }

    public DoctorFarm importOrgFarmUser(Sheet shit) {
        return new DoctorFarm();
    }

    /**
     * 导入猪舍
     */
    public void importBarn(DoctorFarm farm, Map<String, Long> userMap, Sheet shit) {
        List<DoctorBarn> barns = Lists.newArrayList();
        shit.forEach(row -> {
            //第一行是表头，跳过
            if (row.getRowNum() > 0) {
                DoctorBarn barn = new DoctorBarn();
                barn.setName(ImportExcelUtils.getString(row, 0));
                barn.setOrgId(farm.getOrgId());
                barn.setOrgName(farm.getOrgName());
                barn.setFarmId(farm.getId());
                barn.setFarmName(farm.getName());
                //barn.setPigType();
                barn.setCanOpenGroup(DoctorBarn.CanOpenGroup.YES.getValue());
                barn.setStatus(DoctorBarn.Status.USING.getValue());
                barn.setCapacity(1000);
                barn.setStaffName(ImportExcelUtils.getString(row, 3));
                barn.setStaffId(userMap.get(barn.getStaffName()));
                barn.setExtra(ImportExcelUtils.getString(row, 4));
                barns.add(barn);
            }
        });
        doctorBarnDao.creates(barns);
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
