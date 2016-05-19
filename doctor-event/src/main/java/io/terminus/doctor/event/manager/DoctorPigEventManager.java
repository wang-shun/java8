package io.terminus.doctor.event.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Created by yaoqijun.
 * Date:2016-05-19
 * Email:yaoqj@terminus.io
 * Descirbe: 母猪事件信息录入管理过程
 */
@Component
@Slf4j
public class DoctorPigEventManager {

    /**
     * 进厂事件
     * @return
     */
    public Boolean entrySowFarmEvent(){
        return null;
    }

    /**
     * 公猪进厂事件
     * @return
     */
    public Boolean entryBoarFarmEvent(){
        return null;
    }

    /**
     * 公猪采精事件
     * @return
     */
    public Boolean boarSnmenEvent(){
        return null;
    }

    public Boolean diseaseEvent(){
        return null;
    }

    public Boolean vaccinationEvent(){
        return null;
    }

    /**
     * 体况信息检查
     * @return
     */
    public Boolean conditionEvent(){
        return null;
    }

    /**
     * 转舍事件信息
     * @return
     */
    public Boolean chgLocationEvent(){
        return null;
    }

    /**
     * 转舍事件信息
     * @return
     */
    public Boolean chgFarmEvent(){
        return null;
    }

    /**
     * 离场事件信息
     * @return
     */
    public Boolean removalEvent(){
        return null;
    }

    /**
     * 配种事件
     * @return
     */
    public Boolean  matingEvent(){
        return null;
    }
}
