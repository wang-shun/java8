package io.terminus.doctor.move.model;

import lombok.Data;

import javax.annotation.sql.DataSourceDefinitions;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class View_EventListGain implements Serializable{
    private static final long serialVersionUID = 5363694163735495420L;
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
    private String EOID;
    private Date EventDate;
    private String EventName;
    private String EventDetail;
    private String RelMainOID;
    private String ERemark;
    private String EventType;
    private String EventEName;
    private String EventLocation;
    private Double EWeight;
    private String ChgType;
    private String ChgReason;
    private Double Price;
    private Double SumJe;
    private String Customer;
    private String OutDestination;
    private String Disease;
    private String Treament;
    private String Operator;
    private String AutoGenerate;
    private String RelEventOID;
    private String MoveCategory;
    private String ESex;
    private String EBreed;
    private String Source;
    private String SourceGainID;
    private Integer NumberOfPigs;
    private Integer NumberOfMale;
    private Integer NumberOfFemale;
    private Integer AverageAge;
    private String LitterID;
    private Date BirthDate;
    private String Staff;
    private String SowPigOID;
    private Integer SowCurrentParity;
    private String FarmOIDText;
    private String LocationCaption;
    private String EventTypeName;
    private String CurrentLocationCaption;
    private String PigTypeOID;
    private String TypeName;
    private String MoveCategoryText;
}
