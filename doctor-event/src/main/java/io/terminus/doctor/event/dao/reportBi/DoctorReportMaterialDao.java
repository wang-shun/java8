package io.terminus.doctor.event.dao.reportBi;

import io.terminus.common.mysql.dao.MyBatisDao;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportMaterial;
import org.springframework.cache.support.AbstractCacheManager;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

/**
 * Desc:
 * Mail: [ your email ]
 * Date: 2018-01-11 16:16:32
 * Created by [ your name ]
 */
@Repository
public class DoctorReportMaterialDao extends MyBatisDao<DoctorReportMaterial> {
    public DoctorReportMaterial findByDimension(DoctorDimensionCriteria dimensionCriteria) {

        DoctorReportMaterial material = getSqlSession().selectOne(sqlId("findByDimension"), dimensionCriteria);
        if (null == material) {
            material = new DoctorReportMaterial();
            material.setOrzId(dimensionCriteria.getOrzId());
            material.setOrzType(dimensionCriteria.getOrzType());
            material.setDateType(dimensionCriteria.getDateType());

            material.setHoubeiFeedAmount(new BigDecimal(0));
            material.setHoubeiFeedQuantity(0);
            material.setHoubeiMaterialQuantity(0);
            material.setHoubeiMaterialAmount(new BigDecimal(0));
            material.setHoubeiVaccinationAmount(new BigDecimal(0));
            material.setHoubeiMedicineAmount(new BigDecimal(0));
            material.setHoubeiConsumeAmount(new BigDecimal(0));

            material.setBaoyuFeedAmount(new BigDecimal(0));
            material.setBaoyuFeedQuantity(0);
            material.setBaoyuMaterialAmount(new BigDecimal(0));
            material.setBaoyuMaterialQuantity(0);
            material.setBaoyuConsumeAmount(new BigDecimal(0));
            material.setBaoyuVaccinationAmount(new BigDecimal(0));
            material.setBaoyuMedicineAmount(new BigDecimal(0));

            material.setPeihuaiFeedAmount(new BigDecimal(0));
            material.setPeihuaiFeedQuantity(0);
            material.setPeihuaiMaterialAmount(new BigDecimal(0));
            material.setPeihuaiMaterialQuantity(0);
            material.setPeihuaiConsumeAmount(new BigDecimal(0));
            material.setPeihuaiMedicineAmount(new BigDecimal(0));
            material.setPeihuaiVaccinationAmount(new BigDecimal(0));

            material.setSowFeedAmount(new BigDecimal(0));
            material.setSowFeedQuantity(0);
            material.setSowMaterialAmount(new BigDecimal(0));
            material.setSowMaterialQuantity(0);
            material.setSowVaccinationAmount(new BigDecimal(0));
            material.setSowMedicineAmount(new BigDecimal(0));
            material.setSowConsumeAmount(new BigDecimal(0));

            material.setPigletFeedAmount(new BigDecimal(0));
            material.setPigletFeedQuantity(0);
            material.setPigletMaterialAmount(new BigDecimal(0));
            material.setPigletMaterialQuantity(0);
            material.setPigletVaccinationAmount(new BigDecimal(0));
            material.setPigletConsumeAmount(new BigDecimal(0));
            material.setPigletMedicineAmount(new BigDecimal(0));

            material.setYufeiFeedAmount(new BigDecimal(0));
            material.setYufeiFeedQuantity(0);
            material.setYufeiMaterialAmount(new BigDecimal(0));
            material.setYufeiMaterialQuantity(0);
            material.setYufeiConsumeAmount(new BigDecimal(0));
            material.setYufeiVaccinationAmount(new BigDecimal(0));
            material.setYufeiMedicineAmount(new BigDecimal(0));

            material.setBoarFeedAmount(new BigDecimal(0));
            material.setBoarFeedQuantity(0);
            material.setBoarMaterialAmount(new BigDecimal(0));
            material.setBoarMaterialQuantity(0);
            material.setBoarVaccinationAmount(new BigDecimal(0));
            material.setBoarConsumeAmount(new BigDecimal(0));
            material.setBoarMedicineAmount(new BigDecimal(0));
        }
        return material;
    }

    public void delete() {
        this.sqlSession.delete(sqlId("deleteAll"));
    }

    public void delete(DoctorDimensionCriteria criteria) {
        this.sqlSession.delete(sqlId("deleteBy"), criteria);
    }
}
