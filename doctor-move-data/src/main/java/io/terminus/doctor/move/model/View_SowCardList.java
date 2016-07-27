package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_SowCardList implements Serializable {
    private static final long serialVersionUID = 2952157119962433190L;
    private String OID;
    private String Status;
    private String FarmOID;
    private String PigID;
    private String PigFatherID;
    private String PigMotherID;
    private String Source;
    private Date BirthDate;
    private Double BirthWeight;
    private Date HerdEntryDate;
    private Integer HerdEntryDays;
    private String InitLocation;
    private String Location;
    private Integer InitialParity;
    private Integer CurrentParity;
    private Integer LnippleCount;
    private Integer RnippleCount;
    private String Breed;
    private String Genetic;
    private String Remark;
    private Double Weight;
    private String Sex;
    private String HerdRemovalDate;
    private String EventOID;
    private String Srm;
    private String PigAltID;
    private String FarmOIDText;
    private String LocationCaption;
    private String PigTypeOID;
    private String TypeName;
}
