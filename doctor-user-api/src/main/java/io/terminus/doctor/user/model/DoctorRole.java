package io.terminus.doctor.user.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yudi on 2016/12/7.
 * Mail to yd@terminus.io
 */
public class DoctorRole implements Serializable {
    private static final long serialVersionUID = -6153398683849427431L;
    private String base;
    private int type;
    private List<String> names;
    private Map<String, String> context;
    private List<String> treeNodeSelection;

    public static DoctorRole createStatic(String roleText) {
        DoctorRole r = new DoctorRole();
        r.setBase(roleText);
        r.setType(1);
        r.setNames(new ArrayList());
        r.setContext(new HashMap());
        r.setTreeNodeSelection(null);
        return r;
    }

    public static DoctorRole createDynamic(String roleText, List<String> nodes, List<String> names) {
        DoctorRole r = new DoctorRole();
        r.setBase(roleText);
        r.setType(2);
        r.setNames(new ArrayList(names));
        r.setContext(new HashMap());
        r.setTreeNodeSelection(new ArrayList(nodes));
        return r;
    }

    public DoctorRole() {
    }

    public String getBase() {
        return this.base;
    }

    public int getType() {
        return this.type;
    }

    public List<String> getNames() {
        return this.names;
    }

    public Map<String, String> getContext() {
        return this.context;
    }

    public List<String> getTreeNodeSelection() {
        return this.treeNodeSelection;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setNames(List<String> names) {
        this.names = names;
    }

    public void setContext(Map<String, String> context) {
        this.context = context;
    }

    public void setTreeNodeSelection(List<String> treeNodeSelection) {
        this.treeNodeSelection = treeNodeSelection;
    }

    public String toString() {
        return "Role(base=" + this.getBase() + ", type=" + this.getType() + ", names=" + this.getNames() + ", context=" + this.getContext() + ", treeNodeSelection=" + this.getTreeNodeSelection() + ")";
    }
}
