package io.terminus.doctor.web.front.msg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 角色与模板绑定的dto
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/7
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MsgRoleDto implements Serializable {
    private static final long serialVersionUID = -856579216605336228L;

    /**
     * 规则id
     */
    private Long ruleId;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 规则值, json值
     */
    private String ruleValue;

    /**
     * 是否使用默认配置, 0:不使用, 1:使用
     */
    private Integer useDefault;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;
}
