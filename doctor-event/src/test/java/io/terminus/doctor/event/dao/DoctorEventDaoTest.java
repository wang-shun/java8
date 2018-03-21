package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.utils.Splitters;
import io.terminus.doctor.event.enums.IsOrNot;
import io.terminus.doctor.event.model.DoctorPigEvent;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.isNull;
import static io.terminus.common.utils.Arguments.notNull;
import static io.terminus.doctor.event.handler.DoctorAbstractEventHandler.IGNORE_EVENT;

/**
 * Created by xjn on 16/9/2.
 */
public class DoctorEventDaoTest extends BaseDaoTest {
    @Autowired
    private DoctorPigEventDao doctorPigEventDao;
    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;
    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;
    @Autowired
    private DoctorGroupJoinDao doctorGroupJoinDao;
    @Test
    public void findByIds(){
        List<DoctorPigEvent> doctorPigEvents = doctorPigEventDao.findByIds(Splitters.COMMA.splitToList(doctorPigTrackDao.findById(191).getRelEventIds()).stream().filter(id -> StringUtils.isNotBlank(id)).map(id -> Long.parseLong(id)).collect(Collectors.toList()));
    }


    @Test
    public void queryFattenOutBySumAt(){
        System.out.println(doctorGroupTrackDao.queryFattenOutBySumAt(ImmutableMap.of("avgDayAge", 180, "sumAt", "2016-12-26")));
    }

    @Test
    public void dayAgeListForBarn() {
        System.out.println(doctorGroupJoinDao.dayAgeListForBarn(164L));
    }

    @Test
    public void findUnWeanCountByParity() {
        Integer unWeanCount = doctorPigEventDao.findUnWeanCountByParity(498954L, 1);
        Assert.assertEquals(2L, unWeanCount.longValue());
    }

    @Test
    public void isLastEventTest() {
        DoctorPigEvent pigEvent = doctorPigEventDao.findEventById(5403014L);
        DoctorPigEvent lastEvent = doctorPigEventDao.findLastEventExcludeTypes(pigEvent.getPigId(), IGNORE_EVENT);
        Boolean b = notNull(lastEvent)
                && Objects.equals(pigEvent.getId(), lastEvent.getId())
                && (isNull(pigEvent.getIsAuto()) || pigEvent.getIsAuto() == IsOrNot.NO.getValue());

        Assert.assertTrue(b);
    }
}
