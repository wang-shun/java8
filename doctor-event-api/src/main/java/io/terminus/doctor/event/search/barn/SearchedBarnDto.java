package io.terminus.doctor.event.search.barn;

import io.terminus.common.model.Paging;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 搜索猪舍相关封装
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/29
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchedBarnDto implements Serializable {
    private static final long serialVersionUID = 8736309253435207762L;

    /**
     * 搜索且分页后的猪舍数据
     */
    private Paging<SearchedBarn> barns;

    /**
     * 聚合后的猪群类型
     * @see io.terminus.doctor.common.enums.PigType
     */
    private List<PigType> aggPigTypes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PigType implements Serializable{
        private static final long serialVersionUID = 8317574572260219323L;

        private int key;
        private String name;
        private Long count;  //此类型的数量
    }

    public static PigType createPigType(io.terminus.doctor.common.enums.PigType pigType, Long count) {
        if (pigType != null) {
            return PigType.builder()
                    .key(pigType.getValue())
                    .name(pigType.getDesc())
                    .count(count)
                    .build();
        }
        return null;
    }
}
