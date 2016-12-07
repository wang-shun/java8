package io.terminus.doctor.user.model;


import java.io.Serializable;
import java.util.List;

/**
 * Created by yudi on 2016/12/7.
 * Mail to yd@terminus.io
 */
public class DoctorRoleContent implements Serializable{
    private static final long serialVersionUID = -6365607925589602895L;
    private List<DoctorRole> roles;
    private List<DoctorRole> dynamicRoles;

    public List<DoctorRole> getRoles() {
        return this.roles;
    }

    public List<DoctorRole> getDynamicRoles() {
        return this.dynamicRoles;
    }

    public void setRoles(List<DoctorRole> roles) {
        this.roles = roles;
    }

    public void setDynamicRoles(List<DoctorRole> dynamicRoles) {
        this.dynamicRoles = dynamicRoles;
    }


    public String toString() {
        return "RoleContent(roles=" + this.getRoles() + ", dynamicRoles=" + this.getDynamicRoles() + ")";
    }
}
