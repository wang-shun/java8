package io.terminus.doctor.move.dto;

import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Map;

/**
 * Created by xjn on 17/8/25.
 * 迁移基础数据
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorImportBasicData {

    private DoctorFarm doctorFarm;
    private Map<String, Long> userMap;
    private Map<String, DoctorBarn> barnMap;
    private Map<String, Long> breedMap;
}
