package io.terminus.doctor.move.manager;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Joiners;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.dto.DoctorBasicInputInfoDto;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.manager.DoctorGroupEventManager;
import io.terminus.doctor.event.manager.DoctorGroupManager;
import io.terminus.doctor.event.manager.DoctorPigEventManager;
import io.terminus.doctor.move.builder.DoctorPigEventInputBuilder;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.handler.DoctorMoveDatasourceHandler;
import io.terminus.doctor.move.model.View_EventListBoar;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import io.terminus.doctor.move.model.View_SowCardList;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by xjn on 17/8/4.
 * 迁移与导入
 */
@Slf4j
@Component
public class DoctorMoveAndImportManager {
    @Autowired
    public DoctorPigEventManager pigEventManager;
    @Autowired
    public DoctorGroupEventManager groupEventManager;
    @Autowired
    public DoctorGroupManager groupManager;
    @Autowired
    public DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;
    @Autowired
    private Map<String, DoctorPigEventInputBuilder> pigEventBuilderMap;

    public void executePigEvent(DoctorMoveBasicData moveBasicData, List<? extends View_EventListPig> rawEventList) {
        DoctorBasicInputInfoDto basicInputInfoDto = buildBasicInputInfo(moveBasicData);
        rawEventList.forEach(rawPigEvent -> {

            //1.构建事件所需数据
            DoctorPigEventInputBuilder pigEventInputBuilder = pigEventBuilderMap.get(rawPigEvent.getEventName());
            BasePigEventInputDto pigEventInputDto = pigEventInputBuilder.buildPigEventInput(moveBasicData, rawPigEvent);

            //2.执行事件
            pigEventManager.eventHandle(pigEventInputDto, basicInputInfoDto);
        });
    }

    public void executeGroupEvent() {
        //1.构建事件所需数据

        //2.执行事件
    }

    public List<View_EventListBoar> getAllRawBoarEvent(Long moveId, DoctorFarm farm) {
        return RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_EventListBoar.class, "DoctorPigEvent-EventListBoar")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId())).collect(Collectors.toList());
    }

    public List<View_EventListSow> getAllRawSowEvent(Long moveId, DoctorFarm farm) {
        List<View_SowCardList> sowCards = RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_SowCardList.class, "DoctorPig-SowCardList")).stream()
                .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                .collect(Collectors.toList());

        List<View_EventListSow> sowEventViews = Lists.newArrayList();
        List<List<View_SowCardList>> sowLists = Lists.partition(sowCards, sowCards.size() / 5 + 1);
        sowLists.forEach(ss -> {
            String sowOutIds = Joiners.COMMA.join(ss.stream().map(s -> brace(s.getPigOutId())).collect(Collectors.toList()));
            sowEventViews.addAll(RespHelper.orServEx(doctorMoveDatasourceHandler
                    .findByHbsSql(moveId, View_EventListSow.class, "DoctorPigEvent-EventListSow", ImmutableMap.of("sowOutIds", sowOutIds))).stream()
                    .filter(loc -> isFarm(loc.getFarmOutId(), farm.getOutId()))
                    .collect(Collectors.toList())
            );
        });

        return sowEventViews;
    }

    private DoctorBasicInputInfoDto buildBasicInputInfo(DoctorMoveBasicData moveBasicData) {
        DoctorFarm farm = moveBasicData.getDoctorFarm();
        // TODO: 17/8/4 操作人暂时随便设置一个
        return DoctorBasicInputInfoDto.builder().farmId(farm.getId())
                .farmName(farm.getName())
                .orgId(farm.getOrgId())
                .orgName(farm.getOrgName())
                .staffId(-1L)
                .staffName("")
                .build();
    }

    private static boolean isFarm(String farmOID, String outId) {
        return Objects.equals(farmOID, outId);
    }

    private static String brace(String name) {
        return "'" + name + "'";
    }
}
