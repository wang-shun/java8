package io.terminus.doctor.event.search.barn;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dao.DoctorBarnDao;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.search.api.IndexExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Desc:
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 16/5/25
 */
@Slf4j
@Service
@RpcProvider
public class BarnSearchDumpServiceImpl implements BarnSearchDumpService {

    private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private BarnSearchProperties barnSearchProperties;

    @Autowired
    private IndexedBarnFactory indexedBarnFactory;

    @Autowired
    private IndexExecutor indexExecutor;

    @Autowired
    private IndexedBarnTaskAction indexedBarnTaskAction;

    @Autowired
    private DoctorBarnDao doctorBarnDao;

    @Override
    public Response<Boolean> fullDump(String before) {
        try {
            log.info("barn full dump start");
            Stopwatch stopwatch = Stopwatch.createStarted();
            Integer fullDumpRange = barnSearchProperties.getFullDumpRange();
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
            log.info("barn full dump end. cost: {} ms, dumped {} barns",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), dumpCount);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("barn full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("barn.full.dump.fail");
        }
    }

    @Override
    public Response<Boolean> deltaDump(Integer interval) {
        try {
            log.info("barn delta dump start");
            Stopwatch stopwatch = Stopwatch.createStarted();
            String since = DATE_TIME_FORMATTER.print(DateTime.now().minusMinutes(interval));
            int dumpCount = doIndex(since);

            stopwatch.stop();
            log.info("barn delta dump end. cost: {} ms, dumped {} barns",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), dumpCount);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("barn delta dump failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("barn.delta.dump.fail");
        }
    }

    private int doIndex(String since) {
        // 猪舍最大id
        Long lastId = doctorBarnDao.maxId() + 1;

        // 记录数量
        int dumpCount = 0;
        while (true) {
            List<DoctorBarn> barns = doctorBarnDao.listSince(lastId, since, barnSearchProperties.getBatchSize());
            if (barns == null || barns.size() == 0) {
                break;
            }
            // 建立索引
            barns.forEach(barn -> {
                try{
                    IndexedBarn indexedBarn = indexedBarnFactory.create(barn);
                    indexExecutor.submit(indexedBarnTaskAction.indexTask(indexedBarn));
                }catch (Exception e){
                    log.error("failed to index barn(id={}),cause:{}", barn.getId(), Throwables.getStackTraceAsString(e));
                }
            });

            dumpCount += barns.size();
            log.info("has indexed {} barns, and last handled id is {}", dumpCount, lastId);
            lastId = Iterables.getLast(barns).getId();

            // 如果是最后一个, 结束
            if (barns.size() < barnSearchProperties.getBatchSize()) {
                break;
            }
        }
        return dumpCount;
    }
}
