/*
 * Copyright (c) 2014 杭州端点网络科技有限公司
 */

package io.terminus.doctor.web.core.component;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.terminus.common.utils.Arguments;

/**
 * Copyright (c) 2015 杭州端点网络科技有限公司
 * Date: 1/14/16
 * Time: 12:10 PM
 * Author: 2015年 <a href="mailto:d@terminus.io">张成栋</a>
 */
public class AliyunImageProperties {
    private static final Joiner UNDER_LINE = Joiner.on('_').skipNulls();
    private static final Object[] EMPTY_PARAM = new Object[0];
    private boolean normal = true;
    private Object[] params;

    // 高度
    private String high;
    // 宽度
    private String width;
    // 百分比
    private String percent;
    // 缩放优先边, 0 或 1， 默认值：0：长边（默认值）, 2 表示强制缩放, 指定值为 4 表示缩略后填充颜色
    private String edge;
    // 目标缩略图大于原图是否处理。如果值是1, 即不处理，是0，表示处理
    private String largeThan;
    // 图片质量
    // q: 相对质量,如果原图质量是80%，使用“90q”会得到质量72%的图片
    // Q: 绝对质量,如果原图质量是95%，使用“90Q”还会得到质量90%的图片
    private String quality;
    // 0(表示不旋转), 顺时针旋转
    private String rotate;
    // 自适应方向
    // 0：表示按原图默认方向，不进行自动旋转
    // 1：表示根据图片的旋转参数，对图片进行自动旋转，如果存在缩略参数，是先进行缩略，再进行旋转。
    // 2: 表示根据图片的旋转参数，对图片进行自动旋转，如果存在缩略参数，先进行旋转，再进行缩略
    private String opt;

    // todo: 缩放后背景色填充
    // todo: 裁剪
    // todo: 效果
    // todo: 水印

    public AliyunImageProperties(Object[] params) {
        setParams(params);
    }

    public String getQueryString() {
        return "@" + UNDER_LINE.join(getParameters());
    }

    public String[] getParameters() {
        return new String[] {high, width, percent, edge, largeThan, quality, rotate, opt};
    }

    /**
     * 设置高度
     *
     * @param high    格式为 100h, 单位为 px
     */
    public void setHigh(String high) {
        if (Arguments.isDecimal(high)) {
            this.high = high + "h";
            return;
        }
        this.high = checkFormat(high, "h");
    }

    /**
     * 设置宽度
     *
     * @param width    格式为 100w, 单位为 px
     */
    public void setWidth(String width) {
        if (Arguments.isDecimal(width)) {
            this.width = width + "w";
            return;
        }
        this.width = checkFormat(width, "w");
    }

    /**
     * 设置百分比
     * 与宽高同时设置等于相乘 100h * 200p = 200h
     *
     * @param percent    格式为 100p
     */
    public void setPercent(String percent) {
        this.percent = checkFormat(percent, "p");
    }

    /**
     * 缩放优先边, 0 或 1， 默认值：0：长边（默认值）, 2 表示强制缩放
     *
     * @param edge    格式为 1e
     */
    public void setEdge(String edge) {
        this.edge = checkFormat(edge, "e");
    }

    /**
     * 目标缩略图大于原图是否处理。如果值是1, 即不处理，是0，表示处理
     *
     * @param large    格式为 1l
     */
    public void setLargeThan(String large) {
        this.largeThan = checkBitFormat(large, "l");
    }

    /**
     * 图片质量
     * q: 相对质量,如果原图质量是80%，使用“90q”会得到质量72%的图片
     * Q: 绝对质量,如果原图质量是95%，使用“90Q”还会得到质量90%的图片
     *
     * @param quality    格式为 90q 或者 90Q
     */
    public void setQuality(String quality) {
        if (quality.endsWith("q")) {
            this.quality = checkFormat(quality, "q");
        } else {
            checkFormat(quality, "Q");
        }
    }

    /**
     * 0(表示不旋转), 顺时针旋转
     *
     * @param rotate    格式: 270r, 0~360
     */
    public void setRotate(String rotate) {
        this.rotate = checkFormat(rotate, "r");
    }

    /**
     * 0：表示按原图默认方向，不进行自动旋转
     * 1：表示根据图片的旋转参数，对图片进行自动旋转，如果存在缩略参数，是先进行缩略，再进行旋转。
     * 2: 表示根据图片的旋转参数，对图片进行自动旋转，如果存在缩略参数，先进行旋转，再进行缩略
     *
     * @param opt    格式: 0o
     */
    public void setOpt(String opt) {
        this.opt = checkFormat(opt, "o");
    }

    public void setParams(Object[] params) {
        // avoid NPE
        if (params == null || params.length == 0) {
            this.normal = false;
            this.params = EMPTY_PARAM;
        } else {
            this.params = params;
        }

        procParams();
    }

    /**
     * 遍历处理图片参数
     */
    private void procParams() {
        for (int i = 0; i < params.length; i++) {
            String param = params[i].toString();
            boolean bare = Arguments.isDecimal(param);
            if (bare) {
                if (i == 0) {
                    setWidth(param);
                } else if (i == 1) {
                    setHigh(param);
                } else {
                    continue;
                }
            }

            switch (param.charAt(param.length() - 1)) {
                case 'p': setPercent(param); break;
                case 'e': setEdge(param); break;
                case 'l': setLargeThan(param); break;
                case 'h': setHigh(param); break;
                case 'w': setWidth(param); break;
                case 'q':
                case 'Q': setQuality(param); break;
                case 'r': setRotate(param); break;
                case 'o': setOpt(param); break;
            }
        }
    }

    /**
     * 检查参数格式是否为 0 或 1 开始, 以指定的参数限定符号结束
     *
     * @param param     单个图片参数
     * @param suffix    参数限定参数, 例如 l 或者 e 等
     * @return 符合格式就返回正确的参数, 例如 1e, 否则返回 null
     */
    private String checkBitFormat(String param, String suffix) {
        if (normal && !Strings.isNullOrEmpty(param)
                && param.endsWith(suffix) && (param.charAt(0) == '0' || param.charAt(0) == '1')) {
            return param;
        }
        return null;
    }

    /**
     * 检查参数格式是否为数字开始, 以指定的参数限定符号结束
     *
     * @param param     单个图片参数
     * @param suffix    参数限定参数, 例如 h 或者 w 等
     * @return 符合格式就返回正确的参数, 例如 100h, 否则返回 null
     */
    private String checkFormat(String param, String suffix) {
        if (normal && !Strings.isNullOrEmpty(param)
                && param.endsWith(suffix) && Arguments.isDecimal(param.substring(0, param.length() - 1))) {
            return param;
        }
        return null;
    }

    public static io.terminus.doctor.web.core.component.AliyunImageProperties from(Object... params) {
        return new io.terminus.doctor.web.core.component.AliyunImageProperties(params);
    }
}
