package io.terminus.doctor.user.dto;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc:
 * Mail: houly@terminus.io
 * author: Hou Luyao
 * Date: 15/11/27.
 */
@Data
public class DoctorMenuDto implements Serializable{
    private static final long serialVersionUID = 5841835065219471069L;

    private Long id;

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
     * 是否需要手机端页面 0不需要 1需要
     */
    private Integer needMobilePage;
    /**
     * 父类菜单
     */
    private Long pid;

    /**
     * 是否选中
     */
    private boolean selected = false;

    /**
     * 子菜单
     */
    private List<DoctorMenuDto> childrenMenus = Lists.newArrayList();

    /**
     * 对应的res的集合
     */
    private List<DoctorRes> reses = Lists.newArrayList();

}
