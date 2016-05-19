package io.terminus.doctor.event.dto.event.sow;

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
 * Descirbe: 母猪进厂信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorSowFarmEntryDto implements Serializable{

    private static final long serialVersionUID = 5276009871407574407L;

    private String pigCode;

    private Date inFarmDate;

    private Date birthday;

    private Long barnId;

    private String barnName;

    private Integer source;

    private String earCode;

    private Integer parity;

    private Integer left;   //左乳头的数量

    private Integer right;  //右乳头数量

    private Long breed; //品种

    private Long breedType;     //品系

    private Long fatherId;

    private Long motherId;

    private String mark;
}
