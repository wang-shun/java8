package io.terminus.doctor.web.front.msg.dto;

import io.terminus.doctor.event.model.DoctorMessageRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc: 猪场对应的规则Dto
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/18
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MsgRuleDto implements Serializable {
    private static final long serialVersionUID = 8506217474382823715L;

    /**
     * 猪场规则详情
     */
    private DoctorMessageRule doctorMessageRule;

    /**
     * 角色Id
     */
    private Long roleId;

    /**
     * 是否被当前角色选中
     */
    private boolean flag;
}
