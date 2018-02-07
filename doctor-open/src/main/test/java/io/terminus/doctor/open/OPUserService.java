package io.terminus.doctor.open;

import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.doctor.open.rest.user.OPDoctorUsers;
import io.terminus.doctor.user.model.DoctorFarm;
import io.terminus.doctor.user.model.DoctorUserDataPermission;
import io.terminus.doctor.user.service.DoctorFarmReadService;
import io.terminus.doctor.user.service.DoctorUserDataPermissionReadService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Created by xjn on 18/2/1.
 * email:xiaojiannan@terminus.io
 */
@RunWith(MockitoJUnitRunner.class)
public class OPUserService {
    @Mock
    private DoctorUserDataPermissionReadService doctorUserDataPermissionReadService;
    @Mock
    private DoctorFarmReadService doctorFarmReadService;

    @Mock
    private OPDoctorUsers doctorUsers;

    @Test
    public void getUserStatus(){
        DoctorUserDataPermission permission = new DoctorUserDataPermission();
        permission.setFarmIds("1,2");
        DoctorFarm doctorFarm = new DoctorFarm();
        doctorFarm.setIsIntelligent(0);
        DoctorFarm doctorFarm1 = new DoctorFarm();
        doctorFarm1.setIsIntelligent(1);
        when(doctorUserDataPermissionReadService.findDataPermissionByUserId(anyLong())).thenReturn(Response.ok(permission));
        when(doctorFarmReadService.findFarmsByIds(anyList())).thenReturn(Response.ok(Lists.newArrayList(doctorFarm, doctorFarm1)));
    }
}
