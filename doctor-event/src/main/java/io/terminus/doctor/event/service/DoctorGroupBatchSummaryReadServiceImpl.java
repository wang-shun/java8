package io.terminus.doctor.event.service;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;
import io.terminus.boot.rpc.common.annotation.RpcProvider;
import io.terminus.common.exception.ServiceException;
import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.enums.PigType;
import io.terminus.doctor.common.utils.CountUtil;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.doctor.event.constants.DoctorBasicEnums;
import io.terminus.doctor.event.dao.DoctorGroupBatchSummaryDao;
import io.terminus.doctor.event.dao.DoctorGroupEventDao;
import io.terminus.doctor.event.dao.DoctorKpiDao;
import io.terminus.doctor.event.dto.DoctorGroupDetail;
import io.terminus.doctor.event.dto.DoctorGroupSearchDto;
import io.terminus.doctor.event.enums.GroupEventType;
import io.terminus.doctor.event.model.DoctorGroup;
import io.terminus.doctor.event.model.DoctorGroupBatchSummary;
import io.terminus.doctor.event.model.DoctorGroupEvent;
import io.terminus.doctor.event.model.DoctorGroupTrack;
import io.terminus.doctor.event.util.EventUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.terminus.common.utils.Arguments.notEmpty;

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
    private final DoctorGroupReadService doctorGroupReadService;
    private final DoctorKpiDao doctorKpiDao;

    @Autowired
    public DoctorGroupBatchSummaryReadServiceImpl(DoctorGroupEventDao doctorGroupEventDao,
                                                  DoctorGroupBatchSummaryDao doctorGroupBatchSummaryDao,
                                                  DoctorGroupReadService doctorGroupReadService,
                                                  DoctorKpiDao doctorKpiDao) {
        this.doctorGroupEventDao = doctorGroupEventDao;
        this.doctorGroupBatchSummaryDao = doctorGroupBatchSummaryDao;
        this.doctorGroupReadService = doctorGroupReadService;
        this.doctorKpiDao = doctorKpiDao;
    }

    /**
     * 通过猪群明细实时获取批次总结
     *
     * @param groupDetail 猪群明细
     * @return 猪群批次总结
     */
    @Override
    public Response<DoctorGroupBatchSummary> getSummaryByGroupDetail(DoctorGroupDetail groupDetail, Double fcrFeed) {
        try {
            //最后一次转群后, 会把统计结果插入数据库, 如果没查到, 实时计算
            DoctorGroupBatchSummary summary = doctorGroupBatchSummaryDao.findByGroupId(groupDetail.getGroup().getId());
            return Response.ok(summary != null ? summary : getSummary(groupDetail.getGroup(), groupDetail.getGroupTrack(), fcrFeed));
        } catch (Exception e) {
            log.error("get group batch summary by group detail failed, groupDetail:{}, cause:{}", groupDetail, Throwables.getStackTraceAsString(e));
            return Response.fail("group.batch.summary.find.fail");
        }
    }

    @Override
    public Response<Paging<DoctorGroupBatchSummary>> pagingGroupBatchSummary(DoctorGroupSearchDto searchDto, Integer pageNo, Integer pageSize) {
        try {
            Paging<DoctorGroupDetail> groupPaging = RespHelper.orServEx(doctorGroupReadService.pagingGroup(searchDto, pageNo, pageSize));
            Paging<DoctorGroupBatchSummary> summaryPaging = new Paging<>();
            summaryPaging.setTotal(groupPaging.getTotal());
            summaryPaging.setData(groupPaging.getData().stream().map(g -> RespHelper.orServEx(getSummaryByGroupDetail(g, null))).collect(Collectors.toList()));
            return Response.ok(summaryPaging);
        } catch (ServiceException e) {
            return Response.fail(e.getMessage());
        } catch (Exception e) {
            log.error("paging group batch summary failed, searchDto:{}, pageNo:{}, pageSize:{}, cause:{}",
                    searchDto, pageNo, pageSize, Throwables.getStackTraceAsString(e));
            return Response.fail("group.batch.summary.find.fail");
        }
    }

    @Override
    public Response<DoctorGroupBatchSummary> getGroupBatchSummary(DoctorGroup group, DoctorGroupTrack groupTrack, Double fcrFeed) {
        try {
            return Response.ok(getSummary(group, groupTrack, fcrFeed));
        } catch (Exception e) {
            log.error("get group batch summary failed, group:{}, groupTrack:{}, fcrFeed:{}, cause:{}",
                    group, groupTrack, fcrFeed, Throwables.getStackTraceAsString(e));
            return Response.fail("group.batch.summary.find.fail");
        }
    }

    //根据猪群与猪群跟踪实时计算批次总结
    private DoctorGroupBatchSummary getSummary(DoctorGroup group, DoctorGroupTrack groupTrack, Double fcrFeed) {
        List<DoctorGroupEvent> events = doctorGroupEventDao.findByGroupId(group.getId());

        //转入 // TODO: 2016/12/8 目前是全部转入事件
        int inCount = CountUtil.intStream(events, DoctorGroupEvent::getQuantity, event -> Objects.equals(GroupEventType.MOVE_IN.getValue(), event.getType())).sum();
        double inAvgWeight = CountUtil.doubleStream(events, DoctorGroupEvent::getAvgWeight, event -> Objects.equals(GroupEventType.MOVE_IN.getValue(), event.getType())).average().orElse(0D);

        DoctorGroupBatchSummary summary = new DoctorGroupBatchSummary();
        summary.setFarmId(group.getFarmId());                                        //猪场id
        summary.setGroupId(group.getId());                                           //猪群id
        summary.setGroupCode(group.getGroupCode());                                  //猪群号
        summary.setStatus(group.getStatus());
        summary.setPigType(group.getPigType());                                      //猪类
        summary.setAvgDayAge(groupTrack.getAvgDayAge());                             //平均日龄
        summary.setOpenAt(group.getOpenAt());                                        //建群时间
        summary.setCloseAt(group.getCloseAt());                                      //关群时间
        summary.setBarnId(group.getCurrentBarnId());                                 //猪舍id
        summary.setBarnName(group.getCurrentBarnName());                             //猪舍名称
        summary.setUserId(group.getStaffId());                                       //工作人员id
        summary.setUserName(group.getStaffName());                                   //工作人员name


        summary.setNest(groupTrack.getNest());                                       //窝数
        summary.setLiveCount(groupTrack.getLiveQty());                               //活仔数
        summary.setHealthCount(groupTrack.getHealthyQty());                          //健仔数
        summary.setWeakCount(groupTrack.getWeakQty());                               //弱仔数
        summary.setBirthAvgWeight(EventUtil.get2(EventUtil.getAvgWeight(groupTrack.getBirthWeight(), groupTrack.getLiveQty())));//出生均重(kg)
        summary.setDeadRate(EventUtil.get4(doctorKpiDao.getDeadRateByGroupId(group.getId())));       //死淘率
        summary.setWeanCount(groupTrack.getWeanQty());                               //断奶数
        summary.setUnqCount(groupTrack.getQuaQty());                                 //注意：合格数(需求变更，只需要合格数了，这里翻一下)
        summary.setWeanAvgWeight(EventUtil.get2(EventUtil.getAvgWeight(groupTrack.getWeanWeight(), groupTrack.getWeanQty())));  //断奶均重(kg)
        summary.setSaleCount(getSaleCount(events));                                  //销售头数
        summary.setSaleAmount(getSaleAmount(events));                                //销售金额(分)
        summary.setInCount(inCount);                                                 //转入数
        summary.setInAvgWeight(EventUtil.get2(inAvgWeight));                                         //转入均重(kg)

        Double fcrWeight = getFcrDeltaWeight(events, inCount, inAvgWeight);
        if (fcrFeed == null) {
            summary.setFcr(0D);              //料肉比(这里只放分母，由上层算出物料的分子，出一下)2
        } else {
            summary.setFcr(fcrFeed / fcrWeight);
        }
        Double gain = EventUtil.get2(EventUtil.getAvgWeight(fcrWeight, groupTrack.getAvgDayAge() - getFirstMoveInEvent(events)));
        summary.setDailyWeightGain(gain < 0 ? 0 : gain);//日增重(kg)
        setToNurseryOrFatten(summary, events);                                       //阶段转

        // TODO: 16/9/13 上线后再弄
//        summary.setToFattenCost();         //转育肥成本(分)
//        summary.setToNurseryCost();        //转保育成本(分)
//        summary.setBirthCost();            //出生成本(分)
//        summary.setInCost();               //转入成本(均)
//        summary.setOutCost();              //出栏成本(分)
        return summary;
    }

    private int getFirstMoveInEvent(List<DoctorGroupEvent> events) {
        DoctorGroupEvent event = events.stream()
                .filter(e -> Objects.equals(e.getType(), GroupEventType.MOVE_IN.getValue()))
                .sorted(Comparator.comparing(DoctorGroupEvent::getId)).findFirst()
                .orElse(null);
        return event == null ? 0 : event.getAvgDayAge();
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
        //产房仔猪 => 保育猪
        if (PigType.FARROW_TYPES.contains(summary.getPigType())) {
            summary.setToNurseryCount(CountUtil.intStream(events, DoctorGroupEvent::getQuantity, 
                    event -> isToNursery(event.getType(), event.getOtherBarnType())).sum());       //转保育数量
            
            double toWeight = CountUtil.doubleStream(events, event -> event.getQuantity() * event.getAvgWeight(), 
                    event -> isToNursery(event.getType(), event.getOtherBarnType())).sum();
            summary.setToNurseryAvgWeight(EventUtil.get2(EventUtil.getAvgWeight(toWeight, summary.getToNurseryCount())));   //转保育均重(kg)
        }

        //保育猪 => 育肥猪
        if (Objects.equals(summary.getPigType(), PigType.NURSERY_PIGLET.getValue())) {
            summary.setToFattenCount(CountUtil.intStream(events, DoctorGroupEvent::getQuantity,
                    event -> isToFatten(event.getType(), event.getOtherBarnType())).sum());        //转育肥数量

            double toWeight = CountUtil.doubleStream(events, event -> event.getQuantity() * event.getAvgWeight(),
                    event -> isToFatten(event.getType(), event.getOtherBarnType())).sum();
            summary.setToFattenAvgWeight(EventUtil.get2(EventUtil.getAvgWeight(toWeight, summary.getToNurseryCount())));    //转育肥均重(kg)
        }
    }
    
    private static boolean isToNursery(Integer eventType, Integer toPigType) {
        return Objects.equals(eventType, GroupEventType.TRANS_GROUP.getValue()) 
                && Objects.equals(toPigType, PigType.NURSERY_PIGLET.getValue());
    }

    private static boolean isToFatten(Integer eventType, Integer toPigType) {
        return Objects.equals(eventType, GroupEventType.TRANS_GROUP.getValue())
                && Objects.equals(toPigType, PigType.FATTEN_PIG.getValue());
    }
    
    //获取料肉比增重: 增重 = 转出重 - 转入重
    private static double getFcrDeltaWeight(List<DoctorGroupEvent> events, int inCount, double inAvgWeight) {
        if (!notEmpty(events)) {
            return 0D;
        }
        double outWeight = CountUtil.doubleStream(events, event -> MoreObjects.firstNonNull(event.getWeight(), 0D),
                event -> Objects.equals(event.getType(), GroupEventType.TRANS_GROUP.getValue()) ||  //转群
                        Objects.equals(event.getType(), GroupEventType.TRANS_FARM.getValue()) ||    //转场
                        Objects.equals(event.getType(), GroupEventType.TURN_SEED.getValue()) ||     //转种猪
                        Objects.equals(event.getType(), GroupEventType.CHANGE.getValue()))          //变动
                .sum();
        double delta = outWeight - inCount * inAvgWeight;
        return delta <= 0D ? 1D : delta;
    }
}
