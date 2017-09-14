package io.terminus.doctor.move.builder.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by xjn on 17/8/8.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DoctorGroupEventInputBuilders {
    private Map<String, DoctorGroupEventInputBuilder> groupEventInputBuilderMap;
}
