package io.terminus.doctor.event.dao;

import com.google.common.collect.ImmutableMap;
import io.terminus.common.mysql.dao.MyBatisDao;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.ParameterizedType;
import java.util.*;

import static com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Int;

/**
 * Created by IntelliJ IDEA.
 * Author: luoys
 * Date: 11:44 16/8/11
 */
@Repository
public class DoctorMonthlyReportDao{
    @Autowired
    protected SqlSessionTemplate sqlSession;

    public final String nameSpace = "DoctorMonthlyReport";

    protected String sqlId(String id) {
        return this.nameSpace + "." + id;
    }


    /**
     * 预产胎数
     * @param farmId  猪场ID
     * @param startAt 开始时间
     * @param endAt   结束时间
     * @return
     */
    public int getPreDelivery(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("preDeliveryCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 分娩窝数
     * @param farmId
     * @param startAt
     * @param endAt
     * @return
     */
    public int getDelivery(Long farmId, Date startAt, Date endAt){
        return this.sqlSession.selectOne(this.sqlId("deliveryCounts"), ImmutableMap.of("farmId", farmId, "startAt", startAt, "endAt", endAt));

    }
}
