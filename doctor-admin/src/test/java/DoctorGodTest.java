import io.terminus.common.model.Response;
import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.basic.service.DoctorBasicReadService;
import io.terminus.doctor.common.utils.DateUtil;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.enums.PigEvent;
import io.terminus.doctor.event.model.DoctorPigEvent;
import io.terminus.doctor.event.service.DoctorModifyEventService;
import io.terminus.doctor.event.service.DoctorPigEventReadService;
import io.terminus.doctor.event.service.DoctorPigReadService;
import io.terminus.doctor.web.admin.controller.DoctorGodController;
import io.terminus.doctor.web.admin.utils.PigEventFeeder;
import io.terminus.doctor.web.admin.utils.SmartPigEventHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

/**
 * Created by sunbo@terminus.io on 2017/9/13.
 */
@RunWith(MockitoJUnitRunner.class)
public class DoctorGodTest {

    protected final ToJsonMapper TO_JSON_MAPPER = ToJsonMapper.JSON_NON_DEFAULT_MAPPER;

    @Mock
    DoctorPigEventReadService doctorPigEventReadService;

    @Mock
    DoctorBasicReadService doctorBasicReadService;

    @Mock
    DoctorModifyEventService doctorModifyEventService;

    @Mock
    DoctorPigReadService doctorPigReadService;

    @Mock
    SmartPigEventHandler smartPigEventHandler;

    @InjectMocks
    private DoctorGodController doctorGodController;


    @Test
    public void entryEvent() throws Exception {
        DoctorPigEvent entryEvent = DoctorPigEvent.builder()
                .id(872097L)
                .orgId(95L)
                .orgName("武汉金龙畜禽有限公司")
                .farmId(94L)
                .farmName("湖北新今农农牧股份有限公司")
                .pigId(75928L)
                .pigCode("A3909")
                .isAuto(1)
                .eventAt(DateUtil.toDate("2017-03-06"))
                .type(7)
                .kind(1)
                .name("进场")
                .desc("品种：二元#品系：新美系")
                .barnId(2525L)
                .barnName("妊娠10舍")
                .barnType(6)
                .relGroupEventId(88243L)
                .pigStatusBefore(1)
                .pigStatusAfter(1)
                .parity(1)
                .dpnpd(0)
                .pfnpd(0)
                .plnpd(0)
                .psnpd(0)
                .pynpd(0)
                .ptnpd(0)
                .jpnpd(0)
                .npd(0)
                .status(1)
                .eventSource(1)
                .extra("{\"pigId\":75928,\"pigCode\":\"A3909\",\"barnId\":2525,\"birthday\":1457222400000,\"inFarmDate\":1488758400000,\"source\":1,\"breed\":2,\"breedType\":6,\"fatherCode\":\"ddd\",\"motherCode\":\"\",\"entryMark\":\"\",\"earCode\":\"\",\"parity\":1}")
                .source(1)
                .breedId(2L)
                .breedName("二元")
                .breedTypeId(6L)
                .breedTypeName("新美系")
                .operatorId(10403L)
                .operatorName("cyq@hbxjn")
                .creatorId(10403L)
                .creatorName("cyq@hbxjn")
                .createdAt(DateUtil.toDateTime("2017-03-06 20:37:35"))
                .updatedAt(DateUtil.toDateTime("2017-09-11 15:06:07"))
                .build();

        String oldPigEvent = TO_JSON_MAPPER.toJson(entryEvent);


//        Mockito.when(doctorPigEventReadService.findById(Mockito.anyLong())).thenReturn(Response.ok(entryEvent));
//        DoctorBasic basic = new DoctorBasic();
//        basic.setName("大话猪");
//        DoctorBasic breedTypeBasic = new DoctorBasic();
//        breedTypeBasic.setName("中华田园系");
//        Mockito.when(doctorBasicReadService.findBasicById(3L)).thenReturn(Response.ok(basic)).thenReturn(Response.ok(breedTypeBasic));
        Mockito.when(smartPigEventHandler.isSupportedEvent(Mockito.anyObject())).thenReturn(true);

        Mockito.when(doctorModifyEventService.modifyPigEvent(Mockito.anyString(), Mockito.anyObject())).thenReturn(RespWithEx.ok(true));

        doctorGodController.eventEdit(Mockito.anyLong(), "{\"pigId\":\"75928\",\"barnId\":\"2525\",\"birthday\":\"2016-03-06\",\"inFarmDate\":\"2017-03-06\",\"pigCode\":\"A3909\",\"source\":\"1\",\"breed\":\"3\",\"breedType\":\"8\",\"parity\":\"1\",\"fatherCode\":\"d\",\"motherCode\":\"fme\",\"earCode\":\"443\",\"left\":\"45\",\"right\":\"4\",\"entryMark\":\"987\"}");

//        entryEvent.setBreedId(3L);
        entryEvent.setBreedName("大话猪");
        entryEvent.setBreedTypeId(8L);
        entryEvent.setBreedTypeName("中华田园系");
        entryEvent.setRemark("987");
        Mockito.verify(doctorModifyEventService).modifyPigEvent(Mockito.eq(oldPigEvent), Mockito.refEq(entryEvent));

    }


}
