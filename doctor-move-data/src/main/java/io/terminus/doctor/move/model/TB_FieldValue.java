package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/27
 */
@Data
public class TB_FieldValue implements Serializable {
    private static final long serialVersionUID = -8364385462797827002L;

    private String OID;

    private String TypeId;

    private String FieldText;

    private String Remark;

    private String Srm;
}
