package io.terminus.doctor.move.builder.pig;

import io.terminus.doctor.basic.model.DoctorBasic;
import io.terminus.doctor.event.dto.event.BasePigEventInputDto;
import io.terminus.doctor.event.dto.event.usual.DoctorFarmEntryDto;
import io.terminus.doctor.event.enums.BoarEntryType;
import io.terminus.doctor.event.model.DoctorBarn;
import io.terminus.doctor.event.model.DoctorPig;
import io.terminus.doctor.move.builder.DoctorBuilderCommonOperation;
import io.terminus.doctor.move.dto.DoctorMoveBasicData;
import io.terminus.doctor.move.model.View_EventListBoar;
import io.terminus.doctor.move.model.View_EventListPig;
import io.terminus.doctor.move.model.View_EventListSow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

/**
 * Created by xjn on 17/8/4.
 * 进场
 */
@Component
public class DoctorEntryInputBuilder implements DoctorPigEventInputBuilder {
    @Autowired
    private DoctorBuilderCommonOperation builderCommonOperation;

    @Override
    public BasePigEventInputDto buildFromMove(DoctorMoveBasicData moveBasicData,
                                              View_EventListPig pigRawEvent) {
        if (Objects.equals(pigRawEvent.getPigSex(), DoctorPig.PigSex.SOW.getKey())) {
            return buildSowEntryInput(moveBasicData, pigRawEvent);
        }
        return buildBoarEntryInput(moveBasicData, pigRawEvent);
    }

    private BasePigEventInputDto buildSowEntryInput(DoctorMoveBasicData moveBasicData,
                                                    View_EventListPig pigRawEvent) {
        View_EventListSow event = (View_EventListSow) pigRawEvent;
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();

        DoctorFarmEntryDto entry = new DoctorFarmEntryDto();
        builderCommonOperation.fillPigEventCommonInputFromMove(entry, moveBasicData, pigRawEvent);

        entry.setPigOutId(event.getPigOutId());
        entry.setEarCode(event.getPigCode());                        //耳号取猪号
        entry.setParity(event.getParity());                          //当前事件胎次
        entry.setLeft(event.getLeftCount());
        entry.setRight(event.getRightCount());
        entry.setPigType(DoctorPig.PigSex.SOW.getKey());             //类型: 母猪
        entry.setPigCode(event.getPigCode());                        // pig code 猪 编号
        entry.setBirthday(event.getBirthDate());                     // 猪生日
        entry.setInFarmDate(event.getInFarmDate());                  // 进厂时间
        entry.setFatherCode(event.getPigFatherCode());               // 父类Code （非必填）
        entry.setMotherCode(event.getPigMotherCode());               // 母Code （非必填）
        entry.setEntryMark(event.getRemark());                       // 非必填
        entry.setSource(event.getSource());

        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(event.getBreed());
        entry.setBreed(breed == null ? null : breed.getId());         //品种Id （basic Info）
        entry.setBreedName(event.getBreed());                         //品种名称
        DoctorBasic gene = basicMap.get(DoctorBasic.Type.GENETICS.getValue()).get(event.getGenetic());
        entry.setBreedType(gene == null ? null : gene.getId());       //品系Id  (basic info)
        entry.setBreedTypeName(event.getGenetic());                   //品系名称

        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        if (barn != null) {
            entry.setBarnId(barn.getId());
            entry.setBarnName(barn.getName());
        }
        return entry;
    }

    private BasePigEventInputDto buildBoarEntryInput(DoctorMoveBasicData moveBasicData,
                                                     View_EventListPig pigRawEvent) {
        View_EventListBoar event = (View_EventListBoar) pigRawEvent;
        Map<String, DoctorBarn> barnMap = moveBasicData.getBarnMap();
        Map<Integer, Map<String, DoctorBasic>> basicMap = moveBasicData.getBasicMap();

        DoctorFarmEntryDto entry = new DoctorFarmEntryDto();
        builderCommonOperation.fillPigEventCommonInputFromMove(entry, moveBasicData, pigRawEvent);

        entry.setPigOutId(event.getPigOutId());
        BoarEntryType type = BoarEntryType.from(event.getBoarType());
        entry.setBoarType(type == null ? null : type.getKey());
        entry.setBoarTypeName(event.getBoarType());
        entry.setPigType(DoctorPig.PigSex.BOAR.getKey());  //类型: 公猪
        entry.setPigCode(event.getPigCode());       // pig code 猪 编号
        entry.setBirthday(event.getBirthDate());      // 猪生日
        entry.setInFarmDate(event.getInFarmDate());    // 进厂时间
        entry.setFatherCode(event.getPigFatherCode());    // 父类Code （非必填）
        entry.setMotherCode(event.getPigMotherCode());    // 母Code （非必填）
        entry.setEntryMark(event.getRemark());     // 非必填
        entry.setSource(event.getSource());

        DoctorBarn barn = barnMap.get(event.getBarnOutId());
        if (barn != null) {
            entry.setBarnId(barn.getId());
            entry.setBarnName(barn.getName());
        }
        //品种 品系
        DoctorBasic breed = basicMap.get(DoctorBasic.Type.BREED.getValue()).get(event.getBreed());
        entry.setBreed(breed == null ? null : breed.getId());         //品种Id （basic Info）
        entry.setBreedName(event.getBreed());     //品种名称

        DoctorBasic gene = basicMap.get(DoctorBasic.Type.GENETICS.getValue()).get(event.getGenetic());
        entry.setBreedType(gene == null ? null : gene.getId());     //品系Id  (basic info)
        entry.setBreedTypeName(event.getGenetic()); //品系名称
        return entry;
    }
}
