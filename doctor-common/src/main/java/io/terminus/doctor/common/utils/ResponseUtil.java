package io.terminus.doctor.common.utils;

import io.terminus.common.model.Response;

import java.util.List;
import java.util.Map;

public class ResponseUtil<T> extends Response {

    private List<Map> farms;

    /*public static <T> Response<T> ok(T data) {
        Response<T> resp = new Response();
        resp.setResult(data);
        return resp;
    }*/

    public static <T> ResponseUtil isOk(T data,List farms){
        ResponseUtil resp = new ResponseUtil();
        resp.setResult(data);
        resp.setFarms(farms);
        return resp;
    }

    public static <T> ResponseUtil isFail(String error){
        ResponseUtil<T> resp = new ResponseUtil();
        resp.setError(error);
        return resp;
    }


    public List<Map> getFarms() {
        return farms;
    }

    public void setFarms(List<Map> farms) {
        this.farms = farms;
    }
}
