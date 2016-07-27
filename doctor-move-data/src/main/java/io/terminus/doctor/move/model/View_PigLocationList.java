package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_PigLocationList implements Serializable{
    private static final long serialVersionUID = -2947199299070135625L;
    private String OID;
    private String PigTypeOID;
    private String FarmOID;
    private String Barn;
    private String Room;
    private String Pen;
    private String barnName;
    private Integer Ord;
    private String Remark;
    private String StaffName;
    private String CanOpenGroup;
    private String IsStopUse;
    private String PigLocation;
    private String FarmOIDText;
    private String TypeName;
    private String pigType;
    private String PigTypeName;
    private String sex;
    private Integer ColID;
    private String SexName;
    private String CanOpenGroupText;
    private String IsStopUseText;
}
