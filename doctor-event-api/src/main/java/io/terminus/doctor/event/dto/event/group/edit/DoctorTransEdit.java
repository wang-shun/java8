package io.terminus.doctor.event.dto.event.group.edit;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/6/30
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorTransEdit extends BaseGroupEdit implements Serializable {
    private static final long serialVersionUID = 2320770149889330405L;

    /**
     * 品种id
     */
    private Long breedId;

    private String breedName;

    /**
     * 总活体重(kg)
     */
    private Double weight;
}
