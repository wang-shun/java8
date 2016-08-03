package io.terminus.doctor.msg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 子账号消息传递对象
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/2
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubUser implements Serializable {

    private static final long serialVersionUID = 8375046860013097055L;
    /**
     * 账户id
     */
    private Long userId;

    /**
     * 主账户id
     */
    private Long parentUserId;

    /**
     * 角色id
     */
    private Long roleId;

    /**
     * 有权限的猪场
     */
    private List<Long> farmIds;

}
