package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/29.
 */

@Data
public class B_WareHouse implements Serializable {
    private static final long serialVersionUID = -6986335801180237688L;

    private String OID;
    private String FarmOID;
    private String WareHouseName;
    private String Manager;
    private String Address;
    private String WHType;
    private String Srm;
    private Integer Ord;
    private String IsDefault;
}
