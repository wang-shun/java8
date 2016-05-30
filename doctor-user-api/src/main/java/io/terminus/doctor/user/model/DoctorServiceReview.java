package io.terminus.doctor.user.model;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 用户服务审批表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-17
 */
@Data
public class DoctorServiceReview implements Serializable {
    private static final long serialVersionUID = 8803966503275820224L;

    private Long id;
    
    /**
     * 用户id
     */
    private Long userId;
    
    /**
     * 服务类型 1 猪场软件, 2 新融电商, 3 大数据, 4 生猪交易
     * @see io.terminus.doctor.user.model.DoctorServiceReview.Type
     */
    private Integer type;
    
    /**
     * 审核状态 0 未审核, 2 待审核(提交申请) 1 通过，-1 不通过, -2 冻结
     * @see io.terminus.doctor.user.model.DoctorServiceReview.Status
     */
    private Integer status;
    
    /**
     * 审批人id
     */
    private Long reviewerId;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 服务类型枚举
     */
    public enum Type {
        PIG_DOCTOR(1, "猪场软件"),
        PIGMALL(2, "新融电商"),
        NEVEREST(3, "大数据"),
        PIG_TRADE(4, "生猪交易");

        @Getter
        private final int value;
        @Getter
        private final String desc;

        Type(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Type from(int number) {
            for (Type type : Type.values()) {
                if (Objects.equal(type.value, number)) {
                    return type;
                }
            }
            throw new ServiceException("doctor.service.review.type.error");
        }
    }

    /**
     * 审批状态枚举
     */
    public enum Status {
        INIT(0, "用户未提交申请"),
        OK(1, "审核通过"),
        REVIEW(2, "用户已提交申请,正在审核中"),
        NOT_OK(-1, "审核不通过"),
        FROZEN(-2, "冻结");

        @Getter
        private int value;
        @Getter
        private String desc;

        Status(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Status from(int number) {
            return Lists.newArrayList(Status.values()).stream()
                    .filter(s -> Objects.equal(s.value, number))
                    .findFirst()
                    .orElseThrow(() -> {
                        throw new ServiceException("doctor.service.review.status.error");
                    });
        }
    }
}
