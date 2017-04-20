package io.terminus.doctor.event.model;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 13:58 2017/4/20
 */
@Data
public class DoctorGroupChangeSum implements Serializable{
    private static final long serialVersionUID = -7911853420209555558L;

    /**
     * 期初
     */
    private Integer farrowStart;
    /**
     * 同类型猪群转入，后面统计不计入该类型猪群转入
     */
    private Integer farrowInnerIn;

    /**
     * 不同类型猪群转入，外部转入
     */
    private Integer farrowOuterIn;

    /**
     * 销售
     */
    private Integer farrowSale;

    /**
     * 死亡
     */
    private Integer farrowDead;

    /**
     * 淘汰
     */
    private Integer farrowWeedOut;

    /**
     * 其他变动减少
     */
    private Integer farrowOtherChange;

    /**
     * 转场
     */
    private Integer farrowChgFarm;

    /**
     * 同类型猪群转群，不计入该类型猪	群减少
     */
    private Integer farrowInnerOut;

    /**
     * 不同类型猪群转群,转种猪
     */
    private Integer farrowOuterOut;

    /**
     * 期末
     */
    private Integer farrowEnd;


    /**
     * 期初
     */
    private Integer nurseryStart;
    /**
     * 同类型猪群转入，后面统计不计入该类型猪群转入
     */
    private Integer nurseryInnerIn;

    /**
     * 不同类型猪群转入，外部转入
     */
    private Integer nurseryOuterIn;

    /**
     * 销售
     */
    private Integer nurserySale;

    /**
     * 死亡
     */
    private Integer nurseryDead;

    /**
     * 淘汰
     */
    private Integer nurseryWeedOut;

    /**
     * 其他变动减少
     */
    private Integer nurseryOtherChange;

    /**
     * 转场
     */
    private Integer nurseryChgFarm;

    /**
     * 同类型猪群转群，不计入该类型猪	群减少
     */
    private Integer nurseryInnerOut;

    /**
     * 不同类型猪群转群,转种猪
     */
    private Integer nurseryOuterOut;

    /**
     * 期末
     */
    private Integer nurseryEnd;


    /**
     * 期初
     */
    private Integer fattenStart;
    /**
     * 同类型猪群转入，后面统计不计入该类型猪群转入
     */
    private Integer fattenInnerIn;

    /**
     * 不同类型猪群转入，外部转入
     */
    private Integer fattenOuterIn;

    /**
     * 销售
     */
    private Integer fattenSale;

    /**
     * 死亡
     */
    private Integer fattenDead;

    /**
     * 淘汰
     */
    private Integer fattenWeedOut;

    /**
     * 其他变动减少
     */
    private Integer fattenOtherChange;

    /**
     * 转场
     */
    private Integer fattenChgFarm;

    /**
     * 同类型猪群转群，不计入该类型猪	群减少
     */
    private Integer fattenInnerOut;

    /**
     * 不同类型猪群转群,转种猪
     */
    private Integer fattenOuterOut;

    /**
     * 期末
     */
    private Integer fattenEnd;


    /**
     * 期初
     */
    private Integer houbeiStart;
    /**
     * 同类型猪群转入，后面统计不计入该类型猪群转入
     */
    private Integer houbeiInnerIn;

    /**
     * 不同类型猪群转入，外部转入
     */
    private Integer houbeiOuterIn;

    /**
     * 销售
     */
    private Integer houbeiSale;

    /**
     * 死亡
     */
    private Integer houbeiDead;

    /**
     * 淘汰
     */
    private Integer houbeiWeedOut;

    /**
     * 其他变动减少
     */
    private Integer houbeiOtherChange;

    /**
     * 转场
     */
    private Integer houbeiChgFarm;

    /**
     * 同类型猪群转群，不计入该类型猪	群减少
     */
    private Integer houbeiInnerOut;

    /**
     * 不同类型猪群转群,转种猪
     */
    private Integer houbeiOuterOut;

    /**
     * 期末
     */
    private Integer houbeiEnd;
}
