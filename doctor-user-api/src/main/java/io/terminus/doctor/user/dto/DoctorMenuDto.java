package io.terminus.doctor.user.dto;

import lombok.Data;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc:
 * Mail: houly@terminus.io
 * author: Hou Luyao
 * Date: 15/11/27.
 */
@Data
@Builder
public class DoctorMenuDto implements Serializable{
    private static final long serialVersionUID = 5841835065219471069L;

    private Long id;

    /**
     * 名称
     */
    private String name;
    /**
     * 级别
     */
    private Integer level;
    /**
     * 访问路径
     */
    private String url;

    /**
     * logoClass
     */
    private String iconClass;

}
