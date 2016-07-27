package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class B_Customer implements Serializable {
    private static final long serialVersionUID = -7727334729858856418L;
    private String OID;
    private String CustomerName;
    private String LinkMan;
    private String Address;
    private String Telephone;
    private String MobilePhone;
    private String PostCode;
    private String EMail;
    private String Srm;
    private Integer Ord;
}
