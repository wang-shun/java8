package io.terminus.doctor.web.front.event.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by xjn on 17/1/11.
 * 封装一个猪群的事件输入信息
 */
@Data
public class DoctorGroupEventInputDto implements Serializable{
    private static final long serialVersionUID = -1107871701408029944L;

    /**
     * 猪群id
     */
    private Long groupId;

    /**
     * 输入信息
     */
    private String inputJson;
}
