package io.terminus.doctor.workflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Builder;

import java.io.Serializable;

/**
 * 用于封装流程节点中的timer 字段
 * Created by xiao on 16/8/11.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FlowTimer implements Serializable{

    private static final long serialVersionUID = -1962422458604599286L;

    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;
    private int second;

    public FlowTimer(String timer) {
        String[] strings = timer.trim().split("\\s+");
        for (int i = 0; strings != null && i < strings.length; i++) {
            int value = Integer.parseInt(strings[i]);
            switch (i) {
                case 0:
                    this.second = value;
                    break;
                case 1:
                    this.minute = value;
                    break;
                case 2:
                    this.hour = value;
                    break;
                case 3:
                    this.day = value;
                    break;
                case 4:
                    this.month = value;
                    break;
                case 5:
                    this.year = value;
                    break;
                default:
                    break;
            }
        }
    }


}
