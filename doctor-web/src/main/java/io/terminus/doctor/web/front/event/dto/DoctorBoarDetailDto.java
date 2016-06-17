package io.terminus.doctor.web.front.event.dto;

import io.terminus.doctor.event.model.DoctorPigEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-06-16
 * Email:yaoqj@terminus.io
 * Descirbe: 公猪详情界面信息
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorBoarDetailDto implements Serializable{

    private static final long serialVersionUID = -4732947162931613533L;

    private String pigBoarCode;  // 母猪Code

    private String breedName; // 品种名称

    private String barnCode; // barn Code

    private Integer pigStatus; // 猪状态

    private String field123456; // 字段 123456 TODO

    private List<DoctorPigEvent> doctorPigEvents;

}
