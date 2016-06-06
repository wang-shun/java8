package io.terminus.doctor.user.dto;

import io.terminus.doctor.user.model.DoctorOrg;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Desc: 用户申请开通服务dto
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/18
 */
@Data
public class DoctorServiceApplyDto implements Serializable {
    private static final long serialVersionUID = -2057730252468519998L;

    /**
     * 开通服务类型
     * @see io.terminus.doctor.user.model.DoctorServiceReview.Type
     */
    @NotNull(message = "service.type.not.null")
    private Integer type;

    /**
     * 公司信息(如果类型是猪场软件, 此项必填)
     */
    private DoctorOrg org;
}
