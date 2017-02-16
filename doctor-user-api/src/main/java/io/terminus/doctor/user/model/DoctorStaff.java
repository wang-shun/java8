package io.terminus.doctor.user.model;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import io.terminus.common.exception.ServiceException;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 猪场职员表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-05-19
 */
@Data
public class DoctorStaff implements Serializable {
    private static final long serialVersionUID = -403686963673350907L;

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
     * 状态 1:在职，-1:不在职
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 修改时间
     */
    private Date updatedAt;

    /**
     * 是否在职枚举
     */
    public enum Status {
        ABSENT(-1, "不在职"),
        PRESENT(1, "在职");

        private int value;
        private String desc;

        Status(int value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public static Status from(int number) {
            return Lists.newArrayList(Status.values()).stream()
                    .filter(s -> Objects.equal(s.value, number))
                    .findFirst()
                    .<ServiceException>orElseThrow(() -> {
                        throw new ServiceException("doctor.service.review.status.error");
                    });
        }

        public int value(){
            return this.value;
        }
        public String toString(){
            return this.desc;
        }
    }
}
