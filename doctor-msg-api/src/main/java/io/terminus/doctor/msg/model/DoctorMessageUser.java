package io.terminus.doctor.msg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;
import java.util.Date;

/**
 *用户关联消息
 * Created by xiao on 16/10/11.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DoctorMessageUser implements Serializable{

    private static final long serialVersionUID = -8805746250107654299L;
    /**
     * 自增主键
     */
    private Long id;

    /**
     * 猪场id
     */
    private Long farmId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 消息id
     */
    private Long messageId;

    /**
     * 消息对应的操作id: 猪id、猪群id、物料id
     */
    private Long businessId;

    /**
     * 消息规则模板id
     */
    private Long templateId;

    /**
     * 状态 1:未发送, 2:已发送, 3:已读,  -1:删除, -2:发送失败
     */
    private Integer status;

    /**
     * 发送时间
     */
    private Date sendedAt;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 更新时间
     */
    private Date updatedAt;

    /**
     * 失败原因
     */
    private String failedBy;

    public enum Status {
        NORMAL(1, "未发送"),
        SENDED(2, "已发送"),
        READED(3, "已读"),
        DELETE(-1, "删除"),
        FAILED(-2, "发送失败");

        @Getter
        private Integer value;

        @Getter
        private String describe;

        Status(Integer value, String describe) {
            this.value = value;
            this.describe = describe;
        }
    }
}
