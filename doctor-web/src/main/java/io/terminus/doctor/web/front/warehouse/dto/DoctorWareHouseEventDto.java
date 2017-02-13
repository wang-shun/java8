package io.terminus.doctor.web.front.warehouse.dto;

import io.terminus.doctor.basic.model.DoctorMaterialConsumeProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Created by xjn on 17/2/13.
 * 仓库事件信息封装,带有事件能否回滚标志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWareHouseEventDto extends DoctorMaterialConsumeProvider implements Serializable {
    private static final long serialVersionUID = 5791257249353739674L;

    private Boolean isRollback;
}
