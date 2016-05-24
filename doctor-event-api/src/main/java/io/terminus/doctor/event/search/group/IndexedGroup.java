package io.terminus.doctor.event.search.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群(索引对象)
 *      @see io.terminus.doctor.event.model.DoctorGroup
 *      @see io.terminus.doctor.event.model.DoctorGroupTrack
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexedGroup implements Serializable {
    private static final long serialVersionUID = 8463331809080229435L;

    private Long id;

    private String groupCode;
    private String batchNo;

    private Integer pigType;
    private String pigTypeName;

    private Integer sex;
    private Integer status;

    private Long orgId;
    private String orgName;

    private Long farmId;
    private String farmName;

    private Date openAt;
    private Date closeAt;

    private Long initBarnId;
    private String initBarnName;

    private Long currentBarnId;
    private String currentBarnName;

    private Long breedId;
    private String breedName;

    private Long geneticId;
    private String geneticName;

    private Integer quantity;
    private Double avgDayAge;

    private Double weight;
    private Double avgWeight;

    private Long price;
    private Long amount;

    private Integer saleQty;

    private Date updatedAt;
}
