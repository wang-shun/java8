package io.terminus.doctor.web.front.warehouse.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 17/3/2.
 * 仓库物料查询条件封装
 */
@Data
public class DoctorWareHouseMaterialCriteria implements Serializable {
    private static final long serialVersionUID = 1318638360563902156L;

    private Long farmId;

    private Long wareHouseId;

    private Long materialId;

    private String materialName;

    private Long materialType;

    private Long type;

    private Long barnId;

    private String barnName;

    private List<Long> groupId;

    private String startDate;

    private String endDate;

    private Integer pageNo;

    private Integer size;
}
