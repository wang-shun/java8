package io.terminus.doctor.event.search.pig;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
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
@RpcProvider
public class PigDumpServiceImpl implements PigDumpService {

    private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private PigSearchProperties pigSearchProperties;

    @Autowired
    private IndexedPigFactory indexedPigFactory;

    @Autowired
    private IndexExecutor indexExecutor;

    @Autowired
    private IndexedPigTaskAction indexedPigTaskAction;

    @Autowired
    private DoctorPigDao doctorPigDao;

    @Autowired
    private DoctorPigTrackDao doctorPigTrackDao;

    /**
     * 全量dump
     * @return
     */
    @Override
    public Response<Boolean> fullDump(String before) {
        try {
            log.info("pig full dump start");
            Stopwatch stopwatch = Stopwatch.createStarted();
            Integer fullDumpRange = pigSearchProperties.getFullDumpRange();
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
            log.info("pig full dump end. cost: {} ms, dumped {} pigs",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), dumpCount);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("pig full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("pig.full.dump.fail");
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
            log.info("pig delta dump start");
            Stopwatch stopwatch = Stopwatch.createStarted();
            String since = DATE_TIME_FORMATTER.print(DateTime.now().minusMinutes(interval));
            int dumpCount = doIndex(since);

            stopwatch.stop();
            log.info("pig delta dump end. cost: {} ms, dumped {} pigs",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), dumpCount);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("pig delta dump failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("pig.delta.dump.fail");
        }
    }

    private int doIndex(String since) {
        // 猪最大id
        Long lastId = doctorPigTrackDao.maxId() + 1;

        // 记录数量
        int dumpCount = 0;
        while (true) {
            List<DoctorPigTrack> pigTracks = doctorPigTrackDao.listSince(lastId, since, pigSearchProperties.getBatchSize());
            if (pigTracks == null || pigTracks.size() == 0) {
                break;
            }
            // 建立索引
            pigTracks.forEach(track -> {
                try{
                    DoctorPig pig = doctorPigDao.findById(track.getPigId());
                    IndexedPig indexedPig = indexedPigFactory.create(pig, track);
                    indexExecutor.submit(indexedPigTaskAction.indexTask(indexedPig));
                }catch (Exception e){
                    log.error("failed to index pig(id={}),cause:{}", track.getPigId(), Throwables.getStackTraceAsString(e));
                }
            });

            dumpCount += pigTracks.size();
            log.info("has indexed {} pigs, and last handled id is {}", dumpCount, lastId);
            lastId = Iterables.getLast(pigTracks).getId();

            // 如果是最后一个, 结束
            if (pigTracks.size() < pigSearchProperties.getBatchSize()) {
                break;
            }
        }
        return dumpCount;
    }
}
