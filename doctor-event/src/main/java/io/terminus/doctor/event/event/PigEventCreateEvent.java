package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by yaoqijun.
 * Date:2016-05-24
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PigEventCreateEvent implements Serializable{

    private static final long serialVersionUID = -7364318853701532344L;

    private Map<String, Object> context;

}
