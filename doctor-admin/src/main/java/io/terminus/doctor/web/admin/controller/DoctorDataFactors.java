package io.terminus.doctor.web.admin.controller;

import com.google.api.client.util.Lists;
import com.google.common.collect.Maps;
import io.terminus.boot.rpc.common.annotation.RpcConsumer;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.dto.DataFactorDto;
import io.terminus.doctor.event.model.DoctorDataFactor;
import io.terminus.doctor.event.service.DoctorDataFactorReadService;
import io.terminus.doctor.event.service.DoctorDataFactorWriteService;
import io.terminus.doctor.web.admin.dto.DoctorDataFactorDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Desc: 信用模型计算因子 API
 * Mail: hehaiyang@terminus.io
 * Date: 2017/04/12
 */
@Slf4j
@RestController
@RequestMapping("/api/doctor/admin/data-factor")
public class DoctorDataFactors {

    @RpcConsumer
    private DoctorDataFactorWriteService doctorDataFactorWriteService;

    @RpcConsumer
    private DoctorDataFactorReadService doctorDataFactorReadService;

    /**
     * 查询详情
     * @param id
     * @return
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public DoctorDataFactor findDoctorDataFactor(@PathVariable Long id) {
        Response<DoctorDataFactor> response =  doctorDataFactorReadService.findById(id);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return response.getResult();
    }

    /**
     * 列表
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<DoctorDataFactor> listDoctorDataFactor() {
        Map<String, Object> criteria = Maps.newHashMap();
        Response<List<DoctorDataFactor>> result =  doctorDataFactorReadService.list(criteria);
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        return result.getResult();
    }

    @RequestMapping(value = "/map-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Integer, List<DataFactorDto>> mapListDoctorDataFactor() {
        Map<String, Object> criteria = Maps.newHashMap();
        Response<List<DoctorDataFactor>> result =  doctorDataFactorReadService.list(criteria);
        if(!result.isSuccess()){
            throw new JsonResponseException(result.getError());
        }
        Map<Integer, List<DataFactorDto>> map = Maps.newHashMap();
        for(DoctorDataFactor factor: result.getResult()){
            List<DataFactorDto> list;
            if(map.containsKey(factor.getType())){
                 list = map.get(factor.getType());
            }else{
                 list = Lists.newArrayList();
            }

            DataFactorDto dto = new DataFactorDto();
            dto.setId(factor.getId());
            dto.setType(factor.getType());
            dto.setTypeName(factor.getTypeName());
            dto.setSubType(factor.getSubType());
            dto.setSubTypeName(factor.getSubTypeName());
            dto.setFactor(factor.getFactor());
            if(factor.getRangeFrom()!=null && factor.getRangeFrom() < -999999999){
                dto.setRangeFrom("MIN");
            }else{
                dto.setRangeFrom(String.valueOf(factor.getRangeFrom()));
            }
            if(factor.getRangeTo() != null && factor.getRangeTo() > 999999999){
                dto.setRangeTo("MAX");
            }else{
                dto.setRangeTo(String.valueOf(factor.getRangeTo()));
            }

            list.add(dto);
            List<DataFactorDto> sList = list.stream().sorted(Comparator.comparing(it -> changeTo(it.getRangeFrom()))).collect(Collectors.toList());
            map.put(factor.getType(), sList);
        }
        return map;
    }


    private Double changeTo(String value){
        if("MIN".equals(value)){
            return -Double.MAX_VALUE;
        }else{
            return Double.valueOf(value);
        }
    }

    /**
     * 更新
     * @param
     */
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public Boolean updateDoctorDataFactor(@RequestBody DoctorDataFactorDto datas) {

        List<DoctorDataFactor> factors = Lists.newArrayList();
        for(DataFactorDto dto: datas.getDatas()){
            DoctorDataFactor factor = new DoctorDataFactor();

            factor.setType(dto.getType());
            factor.setTypeName(dto.getTypeName());
            factor.setSubType(dto.getSubType());
            factor.setSubTypeName(dto.getSubTypeName());
            factor.setFactor(dto.getFactor());
            if("MIN".equals(dto.getRangeFrom())){
                factor.setRangeFrom(-Double.MAX_VALUE);
            }else{
                factor.setRangeFrom(Double.valueOf(dto.getRangeFrom()));
            }
            if("MAX".equals(dto.getRangeTo())){
                factor.setRangeTo(Double.MAX_VALUE);
            }else{
                factor.setRangeTo(Double.valueOf(dto.getRangeTo()));
            }
            factors.add(factor);
        }
        Response<Boolean> response = doctorDataFactorWriteService.batchUpdate(factors);
        if (!response.isSuccess()) {
            throw new JsonResponseException(500, response.getError());
        }
        return true;
    }

}