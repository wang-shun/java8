package io.terminus.doctor.event.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by xjn on 17/10/31.
 */
@Data
public class IotBarnInfo implements Serializable{
    private static final long serialVersionUID = 160496028210855761L;

    /**
     * 猪舍id
     */
    private Long barnId;

    /**
     * 猪舍名
     */
    private String barnName;

    /**
     * 关联员id
     */
    private Long staffId;

    /**
     * 关联员名称
     */
    private String staffName;

    /**
     * 当前猪数量
     */
    private Integer currentPigs;

    /**
     * 猪舍容量
     */
    private Integer capacity;

    /**
     * 各种状态猪数量（包含日龄：数量） 状态名：数量
     */
    private Map<String, Integer> statusPigs;
}
