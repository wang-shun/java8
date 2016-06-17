package io.terminus.doctor.common.event;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.enums.DataEventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.isNull;

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
public class DataEvent {

    private Integer eventType;     // 事件类型

    /**
     * @see DataEventType
     */
    private String content;     //json 数据类型

    public static <T> DataEvent make(Integer eventType, T content){
        return DataEvent.builder()
                .eventType(eventType)
                .content(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(content))
                .build();
    }

    public static byte[] toBytes(DataEvent dataEvent){
        return JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(dataEvent).getBytes();
    }

    public static <T> byte[] toBytes(Integer eventType, T context){
        return toBytes(DataEvent.make(eventType, context));
    }

    public static DataEvent fromBytes(byte[] bytes){
        checkState(!isNull(bytes), "dataEvent.fromBytes.empty");
        return JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(new String(bytes),DataEvent.class);
    }

    public static <T> T analyseContent(DataEvent dataEvent, Class<T> clazz){
        checkState(!isNull(dataEvent.getContent()),"dataEvent.content.empty");
        return JsonMapper.JSON_NON_DEFAULT_MAPPER.fromJson(dataEvent.getContent(), clazz);
    }

    public static <T> T analyseContextByBytes(byte[] bytes, Class<T> clazz){
        return analyseContent(fromBytes(bytes),clazz);
    }
}
