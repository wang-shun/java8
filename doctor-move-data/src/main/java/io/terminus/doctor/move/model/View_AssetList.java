package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/29.
 */
@Data
public class View_AssetList implements Serializable {
    private String OID;
    private String AssetName;
    private String MaterialName;
    private String UnitGroup;
    private String Srm;
    private String Remark;
    private String Ord;
    private String TypeOID;
    private String TypeText;
    private String UnitGroupText;
    private String UnitOID;
    private String UnitName;
    private String DefaultUnitName;
}
