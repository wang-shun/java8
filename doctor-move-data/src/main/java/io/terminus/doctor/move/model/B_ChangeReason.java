package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class B_ChangeReason implements Serializable {
    private static final long serialVersionUID = -3752510813264351363L;

    private String OID;
    private String changeTypeOID;
    private String ReasonName;
    private String srm;
    private String Remark;
    private Integer Ord;
}
