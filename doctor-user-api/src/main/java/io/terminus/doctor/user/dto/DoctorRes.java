package io.terminus.doctor.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/19
 */
@Data
public class DoctorRes implements Serializable {
    private static final long serialVersionUID = 6324852167974020767L;

    private Long id; //key id
    private String key;
    private String name;
    private String path;

    // 编译之后的正则路径
    @JsonIgnore
    private Pattern cpath;
    private String method;

    // 是否虚拟节点
    private Boolean virtual = false;

    // 是否继承父节点可访问性
    // 继承时, 此节点不会出现在管理列表中, 其下级子树均挂到上级节点
    // 顶层节点不考虑继承, 即时 inherit = true
    private Boolean inherit = false;

    private Integer level;
    private List<DoctorRes> children;
}
