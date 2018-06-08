package io.terminus.doctor.event.service;

import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DoctorDimensionCriteria;
import io.terminus.doctor.event.model.DoctorReportBoar;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import io.terminus.doctor.event.model.DoctorReportFatten;
import io.terminus.doctor.event.model.DoctorReportMaterial;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.model.DoctorReportNursery;
import io.terminus.doctor.event.model.DoctorReportReserve;
import io.terminus.doctor.event.model.DoctorReportSow;

import java.util.List;

/**
 * @author xjn
 * email xiaojiannan@terminus.io
 * @date 18/5/14
 */
public interface DoctorDailyReportV2ReadService {

    /**
     * 母猪区报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportSow>> sowReport(DoctorDimensionCriteria dimensionCriteria);

    /**
     * 公猪区报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportBoar>> boarReport(DoctorDimensionCriteria dimensionCriteria);

    /**
     * 产房区报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportDeliver>> deliverReport(DoctorDimensionCriteria dimensionCriteria);

    /**
     * 效率指标报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportEfficiency>> efficiencyReport(DoctorDimensionCriteria dimensionCriteria);

    /**
     * 育肥区报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportFatten>> fattenReport(DoctorDimensionCriteria dimensionCriteria);

    /**
     * 物料指标报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportMaterial>> materialReport(DoctorDimensionCriteria dimensionCriteria);

    /**
     * 配怀区报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportMating>> matingReport(DoctorDimensionCriteria dimensionCriteria);

    /**
     * 保育区报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportNursery>> nurseryReport(DoctorDimensionCriteria dimensionCriteria);

    /**
     * 后备区报表
     * @param dimensionCriteria 查询条件
     * @return 报表数据
     */
    Response<List<DoctorReportReserve>> reserveReport(DoctorDimensionCriteria dimensionCriteria);
}
