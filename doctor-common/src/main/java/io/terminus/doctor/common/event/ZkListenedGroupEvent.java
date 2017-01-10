package io.terminus.doctor.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 猪群事件zk携带信息
 * xjn
 * Date: 2017/1/10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZkListenedGroupEvent implements Serializable{
    private static final long serialVersionUID = 4293105794097951698L;
    private Long orgId;

    private Long farmId;

    private List<ZkGroupPublishDto> groups;
}
