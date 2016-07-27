package io.terminus.doctor.move.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/27.
 */
@Data
public class RoleTemplate implements Serializable {
    private String OID;
    private String RoleName;
    private String UpOID;
    private String RoleOf;
    private String remark;
}
