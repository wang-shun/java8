package io.terminus.doctor.event.search.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪群(索引对象)
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexedGroup implements Serializable {
    private static final long serialVersionUID = 1121581546502813499L;

    private Long id;
    private String pigCode;

    private Long orgId;
    private String orgName;

    private Long farmId;
    private String farmName;

    private Integer pigType;
    private String pigTypeName;

    private Long pigFatherId;
    private String pigFatherCode;

    private Long pigMotherId;
    private String pigMotherCode;

    private Integer source;

    private Date birthDate;
    private Double birthWeight;

    private Date inFarmDate;
    private Integer inFarmDayAge;

    private Long initBarnId;
    private String initBarnName;

    private Long breedId;
    private String breedName;

    private Long geneticId;
    private String geneticName;

    private Integer status;
    private String statusName;

    private Long currentBarnId;
    private String currentBarnName;

    private Double weight;

    private Date outFarmDate;

    private Integer currentParity;

    private Date updatedAt;
}
