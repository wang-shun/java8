package io.terminus.doctor.user.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 菜单
 * Mail: houly@terminus.io
 * author: Hou Luyao
 * Date: 15/11/27.
 */
@Data
public class DoctorMenu implements Serializable{
    private static final long serialVersionUID = -8492370759205322578L;

    private Long id;
    /**
     * 父级id
     */
    private Long pid;
    /**
     * 名称
     */
    private String name;
    /**
     * 级别
     */
    private Integer level;
    /**
     * 访问路径
     */
    private String url;
    /**
     * 是否含有logo 1:含有 0:不含有
     */
    private Integer hasIcon;
    /**
     * logoClass
     */
    private String iconClass;
    /**
     * logo路径
     */
    private String icon;
    /**
     * 类型 1:普通路径页面 2:自定义图表 3:虚拟节点 4:全局res白名单节点
     */
    private Integer type;
    /**
     * 排序号
     */
    private Integer orderNo;
    /**
     * 是否需要隐藏
     */
    private Integer needHiden;
    /**
     * 是否需要手机端页面
     */
    private Integer needMobilePage;
    /**
     * 是否是虚拟节点
     */
    private Integer resVirtual;
    /**
     * 权限key名称
     */
    private String resKey;
    /**
     * 权限名称
     */
    private String resName;
    /**
     * 访问路径
     */
    private String resPathUrl;
    /**
     * 访问方法，1:GET 2:POST
     */
    private Integer resMethod;

    private Date createdAt;

    private Date updatedAt;
}
