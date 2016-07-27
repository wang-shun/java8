package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_GainCardList implements Serializable{
    private static final long serialVersionUID = 5256246992598006428L;
    private String OID;
    private Date GainOpenDate;
    private String Status;
    private String FarmOID;
    private String PigID;
    private String InitLocation;
    private String Sex;
    private String Breed;
    private String Genetic;
    private String Remark;
    private Integer MaximumMarketingAge;
    private String Location;
    private String GainCloseDate;
    private String EventOID;
    private String ProductionStage;
    private Double Weight;
    private String Srm;
    private String MngByPig;
    private String FarmOIDText;
    private String LocationCaption;
    private String LocationTypeOID;
    private String LocationTypeName;
    private String StaffName;
    private String PigTypeOID;
    private String TypeName;
}
