package io.terminus.doctor.workflow.updateData;

import io.terminus.doctor.workflow.base.BaseServiceTest;
import io.terminus.doctor.workflow.core.WorkFlowService;
import io.terminus.doctor.workflow.dao.FlowHistoryProcessDao;
import io.terminus.doctor.workflow.dao.FlowProcessTrackDao;
import io.terminus.doctor.workflow.model.FlowInstance;
import io.terminus.doctor.workflow.model.FlowProcess;
import io.terminus.doctor.workflow.model.FlowProcessTrack;
import io.terminus.doctor.workflow.utils.BeanHelper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;

import java.util.List;

/**
 * Created by xiao on 16/8/8.
 */
public class UpdateData extends BaseServiceTest {
    @Autowired
    private WorkFlowService workFlowService ;
    @Autowired
    private FlowProcessTrackDao flowProcessTrackDao;
    @Autowired
    private FlowHistoryProcessDao flowHistoryProcessDao;

    @Test
    @Rollback(false)
    public void UpdateData(){
        workFlowService.updateData("sow",60l);
    }

    @Test
    @Rollback(false)
    public void UpdateDataTrack(){
        List flowHistoryProcesses = flowHistoryProcessDao.paging(100,100).getData();
        List flowInstanceTracks = flowProcessTrackDao.paging(100,100).getData();
        for (int i = 0; i < flowInstanceTracks.size(); i++) {
            BeanHelper.copy(flowInstanceTracks.get(i),flowHistoryProcesses.get(i));
            flowProcessTrackDao.update((FlowProcessTrack) flowInstanceTracks.get(i));
        }

    }
}
