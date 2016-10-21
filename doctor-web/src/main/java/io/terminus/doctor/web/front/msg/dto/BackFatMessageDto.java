package io.terminus.doctor.web.front.msg.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xjn on 16/10/19.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BackFatMessageDto implements Serializable{

    private static final long serialVersionUID = -2411623622533769492L;

    private Integer pigCount;

    private Integer ruleValueId;
}
