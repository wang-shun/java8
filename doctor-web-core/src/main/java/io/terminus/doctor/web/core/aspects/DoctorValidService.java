package io.terminus.doctor.web.core.aspects;

import io.terminus.doctor.common.validate.DoctorServiceInvokeValidator;
import org.springframework.stereotype.Component;

import javax.validation.Valid;

/**
 * Desc: @Valid 封装
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2017/2/15
 */
@Component
public class DoctorValidService {

    /**
     * 执行一下 @Valid 注解，实现入参里的校验
     * 由于 {@link DoctorServiceInvokeValidator#validate} 里的 point cut 配置的扫service包
     * 所以这个方法要在service包内
     */
    public <T> T valid(@Valid T t, String code) {
        return t;
    }
}
