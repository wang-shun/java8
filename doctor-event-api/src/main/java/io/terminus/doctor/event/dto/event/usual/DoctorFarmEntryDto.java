package io.terminus.doctor.event.dto.event.usual;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaoqijun.
 * Date:2016-05-20
 * Email:yaoqj@terminus.io
 * Descirbe: 猪进厂事件dto
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorFarmEntryDto implements Serializable{

    private static final long serialVersionUID = -3221757737932679045L;

    private String pigCode;

    private Date birthday;

    private Date inFarmDate;

    private Long barnId;

    private String barnName;

    private Integer source;

    private Long breed; //品种

    private String breedName;   //品种名称

    private Long breedType;     //品系

    private String breedTypeName; //品系名称

    private Long fatherId;

    private Long motherId;

    private String mark;

    // boar
    private Long boarTypeId;

    private String boarTypeName;

    // sow
    private String earCode;

    private Integer parity;

    private Integer left;   //左乳头的数量

    private Integer right;  //右乳头数量

}
