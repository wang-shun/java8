package io.terminus.doctor.event.search.group;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorGroupDao;
import io.terminus.doctor.event.dao.DoctorGroupTrackDao;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.search.api.IndexExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Desc: 猪(索引对象)建立索引服务
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Component
@Slf4j
public class GroupDumpServiceImpl implements GroupDumpService {

    private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private GroupSearchProperties groupSearchProperties;

    @Autowired
    private IndexedGroupFactory indexedGroupFactory;

    @Autowired
    private IndexExecutor indexExecutor;

    @Autowired
    private IndexedGroupTaskAction indexedGroupTaskAction;

    @Autowired
    private DoctorGroupDao doctorGroupDao;

    @Autowired
    private DoctorGroupTrackDao doctorGroupTrackDao;

    /**
     * 全量dump
     * @return
     */
    @Override
    public Response<Boolean> fullDump(String before) {
        try {
            log.info("group full dump start");
            Stopwatch stopwatch = Stopwatch.createStarted();
            Integer fullDumpRange = groupSearchProperties.getFullDumpRange();
            if (fullDumpRange <= 0) {
                fullDumpRange = 3;
            }
            String since = DATE_TIME_FORMATTER.print(DateTime.now().withTimeAtStartOfDay().minusDays(fullDumpRange));
            // 如果before不为空, dump 这之前的数据
            if (StringUtils.isNotBlank(before)) {
                since = before;
            }
            int dumpCount = doIndex(since);

            stopwatch.stop();
            log.info("group full dump end. cost: {} ms, dumped {} groups",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), dumpCount);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("group full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("group.full.dump.fail");
        }
    }

    /**
     * 增量dump
     * @param interval  间隔时间(分钟)
     * @return
     */
    @Override
    public Response<Boolean> deltaDump(Integer interval) {
        try {
            log.info("group delta dump start");
            Stopwatch stopwatch = Stopwatch.createStarted();
            String since = DATE_TIME_FORMATTER.print(DateTime.now().minusMinutes(interval));
            int dumpCount = doIndex(since);

            stopwatch.stop();
            log.info("group delta dump end. cost: {} ms, dumped {} groups",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), dumpCount);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("group delta dump failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("group.delta.dump.fail");
        }
    }

    private int doIndex(String since) {
        // 猪最大id
        Long lastId = doctorGroupDao.maxId() + 1;

        // 记录数量
        int dumpCount = 0;
        while (true) {
            List<DoctorGroup> groups = doctorGroupDao.listSince(lastId, since, groupSearchProperties.getBatchSize());
            if (groups == null || groups.size() == 0) {
                break;
            }
            // 建立索引
            groups.forEach(group -> {
                try{
                    DoctorGroupTrack groupTrack = doctorGroupTrackDao.findByGroupId(group.getId());
                    IndexedGroup indexedGroup = indexedGroupFactory.create(group, groupTrack);
                    indexExecutor.submit(indexedGroupTaskAction.indexTask(indexedGroup));
                }catch (Exception e){
                    log.error("failed to index group(id={}),cause:{}", group.getId(), Throwables.getStackTraceAsString(e));
                }
            });

            dumpCount += groups.size();
            log.info("has indexed {} groups, and last handled id is {}", dumpCount, lastId);
            lastId = Iterables.getLast(groups).getId();

            // 如果是最后一个, 结束
            if (groups.size() < groupSearchProperties.getBatchSize()) {
                break;
            }
        }
        return dumpCount;
    }
}
