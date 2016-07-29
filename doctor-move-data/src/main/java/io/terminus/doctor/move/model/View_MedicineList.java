package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/29.
 */

@Data
public class View_MedicineList implements Serializable {
    private static final long serialVersionUID = 5265923701299463470L;
    private String OID;
    private String MedicineCode;
    private String MedicineName;
    private String MaterialName;
    private String UnitGroup;
    private Double Price;
    private String Origin;
    private String LotNumber;
    private String Remark;
    private String Srm;
    private Integer Ord;
    private String TypeOID;
    private Double DefaultDose;
    private String TypeText;
    private String UnitGroupText;
    private String UnitOID;
    private String UnitName;
    private String DefaultUnitName;
}
