package io.terminus.doctor.event.manager;

import com.google.common.base.Strings;
import io.terminus.common.exception.ServiceException;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dao.DoctorMessageDao;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.dao.DoctorPigEventDao;
import io.terminus.doctor.event.dao.DoctorPigTrackDao;
import io.terminus.doctor.event.dto.msg.DoctorMessageSearchDto;
import io.terminus.doctor.event.model.DoctorMessage;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final DoctorPigTrackDao doctorPigTrackDao;

    @Autowired
    private DoctorMessageDao doctorMessageDao;

    @Autowired
    public DoctorPigManager(DoctorPigDao doctorPigDao,
                            DoctorPigEventDao doctorPigEventDao,
                            DoctorPigTrackDao doctorPigTrackDao) {
        this.doctorPigDao = doctorPigDao;
        this.doctorPigEventDao = doctorPigEventDao;
        this.doctorPigTrackDao = doctorPigTrackDao;
    }

    /**
     * 批量更新pigCode
     *
     * @param pigs 猪
     * @return 是否成功
     */
    @Transactional
    public boolean updatePigCodes(List<DoctorPig> pigs) {
        pigs.forEach(pig -> updatePigCode(pig.getId(), pig.getPigCode(), pig.getRfid()));
        return true;
    }


    @Transactional
    public void updatePig(DoctorPig pig, DoctorPigTrack pigTrack) {

        doctorPigDao.update(pig);
        doctorPigTrackDao.update(pigTrack);
    }

    private void updatePigCode(Long pigId, String pigCode, String rfid) {

        //更新猪
        DoctorPig updatePig = new DoctorPig();
        updatePig.setId(pigId);
        if (!Strings.isNullOrEmpty(pigCode)) {
            checkCanUpdate(pigId, pigCode);
            updatePig.setPigCode(pigCode);
        }
        if (!Strings.isNullOrEmpty(rfid)) {
            updatePig.setRfid(rfid);
        }
        doctorPigDao.update(updatePig);

        if (Strings.isNullOrEmpty(pigCode)) {
            return;
        }
        doctorPigEventDao.updatePigCode(pigId, pigCode);
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
        DoctorPig pig = doctorPigDao.findById(pigId);
        if (pig == null) {
            throw new ServiceException("pig.not.found");
        }
        if (doctorPigDao.findPigByFarmIdAndPigCodeAndSex(pig.getFarmId(), newCode, DoctorPig.PigSex.SOW.getKey()) != null) {
            throw new ServiceException("新猪号" + newCode + "已存在");
        }
    }
}
