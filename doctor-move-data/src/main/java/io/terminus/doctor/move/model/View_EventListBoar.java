package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_EventListBoar implements Serializable {
    private static final long serialVersionUID = 5918176765258406604L;
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
    private String BoarType;
    private String Breed;
    private String Genetic;
    private String Remark;
    private Double Weight;
    private String HerdRemovalDate;
    private String EventOID;
    private String Srm;
    private String EOID;
    private Date EventDate;
    private String EventName;
    private String EventDetail;
    private String RelMainOID;
    private String ERemark;
    private String EventType;
    private String EventEName;
    private String EventLocation;
    private Double Price;
    private Double SumJe;
    private String Customer;
    private String Disease;
    private String Treament;
    private Integer Dilution;
    private Double DilutionWeight;
    private Double Density;
    private Double Vitality;
    private Double PHValue;
    private Integer Score;
    private Double AbnormalRate;
    private Integer ScoreHl;
    private Integer ScoreMd;
    private Integer ScoreXt;
    private Integer ScoreSl;
    private Double EWeight;
    private Integer ChgCount;
    private String ChgType;
    private String ChgReason;
    private String OutDestination;
    private String Operator;
    private String AutoGenerate;
    private String RelEventOID;
    private String FarmOIDText;
    private String LocationCaption;
    private String CurrentLocationCaption;
    private String PigTypeOID;
    private String TypeName;
}
