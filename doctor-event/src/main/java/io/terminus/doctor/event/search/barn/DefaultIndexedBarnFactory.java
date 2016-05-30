package io.terminus.doctor.event.search.barn;

import io.terminus.doctor.event.service.DoctorBarnReadService;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */

public class DefaultIndexedBarnFactory extends BaseIndexedBarnFactory<IndexedBarn> {
    public DefaultIndexedBarnFactory(DoctorBarnReadService doctorBarnReadService) {
        super(doctorBarnReadService);
    }
}
