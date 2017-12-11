package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/12/11.
 * email:xiaojiannan@terminus.io
 * 猪相关报表（组织维度：猪场，时间维度：日）
 */
@Data
public class DoctorPigDaily implements Serializable {
    private static final long serialVersionUID = -7317672970322690908L;
}
