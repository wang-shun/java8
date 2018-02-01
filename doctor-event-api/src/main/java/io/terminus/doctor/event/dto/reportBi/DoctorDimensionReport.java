package io.terminus.doctor.event.dto.reportBi;

import io.terminus.doctor.event.model.DoctorReportBoar;
import io.terminus.doctor.event.model.DoctorReportDeliver;
import io.terminus.doctor.event.model.DoctorReportEfficiency;
import io.terminus.doctor.event.model.DoctorReportFatten;
import io.terminus.doctor.event.model.DoctorReportMaterial;
import io.terminus.doctor.event.model.DoctorReportMating;
import io.terminus.doctor.event.model.DoctorReportNursery;
import io.terminus.doctor.event.model.DoctorReportReserve;
import io.terminus.doctor.event.model.DoctorReportSow;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 18/1/18.
 * email:xiaojiannan@terminus.io
 */
@Data
public class DoctorDimensionReport implements Serializable{
    private static final long serialVersionUID = -8157217122551762667L;
    private DoctorReportReserve reportReserve;

    private DoctorReportSow reportSow;

    private DoctorReportMating reportMating;

    private DoctorReportDeliver reportDeliver;

    private DoctorReportNursery reportNursery;

    private DoctorReportFatten reportFatten;

    private DoctorReportBoar reportBoar;

    private DoctorReportMaterial reportMaterial;

    private DoctorReportEfficiency reportEfficiency;
}
