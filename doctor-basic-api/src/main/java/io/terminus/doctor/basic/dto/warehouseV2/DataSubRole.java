package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName DataSubRole
 * @Description TODO
 * @Author Danny
 * @Date 2018/8/24 17:17
 */
@Data
public class DataSubRole  implements Serializable {

    private String userId;
    private String groupIds;
    private String orgIds;
    private String farmIds;

}
