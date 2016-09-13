package io.terminus.doctor.event.service;

import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dao.DoctorGroupBatchSummaryDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * Desc: 猪群批次总结表读服务实现类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-09-13
 */
@Slf4j
@Service
@RpcProvider
public class DoctorGroupBatchSummaryReadServiceImpl implements DoctorGroupBatchSummaryReadService {

    private final DoctorGroupEventDao doctorGroupEventDao;
    private final DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao;

    @Autowired
    public DoctorGroupBatchSummaryReadServiceImpl(DoctorGroupEventDao doctorGroupEventDao,
                                                  DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao) {
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupBatchSummaryDao = doctorGroupBatchSummaryDao;
    }

    @Override
    public Response<DoctorGroupBatchSummary> findGroupBatchSummaryById(Long groupBatchSummaryId) {
        try {
            return Response.ok(doctorGroupBatchSummaryDao.findById(groupBatchSummaryId));
        } catch (Exception e) {
            log.error("find groupBatchSummary by id failed, groupBatchSummaryId:{}, cause:{}", groupBatchSummaryId, Throwables.getStackTraceAsString(e));
            return Response.fail("groupBatchSummary.find.fail");
        }
    }

    /**
     * 通过猪群明细实时获取批次总结
     *
     * @param groupDetail 猪群明细
     * @return 猪群批次总结
     */
    @Override
    public Response<DoctorGroupBatchSummary> getSummaryByGroupDetail(DoctorGroupDetail groupDetail) {
        try {

            return Response.ok();
        } catch (Exception e) {
            log.error("failed, cause:{}", Throwables.getStackTraceAsString(e));
            return Response.fail("");
        }
    }

    //根据猪群与猪群跟踪实时计算批次总结
    private DoctorGroupBatchSummary getSummary(DoctorGroup group, DoctorGroupTrack groupTrack) {
        List<DoctorGroupEvent> events = doctorGroupEventDao.findByGroupId(group.getId());

        //转入
        int inCount = CountUtil.intStream(events, DoctorGroupEvent::getQuantity, event -> Objects.equals(GroupEventType.MOVE_IN.getValue(), event.getType())).sum();
        double inAvgWeight = CountUtil.doubleStream(events, DoctorGroupEvent::getAvgWeight, event -> Objects.equals(GroupEventType.MOVE_IN.getValue(), event.getType())).average().orElse(0D);

        DoctorGroupBatchSummary summary = new DoctorGroupBatchSummary();
        summary.setFarmId(group.getFarmId());                                        //猪场id
        summary.setGroupId(group.getId());                                           //猪群id
        summary.setGroupCode(group.getGroupCode());                                  //猪群号
        summary.setPigType(group.getPigType());                                      //猪类
        summary.setAvgDayAge(groupTrack.getAvgDayAge());                             //平均日龄
        summary.setOpenAt(group.getOpenAt());                                        //建群时间
        summary.setCloseAt(group.getCloseAt());                                      //关群时间
        summary.setBarnId(group.getCurrentBarnId());                                 //猪舍id
        summary.setBarnName(group.getCurrentBarnName());                             //猪舍名称
        summary.setUserId(group.getStaffId());                                       //工作人员id
        summary.setUserName(group.getStaffName());                                   //工作人员name
        summary.setLiveCount(groupTrack.getQuantity());                              //活仔数
        summary.setWeakCount(groupTrack.getWeakQty());                               //弱仔数
        summary.setHealthCount(groupTrack.getQuantity() - summary.getHealthCount()); //健仔数
        summary.setBirthAvgWeight(groupTrack.getBirthAvgWeight());                   //出生均重(kg)
        summary.setWeanCount(groupTrack.getQuantity() - groupTrack.getUnweanQty());  //断奶数 = 总 - 未断奶数
        summary.setUnqCount(groupTrack.getUnqQty());                                 //不合格数
        summary.setWeanAvgWeight(groupTrack.getWeanAvgWeight());                     //断奶均重(kg)
        summary.setSaleCount(getSaleCount(events));                                  //销售头数
        summary.setSaleAmount(getSaleAmount(events));                                //销售金额(分)
        summary.setInCount(inCount);                                                 //转入数
        summary.setInAvgWeight(inAvgWeight);                                         //转入均重(kg)
        summary.setDeadRate(getDeatRate(events, inCount));                           //死淘率
        summary.setNest(0);                                                          // TODO: 16/9/13  窝数
        summary.setFcr(0D);                                                          // TODO: 16/9/13  料肉比

        setToNurseryOrFatten(summary, events);                                       //阶段转
        // TODO: 16/9/13 上线后再弄
//        summary.setToFattenCost();         //转育肥成本(分)
//        summary.setToNurseryCost();        //转保育成本(分)
//        summary.setBirthCost();            //出生成本(分)
//        summary.setInCost();               //转入成本(均)
//        summary.setOutCost();              //出栏成本(分)
        return summary;
    }

    //获取死淘率
    private static double getDeatRate(List<DoctorGroupEvent> events, int inCount) {
        int deadCount = CountUtil.intStream(events, DoctorGroupEvent::getQuantity,
                event -> Objects.equals(GroupEventType.CHANGE.getValue(), event.getType()) &&
                        (Objects.equals(DoctorBasicEnums.DEAD.getId(), event.getChangeTypeId()) ||
                                Objects.equals(DoctorBasicEnums.ELIMINATE.getId(), event.getChangeTypeId())))
                .sum();

        return deadCount / inCount == 0 ? 1 : inCount;
    }

    //获取销售数量
    private static int getSaleCount(List<DoctorGroupEvent> events) {
        return CountUtil.intStream(events, DoctorGroupEvent::getQuantity,
                event -> Objects.equals(GroupEventType.CHANGE.getValue(), event.getType()) &&
                        Objects.equals(DoctorBasicEnums.SALE.getId(), event.getChangeTypeId()))
                .sum();
    }

    //获取销售金额
    private static long getSaleAmount(List<DoctorGroupEvent> events) {
        return CountUtil.longStream(events, DoctorGroupEvent::getAmount,
                event -> Objects.equals(GroupEventType.CHANGE.getValue(), event.getType()) &&
                        Objects.equals(DoctorBasicEnums.SALE.getId(), event.getChangeTypeId()))
                .sum();
    }

    //阶段转
    private void setToNurseryOrFatten(DoctorGroupBatchSummary summary, List<DoctorGroupEvent> events) {
        int toCount = CountUtil.intStream(events, DoctorGroupEvent::getQuantity,
                event -> Objects.equals(event.getType(), GroupEventType.TRANS_GROUP.getValue()) &&
                        Objects.equals(event.getTransGroupType(), DoctorGroupEvent.TransGroupType.OUT.getValue()))
                .sum();
        double toAvgWeight = CountUtil.doubleStream(events, DoctorGroupEvent::getAvgWeight,
                event -> Objects.equals(event.getType(), GroupEventType.TRANS_GROUP.getValue()) &&
                        Objects.equals(event.getTransGroupType(), DoctorGroupEvent.TransGroupType.OUT.getValue()))
                .average().orElse(0D);

        //产房仔猪 => 保育猪
        if (PigType.FARROW_TYPES.contains(summary.getPigType())) {
            summary.setToNurseryCount(toCount);       //转保育数量
            summary.setToNurseryAvgWeight(toAvgWeight);   //转保育均重(kg)
        }

        //保育猪 => 育肥猪
        if (Objects.equals(summary.getPigType(), PigType.NURSERY_PIGLET.getValue())) {
            summary.setToFattenCount(toCount);        //转育肥数量
            summary.setToFattenAvgWeight(toAvgWeight);    //转育肥均重(kg)
        }
    }
}
