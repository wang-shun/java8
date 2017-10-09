package io.terminus.doctor.move.tools;

import org.springframework.stereotype.Component;

import javax.validation.Valid;

/**
 * Created by xjn on 17/8/28.
 * 事件输入校验器
 */
@Component
public class DoctorEventInputValidator {

    public <T> T valid(@Valid T t) { return t;}
}
