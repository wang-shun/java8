package io.terminus.doctor.warehouse.search.material;

import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.warehouse.dao.DoctorMaterialInfoDao;
import io.terminus.doctor.warehouse.model.DoctorMaterialInfo;
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
 * Desc: 物料(索引对象)建立索引服务
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Slf4j
@Service
@RpcProvider
public class MaterialDumpServiceImpl implements MaterialDumpService {

    private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MaterialSearchProperties materialSearchProperties;

    @Autowired
    private IndexedMaterialFactory indexedMaterialFactory;

    @Autowired
    private IndexExecutor indexExecutor;

    @Autowired
    private IndexedMaterialTaskAction indexedMaterialTaskAction;

    @Autowired
    private DoctorMaterialInfoDao doctorMaterialInfoDao;

    /**
     * 全量dump
     */
    @Override
    public Response<Boolean> fullDump(String before) {
        try {
            log.info("material full dump start");
            Stopwatch stopwatch = Stopwatch.createStarted();
            Integer fullDumpRange = materialSearchProperties.getFullDumpRange();
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
            log.info("material full dump end. cost: {} ms, dumped {} materials",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), dumpCount);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("material full dump failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("material.full.dump.fail");
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
            log.info("material delta dump start");
            Stopwatch stopwatch = Stopwatch.createStarted();
            String since = DATE_TIME_FORMATTER.print(DateTime.now().minusMinutes(interval));
            int dumpCount = doIndex(since);

            stopwatch.stop();
            log.info("material delta dump end. cost: {} ms, dumped {} materials",
                    stopwatch.elapsed(TimeUnit.MILLISECONDS), dumpCount);
            return Response.ok(Boolean.TRUE);
        } catch (Exception e) {
            log.error("material delta dump failed, cause by {}", Throwables.getStackTraceAsString(e));
            return Response.fail("material.delta.dump.fail");
        }
    }

    private int doIndex(String since) {
        // 猪最大id
        Long lastId = doctorMaterialInfoDao.maxId() + 1;

        // 记录数量
        int dumpCount = 0;
        while (true) {
            List<DoctorMaterialInfo> materials = doctorMaterialInfoDao.listSince(lastId, since, materialSearchProperties.getBatchSize());
            if (materials == null || materials.size() == 0) {
                break;
            }
            // 建立索引
            materials.forEach(material -> {
                try{
                    IndexedMaterial indexedMaterial = indexedMaterialFactory.create(material);
                    indexExecutor.submit(indexedMaterialTaskAction.indexTask(indexedMaterial));
                }catch (Exception e){
                    log.error("failed to index material(id={}),cause:{}", material.getId(), Throwables.getStackTraceAsString(e));
                }
            });

            dumpCount += materials.size();
            log.info("has indexed {} materials, and last handled id is {}", dumpCount, lastId);
            lastId = Iterables.getLast(materials).getId();

            // 如果是最后一个, 结束
            if (materials.size() < materialSearchProperties.getBatchSize()) {
                break;
            }
        }
        return dumpCount;
    }
}
