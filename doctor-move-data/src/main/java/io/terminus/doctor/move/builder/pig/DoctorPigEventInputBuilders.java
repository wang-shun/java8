package io.terminus.doctor.move.builder.pig;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Created by xjn on 17/8/8.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorPigEventInputBuilders {
    Map<String, DoctorPigEventInputBuilder> pigEvenInputBuilderMap;
}
