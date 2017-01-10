package io.terminus.doctor.basic.handler;

import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

/**
 * Created by yaoqijun.
 * Date:2016-05-30
 * Email:yaoqj@terminus.io
 * Descirbe: ihandler 不同数据表的处理方式
 */
public class DoctorWareHouseHandlerChain {

    private List<IHandler> handlerList;

    public DoctorWareHouseHandlerChain(List<IHandler> iHandlers){
        this.handlerList = iHandlers;
    }

    public void setHandlerList(List<IHandler> iHandlers) {
        if (isNull(iHandlers)) {
            this.handlerList = Collections.emptyList();
        } else {
            this.handlerList = iHandlers;
        }
    }

    public List<IHandler> getHandlerList(){
        if(isNull(handlerList)){
            return Collections.emptyList();
        }
        return handlerList;
    }
}
