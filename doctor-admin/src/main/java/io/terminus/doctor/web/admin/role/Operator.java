package io.terminus.doctor.web.admin.role;

import lombok.Data;

/**
 * @author Effet
 */
@Data
public class Operator {

    private Long id;

    private String username;

    private String password;

    private Long roleId;
}
