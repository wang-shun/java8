package io.terminus.doctor.event.handler;

import java.util.List;

/**
 * Created by yaoqijun.
 * Date:2016-05-27
 * Email:yaoqj@terminus.io
 * Descirbe: handler chain
 */
public class DoctorEventHandlerChain {

    private List<DoctorEventCreateHandler> doctorEventCreateHandlers;

    public void setDoctorEventCreateHandlers(List<DoctorEventCreateHandler> doctorEventCreateHandlers){
        this.doctorEventCreateHandlers = doctorEventCreateHandlers;
    }

    public List<DoctorEventCreateHandler> getDoctorEventCreateHandlers(){
        return doctorEventCreateHandlers;
    }
}
