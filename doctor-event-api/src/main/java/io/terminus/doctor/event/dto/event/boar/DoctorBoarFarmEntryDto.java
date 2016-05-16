package io.terminus.doctor.event.dto.event.boar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-16
 * Email:yaoqj@terminus.io
 * Descirbe: 公猪进厂事件信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBoarFarmEntryDto implements Serializable{

    private static final long serialVersionUID = 3887498880962618841L;

    private Long pigId;

    private String pigCode;

    private Date birthDay;

    private Date inFarmDate;

    private Integer boarType;

    private Integer pigSource;

    private Long barnId;

    private String barnName;

    private Long breedId;

    private String breedName;

    private Long breedTypeId;

    private String breedTypeName;

    private String fatherCode;

    private String motherCode;

    private String remark;
}
