package io.terminus.doctor.event.model;

import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.JsonMapperUtil;
import io.terminus.doctor.event.dto.report.daily.DoctorDailyReportDto;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import static io.terminus.common.utils.Arguments.isEmpty;

/**
 * Desc: 猪场日报表Model类
 * Mail: yangzl@terminus.io
 * author: DreamYoung
 * Date: 2016-07-19
 */
@Data
public class DoctorDailyReport implements Serializable {
    private static final long serialVersionUID = 123812107494777505L;

    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private Long id;
    
    /**
     * 猪场id
     */
    private Long farmId;
    
    /**
     * 猪场名称
     */
    private String farmName;

    /**
     * 母猪存栏
     */
    private int sowCount;

    /**
     * 产房仔猪存栏
     */
    private int farrowCount;

    /**
     * 保育猪存栏
     */
    private int nurseryCount;

    /**
     * 育肥猪存栏
     */
    private int fattenCount;
    
    /**
     * 日报数据，json存储
     * @see DoctorDailyReportDto
     */
    private String data;

    /**
     * 附加字段
     */
    private String extra;
    
    /**
     * 统计时间
     */
    private Date sumAt;
    
    /**
     * 创建时间(仅做记录创建时间，不参与查询)
     */
    private Date createdAt;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private DoctorDailyReportDto reportData;

    public void setReportData(DoctorDailyReportDto reportData) {
        this.reportData = reportData;
        this.data = JSON_MAPPER.toJson(reportData);
    }

    public DoctorDailyReportDto getReportData() {
        if (isEmpty(this.data)) {
            return null;
        }
        return JsonMapperUtil.nonEmptyMapperWithFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                .fromJson(this.data, JSON_MAPPER.createCollectionType(DoctorDailyReportDto.class));
    }
}
