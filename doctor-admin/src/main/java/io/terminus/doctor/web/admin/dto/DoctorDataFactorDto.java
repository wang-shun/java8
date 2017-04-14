package io.terminus.doctor.web.admin.dto;

import io.terminus.doctor.event.model.DoctorDataFactor;
import lombok.Data;

import java.util.List;
/**
 * Desc: 信用模型因子dto
 * Mail: hehaiyang@terminus.io
 * Date: 2017/4/14
 */
@Data
public class DoctorDataFactorDto {

    List<DoctorDataFactor> datas;
}
