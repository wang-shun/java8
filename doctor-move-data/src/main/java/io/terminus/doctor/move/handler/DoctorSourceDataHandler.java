package io.terminus.doctor.move.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.terminus.common.utils.Joiners;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.move.model.View_EventListBoar;
import io.terminus.doctor.move.model.View_EventListGain;
import io.terminus.doctor.move.model.View_EventListSow;
import io.terminus.doctor.move.model.View_SowCardList;
import io.terminus.doctor.user.model.DoctorFarm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by xjn on 17/8/9.
 * 源数据获取封装
 */
@Slf4j
@Component
public class DoctorSourceDataHandler {
    @Autowired
    private DoctorMoveDatasourceHandler doctorMoveDatasourceHandler;

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

    public List<View_EventListGain> getAllRawGroupEvent(Long moveId) {
        return RespHelper.orServEx(doctorMoveDatasourceHandler
                .findByHbsSql(moveId, View_EventListGain.class, "DoctorGroupEvent-EventListGain")).stream()
                .filter(event -> event != null)
                .collect(Collectors.toList());
    }

    public List<View_EventListGain> getAllRawNewGroupEvent(List<View_EventListGain> allRawGroupEvent) {
        return allRawGroupEvent.parallelStream().filter(view_eventListGain ->
                Objects.equals(view_eventListGain.getEventTypeName(), GroupEventType.NEW.getDesc()))
                .collect(Collectors.toList());
    }

    public List<View_EventListGain> getAllRawGroupEventExcludeNew(List<View_EventListGain> allRawGroupEvent) {
        return allRawGroupEvent.parallelStream().filter(view_eventListGain ->
                !Objects.equals(view_eventListGain.getEventTypeName(), GroupEventType.NEW.getDesc()))
                .collect(Collectors.toList());
    }

    private static boolean isFarm(String farmOID, String outId) {
        return Objects.equals(farmOID, outId);
    }

    private static String brace(String name) {
        return "'" + name + "'";
    }
}
