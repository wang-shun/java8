package io.terminus.doctor.msg.producer;

import io.terminus.doctor.msg.dao.DoctorMessageDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleRoleDao;
import io.terminus.doctor.msg.dao.DoctorMessageRuleTemplateDao;
import io.terminus.doctor.msg.enums.Category;
import io.terminus.doctor.msg.model.DoctorMessage;
import io.terminus.doctor.msg.model.DoctorMessageRuleRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Desc: 待配种母猪提示
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/6/1
 */
@Component
public class SowBreedingProducer extends AbstractProducer {

    @Autowired
    public SowBreedingProducer(DoctorMessageRuleTemplateDao doctorMessageRuleTemplateDao,
                               DoctorMessageRuleDao doctorMessageRuleDao,
                               DoctorMessageRuleRoleDao doctorMessageRuleRoleDao,
                               DoctorMessageDao doctorMessageDao) {
        super(doctorMessageRuleTemplateDao,
                doctorMessageRuleDao,
                doctorMessageRuleRoleDao,
                doctorMessageDao,
                Category.SOW_BREEDING);
    }

    @Override
    protected List<DoctorMessage> message(DoctorMessageRuleRole ruleRole) {

        // TODO 处理母猪待配种消息

        return null;
    }
}
