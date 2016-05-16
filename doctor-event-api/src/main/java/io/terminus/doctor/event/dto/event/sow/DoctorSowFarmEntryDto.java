package io.terminus.doctor.event.dto.event.sow;

import io.terminus.doctor.event.model.DoctorPig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪进厂信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSowFarmEntryDto implements Serializable{

    private static final long serialVersionUID = 5276009871407574407L;

    private DoctorPig doctorPig;    // pig 基础数据信息

    private Long status;    //猪状态信息

    private Integer parity; //母猪胎次信息
}
