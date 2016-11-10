package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * Created by xjn on 16/11/9.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListenedBarnEvent implements Serializable{
    private static final long serialVersionUID = 3414980614442974670L;
    private Long barnId;
}
