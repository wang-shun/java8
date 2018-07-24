package io.terminus.doctor.event.service;

import io.terminus.common.model.Paging;
import io.terminus.common.model.Response;
import io.terminus.doctor.common.utils.RespWithEx;
import io.terminus.doctor.event.dto.DoctorPigInfoDetailDto;
import io.terminus.doctor.event.dto.DoctorPigInfoDto;
import io.terminus.doctor.event.dto.IotPigDto;
import io.terminus.doctor.event.dto.search.DoctorPigCountDto;
import io.terminus.doctor.event.dto.search.SearchedPig;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorChgFarmInfo;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yaoqijun.
 * Date:2016-05-13
 * Email:yaoqj@terminus.io
 * Descirbe:
 */
public interface DoctorPigReadService {

    /**
     * 获取猪场里猪的数量
     * @param farmId 猪场id
     * @param pigSex 性别
     * @return 猪数量
     */
    Response<Long> getPigCount(@NotNull(message = "farmId.not.null") Long farmId, @NotNull(message = "pigSex.not.null") DoctorPig.PigSex pigSex);

    /**
     * 通过pigId 获取对应的详细信息
     * @param pigId
     * @return
     */
    RespWithEx<DoctorPigInfoDetailDto> queryPigDetailInfoByPigId(@NotNull(message = "farmId.not.null") Long farmId, @NotNull(message = "input.pigId.empty") Long pigId, Integer eventSize);


    /**
     * 母猪详情导出
     * @return
     */
    Response<List<Map>> findSowPigDetailExpotr(Long farmId, Long pigId, Integer eventSize);

    /**
     * 通过pigId 查询pig
     * @param pigId
     * @return
     */
    Response<DoctorPig> findPigById(Long pigId);

    /**
     * 通过doctorPig 信息分页查询
     * @param doctorPig
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<DoctorPigInfoDto>> pagingDoctorInfoDtoByPig(DoctorPig doctorPig, Integer pageNo, Integer pageSize);

    /**
     * 公猪的Track 信息分页查询
     * @param doctorPigTrack
     * @param pageNo
     * @param pageSize
     * @return
     */
    Response<Paging<DoctorPigInfoDto>> pagingDoctorInfoDtoByPigTrack(DoctorPigTrack doctorPigTrack, Integer pageNo, Integer pageSize);

    /**
     * 分页查询猪
     */
    Response<Paging<SearchedPig>> pagingPig(Map<String, Object> params, Integer pageNo, Integer pageSize);


    /**
     * 通过pigId 获取对应的 pigDto 信息内容
     * @param pigId
     * @return
     */
    Response<DoctorPigInfoDto> queryDoctorInfoDtoById(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 生成对应的窝号 年+月+胎次
     */
    Response<String> genNest(@NotNull(message = "farmId.not.null") Long farmId,
                             @Nullable String eventAt,
                             @Nullable Integer size);

    /**
     * 获取猪舍pig 信息内容
     * @param barnId
     * @return
     */
    Response<List<DoctorPigInfoDto>> queryDoctorPigInfoByBarnId(@NotNull(message = "input.barnId.empty") Long barnId);

    /**
     * 查询当前猪舍里的猪只跟踪列表(注意是当前猪舍)
     * @param barnId 猪舍id
     * @return 猪只跟踪列表
     */
    Response<List<DoctorPigTrack>> findActivePigTrackByCurrentBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 根据猪场id查询猪列表
     * @param farmId 猪场id
     * @return 猪列表
     */
    Response<List<DoctorPig>> findPigsByFarmId(@NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 获取猪的track信息
     * @param pigId 猪id
     * @return
     */
    Response<DoctorPigTrack> findPigTrackByPigId(@NotNull(message = "input.pigId.empty") Long pigId);

    /**
     * 校验 farmId pigCode 不存在
     * @param farmId
     * @param pigCode
     * @return 不存在则返回true
     */
    Response<Boolean> validatePigCodeByFarmId(Long farmId, String pigCode);

    /**
     * 获取猪舍内的所有猪的猪类
     * @param barnId 猪舍id
     * @return 猪类set
     */
    Response<Set<Integer>> findPigStatusByBarnId(@NotNull(message = "barnId.not.null") Long barnId);

    /**
     * 查询猪所在的猪舍信息
     * @param pigId 猪id
     * @return 猪舍信息
     */
    Response<DoctorBarn> findBarnByPigId(@NotNull(message = "pigId.not.null") Long pigId);

    /**
     * 查询母猪配种次数
     * @param pigId 猪id
     * @return 母猪配种次数
     */
    Response<Integer> getCountOfMating(@NotNull(message = "pigId.not.null")Long pigId);

    /**
     * 查询母猪第一次配种的时间
     * @param farmId 猪场id
     * @param pigId 猪id
     * @return 第一次配种的时间
     */
    Response<Date> getFirstMatingTime(@NotNull(message = "pigId.not.null")Long pigId, @NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 查询某一类或几类猪舍里的猪的数量
     * @param farmId   猪场id
     * @param pigTypes 猪类
     * @return 猪的数量
     */
    Response<Long> getPigCountByBarnPigTypes(@NotNull(message = "farmId.not.null") Long farmId,
                                             @NotEmpty(message = "pigTypes.not.empty") List<Integer> pigTypes);

    /**
     * 获取猪场,各个状态猪的存栏数量
     * @param farmId 猪场id
     * @return
     */
    Response<DoctorPigCountDto> getPigCount(@NotNull(message = "farmId.not.null") Long farmId);

    /**
     * 获取猪的当前状态
     * @param pigIds 猪ids
     * @return 返回的track中只包含pigId 和 status 字段
     */
    Response<List<DoctorPigTrack>> queryCurrentStatus(List<Long> pigIds);

    /**
     * 模糊搜索pigCode猪舍下符合
     * @param barnId 猪舍id
     * @param name 模糊搜索字段
     * @param count 返回前count
     * @return
     */
    Response<List<DoctorPig>> suggestSowPig(Long barnId, String name, Integer count);

    /**
     * 根据pigId获取物联网相关猪信息
     * @param pigId 猪id
     * @return 猪信息
     */
    Response<IotPigDto> getIotPig(Long pigId);

    /**
     * 查询猪舍下未离场的母猪
     * @param barnId
     * @return
     */
    Response<List<DoctorPig>> findUnRemovalPigsBy(Long barnId);

    /**
     * 查询转场记录
     * @param farmId
     * @param pigId
     * @return
     */
    Response<DoctorChgFarmInfo> findByFarmIdAndPigId(Long farmId, Long pigId);

    Response<Paging<SearchedPig>> pagingChgFarmPig(Map<String, Object> params, Integer pageNo, Integer pageSize);


    // -------------------- 新增代码-----------------------
    /**
     * 未转场的母猪
     */
    Response<List<Long>> findNotTransitionsSow(Long farmId,Long barnId,Map<String,Object> valueMap,String pigCode,String rfid,Integer isRemoval);


    /**
     * 已转场的母猪
     */
    Response<List<Long>> findHaveTransitionsSow(Long farmId,Long barnId,String pigCode,String rfid);


    /**
     * 查询猪的信息 By Pigs_Id
     */
    Response<Paging<SearchedPig>> pagesSowPigById(Map<String, Object> params, Integer pageNo, Integer pageSize);

}
