package io.terminus.doctor.move.dto;

import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.PrimaryUser;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.util.Map;

/**
 * Created by xjn on 17/8/25.
 * Excel导入基础数据
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorImportBasicData {

    /**
     * 导入猪场
     */
    private DoctorFarm doctorFarm;

    /**
     * 用户map,key:用户名、value:用户id
     */
    private Map<String, Long> userMap;

    /**
     * 猪舍map,key:猪舍名,value:猪舍
     */
    private Map<String, DoctorBarn> barnMap;

    /**
     * 品种map,key:品种名,value:品种id
     */
    private Map<String, Long> breedMap;

    /**
     * 历史胎次默认妊娠舍
     */
    private DoctorBarn defaultPregBarn;

    /**
     * 历史胎次默认产房
     */
    private DoctorBarn defaultFarrowBarn;

    /**
     * 默认操作人(猪场主账户)
     */
    private PrimaryUser defaultUser;

}
