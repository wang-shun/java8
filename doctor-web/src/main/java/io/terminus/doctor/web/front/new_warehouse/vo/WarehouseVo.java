package io.terminus.doctor.web.front.new_warehouse.vo;

import lombok.Data;

import java.util.Date;

/**
 * Created by sunbo@terminus.io on 2017/8/22.
 */
@Data
public class WarehouseVo {

    private Long id;

    private String name;

    private Integer type;

    private String managerName;

    private Long managerId;

    private Date lastApplyDate;
}
