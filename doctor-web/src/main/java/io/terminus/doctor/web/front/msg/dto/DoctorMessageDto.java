package io.terminus.doctor.web.front.msg.dto;

import io.terminus.common.model.Paging;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Desc: message dto
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/9/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorMessageDto implements Serializable {

    private static final long serialVersionUID = -3959615913809640832L;

    /**
     * 分页数据
     */
    private Paging<DoctorMessageWithUserDto> paging;

    /**
     * 跳转到list的url
     */
    private String listUrl;
}
