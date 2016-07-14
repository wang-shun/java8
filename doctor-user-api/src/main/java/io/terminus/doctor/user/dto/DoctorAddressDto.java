package io.terminus.doctor.user.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 前台地址三级联动dto
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/7/14
 */
@Data
public class DoctorAddressDto implements Serializable {
    private static final long serialVersionUID = 5128894275300359872L;

    private Integer value;   //地址id

    private String label;    //地址名

    private List<DoctorAddressDto> children;  //子节点
}
