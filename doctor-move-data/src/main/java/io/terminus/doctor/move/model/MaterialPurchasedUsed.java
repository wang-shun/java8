package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/8/2.
 */
@Data
public class MaterialPurchasedUsed implements Serializable {
    private static final long serialVersionUID = -8753587026014061918L;

    private String EventType;
    private Date EventDate;
    private String Zdr;
    private String WareHouse;
    private Double Count;
    private String Staff;
    private Integer UsedDays;
    private String materialName;
    private String UnitGroupText;
    private String UnitName;
    private String barnOId;
    private String Barn;
    private String materialOID;
}
