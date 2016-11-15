package io.terminus.doctor.common.event;

import io.terminus.common.utils.JsonMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Desc: 缓存事件
 * author: 陈增辉
 * Date: 2016年06月14日
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheEvent implements Serializable{
    private static final long serialVersionUID = 954076466315415543L;

    private Long eventType;
    private Long data;

    public static CacheEvent make(Long eventType, Long data){
        return new CacheEvent(eventType, data);
    }

    public static byte[] toBytes(CacheEvent m) {
        return JsonMapper.JSON_NON_EMPTY_MAPPER.toJson(m).getBytes();
    }

    public static CacheEvent from(byte[] value) {
        return from(new String(value));
    }

    public static CacheEvent from(String value) {
        if(value.contains("eventType") && value.contains("data")){
            return JsonMapper.JSON_NON_EMPTY_MAPPER.fromJson(value, CacheEvent.class);
        }else{
            return null;
        }
    }
}
