package io.terminus.doctor.move.tools;

import io.terminus.doctor.move.dto.DoctorImportSow;
import org.springframework.stereotype.Component;

import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.common.utils.Checks.expectTrue;

/**
 * Created by xjn on 17/9/4.
 * excel 导入数据行校验器
 */
@Component
public class DoctorImportValidator {

    public DoctorImportSow valid(DoctorImportSow importSow) {
        expectTrue(!(notNull(importSow.getPregDate()) && importSow.getPregDate().before(importSow.getMateDate()))
                , "pregDate.before.mateDate", importSow.getLineNumber(), importSow.getSowCode());
        return importSow;
    }
}
