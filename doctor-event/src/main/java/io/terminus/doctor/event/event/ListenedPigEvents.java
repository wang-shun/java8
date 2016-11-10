package io.terminus.doctor.event.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.List;

/**
 * Created by xjn on 16/11/9.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListenedPigEvents implements Serializable{
    private static final long serialVersionUID = 3117202673865093413L;
    private List<ListenedPigEvent> list;
}
