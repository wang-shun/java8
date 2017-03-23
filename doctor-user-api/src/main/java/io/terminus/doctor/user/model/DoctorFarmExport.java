package io.terminus.doctor.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by xjn on 17/3/23.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorFarmExport implements Serializable {
    private static final long serialVersionUID = -7317297983446734024L;

    private Long id;

    private Long farmId;
    /**
     * 导入猪场名
     */
    private String farmName;
    /**
     * 导入文件地址
     */
    private String url;
    private Date createdAt;
    private Date updatedAt;
}
