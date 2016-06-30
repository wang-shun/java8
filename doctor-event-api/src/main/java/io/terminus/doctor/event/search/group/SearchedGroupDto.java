package io.terminus.doctor.event.search.group;

import io.terminus.common.model.Paging;
import io.terminus.doctor.event.search.barn.SearchedBarnDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 搜索猪群相关封装
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchedGroupDto implements Serializable {
    private static final long serialVersionUID = -787770456507152824L;

    /**
     * 分页后的猪群数据
     */
    private Paging<SearchedGroup> groups;

    /**
     * 聚合后的猪群类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private List<SearchedBarnDto.PigType> aggPigTypes;
}
