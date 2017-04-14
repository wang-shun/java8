package io.terminus.doctor.basic.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Desc: 投入品价值
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/14
 */
@Data
public class ValueOfInput implements Serializable {

    private static final long serialVersionUID = -8083541177966328901L;
    
    /**
     * 名称
     */
    private String name;
    
    /**
     * 使用次数
     */
    private String count;

}