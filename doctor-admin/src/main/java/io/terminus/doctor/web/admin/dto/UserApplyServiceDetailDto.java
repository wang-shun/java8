package io.terminus.doctor.web.admin.dto;

import io.terminus.doctor.user.model.DoctorOrg;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

public class UserApplyServiceDetailDto implements Serializable{
    private static final long serialVersionUID = 3590493478487803972L;

    @Setter @Getter
    private Long userId;
    @Setter @Getter
    private DoctorOrg org;

    /**
     * 猪场名称
     */
    @Setter @Getter
    private List<String> farms;
}
