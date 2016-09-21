package io.terminus.doctor.msg.listener;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xiao on 16/9/21.
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PigCreateEvent implements Serializable{

    private Map<String, Object> context;
}
