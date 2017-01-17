package io.terminus.doctor.event.dto.search;

import io.terminus.common.model.Paging;
import io.terminus.doctor.event.enums.PigStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 搜索的猪封装类
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/6/30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchedPigDto implements Serializable {
    private static final long serialVersionUID = -4244496382050350148L;

    public Paging<SearchedPig> pigs;

    public List<SowStatus> sowStatuses;

    /**
     * 母猪状态类
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SowStatus implements Serializable{
        private static final long serialVersionUID = 7792826158693919446L;

        private Integer key;
        private String name;
        private String desc;
    }

    public static SowStatus createStatus(PigStatus pigStatus) {
        if (pigStatus != null) {
            return SowStatus.builder()
                    .key(pigStatus.getKey())
                    .name(pigStatus.getName())
                    .desc(pigStatus.getDesc())
                    .build();
        }
        return null;
    }
}
