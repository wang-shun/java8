package io.terminus.doctor.event.search.pig;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Throwables;
import io.terminus.common.utils.BeanMapper;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.constants.JacksonType;
import io.terminus.doctor.event.dao.DoctorPigDao;
import io.terminus.doctor.event.enums.PigStatus;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.event.model.DoctorPigTrack;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Desc: 猪(索引对象)创建工厂
 * Mail: chk@terminus.io
 * Created by icemimosa
 * Date: 16/5/23
 */
@Slf4j
public abstract class BaseIndexedPigFactory<T extends IndexedPig> implements IndexedPigFactory<T> {

    protected DoctorPigDao doctorPigDao;

    protected final Class<T> clazz;

    @SuppressWarnings("all")
    public BaseIndexedPigFactory(DoctorPigDao doctorPigDao) {
        this.doctorPigDao = doctorPigDao;
        final Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType) {
            clazz = ((Class<T>) ((ParameterizedType) genericSuperclass).getActualTypeArguments()[0]);
        } else {
            clazz = ((Class<T>) ((ParameterizedType) getClass().getSuperclass().getGenericSuperclass())
                    .getActualTypeArguments()[0]);
        }
    }

    @Override
    public T create(DoctorPig pig, DoctorPigTrack pigTrack, Object... others) {
        if (pig == null) {
            return null;
        }
        // 1. 处理猪信息
        T indexedPig = BeanMapper.map(pig, clazz);
        DoctorPig.PIG_TYPE pigType = DoctorPig.PIG_TYPE.from(indexedPig.getPigType());
        indexedPig.setPigTypeName(pigType == null ? "" : pigType.getDesc());
        indexedPig.setPigCodeSearch(pig.getPigCode().toLowerCase());

        // 2. 处理猪Track信息
        if (pigTrack != null) {
            indexedPig.setStatus(pigTrack.getStatus());
            PigStatus status = PigStatus.from(pigTrack.getStatus());
            indexedPig.setStatusName(status == null ? "" : status.getDesc());
            indexedPig.setCurrentBarnId(pigTrack.getCurrentBarnId());
            indexedPig.setCurrentBarnName(pigTrack.getCurrentBarnName());
            indexedPig.setWeight(pigTrack.getWeight());
            indexedPig.setOutFarmDate(pigTrack.getOutFarmDate());
            indexedPig.setCurrentParity(pigTrack.getCurrentParity());
            indexedPig.setUpdatedAt(pigTrack.getUpdatedAt());
            indexedPig.setIsRemoval(pigTrack.getIsRemoval());
            if (pigTrack.getExtra() != null) {
                try {
                    Map<String, String> extraMap = JsonMapper.JSON_NON_DEFAULT_MAPPER.getMapper().readValue(pigTrack.getExtra(), JacksonType.MAP_OF_OBJECT);
                    String pregCheckResult = extraMap.get("pregCheckResult");
                    if (StringUtils.isNotBlank(pregCheckResult)) {
                        indexedPig.setPregCheckResult(Integer.parseInt(pregCheckResult));
                    }
                } catch (Exception e){
                    log.error(" indexedPig create failed cause by {}", Throwables.getStackTraceAsString(e));
                }

            }
        }

        return indexedPig;
    }
}
