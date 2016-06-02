package io.terminus.doctor.event.dto;

import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-06-01
 * Email:yaoqj@terminus.io
 * Descirbe: 获取对应的母猪详细信息内容
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPigInfoDetailDto implements Serializable {

    private static final long serialVersionUID = -8680247209222722898L;

    private DoctorPig doctorPig;

    private DoctorPigTrack doctorPigTrack;

    private List<DoctorPigEvent> doctorPigEvents;
}
