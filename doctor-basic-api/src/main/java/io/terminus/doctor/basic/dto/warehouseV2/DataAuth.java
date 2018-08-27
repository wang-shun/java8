package io.terminus.doctor.basic.dto.warehouseV2;

import lombok.Data;

import java.util.List;

/**
 * @ClassName DataAuth
 * @Description TODO
 * @Author Danny
 * @Date 2018/8/24 17:22
 */
@Data
public class DataAuth {

    private List<String> userIds;
    private String userType;
    private List<DataSubRole> datas;

}
