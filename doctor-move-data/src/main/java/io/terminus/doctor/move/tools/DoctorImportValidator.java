package io.terminus.doctor.move.tools;

import io.terminus.doctor.move.dto.DoctorImportSow;
import org.springframework.stereotype.Component;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;
import static io.terminus.doctor.move.tools.DoctorMessageConverter.assembleErrorAttach;

/**
 * Created by xjn on 17/9/4.
 * excel 导入数据行校验器
 */
@Component
public class DoctorImportValidator {

    public DoctorImportSow valid(DoctorImportSow importSow) {

        if (notNull(importSow.getPregCheckDate())) {
            expectTrue(!importSow.getPregCheckDate().before(importSow.getMateDate()), false,
                    assembleErrorAttach(null, importSow.getLineNumber()),
                    "pregCheckDate.before.mateDate");
        }

        if (notNull(importSow.getPregDate())) {
            expectTrue(!importSow.getPregDate().before(importSow.getPregCheckDate()), false,
                    assembleErrorAttach(null, importSow.getLineNumber()),
                    "pregDate.before.pregCheckDate");
        }

        // TODO: 17/9/4 字段不为空校验 暂时没时间写以后再优化
        return importSow;
    }
}
