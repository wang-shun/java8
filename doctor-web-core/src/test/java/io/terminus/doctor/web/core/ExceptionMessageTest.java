package io.terminus.doctor.web.core;

import io.terminus.lib.email.EmailException;
import org.junit.Test;

/**
 * Created by zhanghecheng on 16/3/11. <br>
 * Mail: zhanghecheng@terminus.io
 */
public class ExceptionMessageTest {

    private void emailEx(){
        throw new EmailException("eroor");
    }
    @Test
    public void test(){
        try{
            emailEx();
        }catch (EmailException e){
            System.out.println(e.getMessage());
        }
    }
}
