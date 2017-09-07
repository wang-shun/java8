package io.terminus.doctor.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/9/6.
 * 猪场导入错误记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFarmMoveError implements Serializable{
    private static final long serialVersionUID = 3747017703875433833L;

    private Long id;
    private String farmName;
    private String code;
    private String outId;
    private String eventName;
    private Date eventAt;
    private String eventOutId;
    private String error;
    private Date createdAt;
    private Date updatedAt;
}
