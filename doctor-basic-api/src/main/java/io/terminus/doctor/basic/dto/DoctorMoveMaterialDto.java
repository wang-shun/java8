package io.terminus.doctor.basic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/6.
 * 调拨信息封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorMoveMaterialDto implements Serializable{
    private static final long serialVersionUID = -4014610135638614793L;

    /**
     * 调出信息
     */
    private DoctorMaterialConsumeProviderDto diaochuDto;

    /**
     * 调入信息
     */
    private DoctorMaterialConsumeProviderDto diaoruDto;
}
