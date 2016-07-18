package io.terminus.doctor.web.core.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by chenzenghui on 16/7/18.
 */

@Data
public class FarmStaff implements Serializable{
    private static final long serialVersionUID = -691125151747762849L;

    private Long userId;
    private Long staffId;
    private Long farmId;
    private String realName;
}
