package io.terminus.doctor.event.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.terminus.common.exception.JsonResponseException;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.event.dao.DoctorDataFactorDao;
import io.terminus.doctor.event.model.DoctorDataFactor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Desc:
 * Mail: hehaiyang@terminus.io
 * Date: 2017/4/15
 */
@Slf4j
@Component
public class FactorManager {

    private final DoctorDataFactorDao doctorDataFactorDao;

    private static ObjectMapper objectMapper = JsonMapper.nonDefaultMapper().getMapper();

    @Autowired
    public FactorManager(DoctorDataFactorDao doctorDataFactorDao) {
        this.doctorDataFactorDao = doctorDataFactorDao;
    }

    /**
     * 设置信用因子
     * @return
     */
    @Transactional
    public Boolean updateFactors(List<DoctorDataFactor> datas) {
        for(DoctorDataFactor factor: datas) {
            rangeVerify(factor);
            if(factor.getRangeFrom()>factor.getRangeTo()){
                throw new JsonResponseException(500,"range.from.gt.to");
            }
            if (factor.getId() == null){
                factor.setIsDelete(0);
                doctorDataFactorDao.create(factor);
            }else{
                doctorDataFactorDao.update(factor);
            }
        }
        return Boolean.TRUE;
    }

    private void rangeVerify(DoctorDataFactor factor){
        if(factor.getRangeFrom() == null && factor.getRangeTo() == null){
            factor.setRangeFrom(factor.getFactor());
            factor.setRangeTo(factor.getRangeTo());
        }else if(factor.getRangeFrom() == null){
            factor.setRangeFrom(Double.MIN_VALUE);
        }else if(factor.getRangeTo() == null){
            factor.setRangeFrom(Double.MAX_VALUE);
        }
    }


}
