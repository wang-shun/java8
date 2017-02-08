package io.terminus.doctor.web.front.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/2/7.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorWareHouseEventData implements Serializable {
    private static final long serialVersionUID = 8108904422957185046L;

    private String wareHouseName;
    private String materialName;
    private Date eventTime;
    private Long unitPrice;
    private String unitName;
    private Double amount;
    private String providerFactoryName;
}
