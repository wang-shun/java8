package io.terminus.doctor.event.manager;

import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.common.utils.ToJsonMapper;
import io.terminus.doctor.event.dao.DoctorMessageDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigSnapshotDao;
import io.terminus.doctor.event.dto.DoctorPigSnapShotInfo;
import io.terminus.doctor.event.dto.msg.DoctorMessageSearchDto;
import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigSnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by chenzenghui on 16/12/19.
 */
@Component
@Slf4j
public class DoctorPigManager {
    private static final JsonMapperUtil MAPPER = JsonMapperUtil.nonDefaultMapperWithFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    private static final JsonMapperUtil IN_JSON_MAPPER = JsonMapperUtil.JSON_NON_DEFAULT_MAPPER;

    private final DoctorPigDao doctorPigDao;
    private final DoctorPigEventDao doctorPigEventDao;

    @Autowired
    private DoctorPigSnapshotDao doctorPigSnapshotDao;

    @Autowired
    private DoctorMessageDao doctorMessageDao;

    @Autowired
    public DoctorPigManager(DoctorPigDao doctorPigDao,
                            DoctorPigEventDao doctorPigEventDao) {
        this.doctorPigDao = doctorPigDao;
        this.doctorPigEventDao = doctorPigEventDao;
    }

    /**
     * 批量更新pigCode
     * @param pigs 猪
     * @return 是否成功
     */
    @Transactional
    public boolean updatePigCodes(List<DoctorPig> pigs) {
        pigs.forEach(pig -> updatePigCode(pig.getId(), pig.getPigCode()));
        return true;
    }

    private void updatePigCode(Long pigId, String pigCode) {
        checkCanUpdate(pigId, pigCode);

        //更新猪
        DoctorPig updatePig = new DoctorPig();
        updatePig.setId(pigId);
        updatePig.setPigCode(pigCode);
        doctorPigDao.update(updatePig);
        doctorPigEventDao.updatePigCode(pigId, pigCode);

        List<DoctorPigSnapshot> snapshots = doctorPigSnapshotDao.findByPigId(pigId);
        snapshots.forEach(snapshot -> {
            DoctorPigSnapShotInfo info = MAPPER.fromJson(snapshot.getToPigInfo(), DoctorPigSnapShotInfo.class);
            info.getPig().setPigCode(pigCode);
            //info.getPigEvent().setPigCode(pigCode);
            snapshot.setToPigInfo(ToJsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(info));
            doctorPigSnapshotDao.update(snapshot);
        });

        //更新消息
        DoctorMessageSearchDto msgSearch = new DoctorMessageSearchDto();
        msgSearch.setBusinessId(pigId);
        doctorMessageDao.list(msgSearch).forEach(msg -> {
            DoctorMessage updateMsg = new DoctorMessage();
            updateMsg.setId(msg.getId());
            updateMsg.setCode(pigCode);
            doctorMessageDao.update(updateMsg);
        });
    }

    private void checkCanUpdate(Long pigId, String newCode) {
        if (!StringUtils.hasText(newCode)) {
            throw new ServiceException("pigCode.not.empty");
        }
        DoctorPig pig = doctorPigDao.findById(pigId);
        if (pig == null) {
            throw new ServiceException("pig.not.found");
        }
        if (doctorPigDao.findPigByFarmIdAndPigCodeAndSex(pig.getFarmId(), newCode, DoctorPig.PigSex.SOW.getKey()) != null) {
            throw new ServiceException("新猪号" + newCode + "已存在");
        }
    }
}
