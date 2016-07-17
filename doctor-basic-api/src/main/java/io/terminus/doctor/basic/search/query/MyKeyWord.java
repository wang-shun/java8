package io.terminus.doctor.basic.search.query;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.terminus.search.api.query.Keyword;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Desc: 关键词搜索覆盖
 * Mail: chk@terminus.io
 * Created by IceMimosa
 * Date: 16/7/2
 */
@Data
public class MyKeyWord extends Keyword implements Serializable {

    private static final long serialVersionUID = 4552756085176686755L;

    private List<Field> myFields;
    private boolean multiField;

    public MyKeyWord(String field, String q) {
        super(ImmutableList.of(field), q);
        this.multiField = true;
        this.myFields = Lists.newArrayList();
        setCharacter(field, q);
    }

    @Data
    static class Field implements Serializable {
        private static final long serialVersionUID = -3670776763653590110L;
        private final String field;
        private final Object value;
        private boolean last;
        // 是否是部分匹配查询
        private boolean isWildCard;

        public Field(String field, Object value, Boolean isWildCard) {
            this.field = field;
            this.value = value;
            this.isWildCard = isWildCard;
        }
    }

    private void setCharacter(String field, String q) {
        // 获取所有的英文和中文(其他)
        StringBuilder sb1 = new StringBuilder();
        List<String> list = Lists.newArrayList(); // 存放英文和数字
        StringBuilder sb2 = new StringBuilder(); // 存放中文
        for (char ch : q.toCharArray()) {
            if (String.valueOf(ch).matches("\\w+")) {
                sb1.append(String.valueOf(ch));
            }else {
                sb2.append(String.valueOf(ch));
                // 存储英文和数字
                if (sb1.length() > 0) {
                    list.add(sb1.toString());
                    sb1 = new StringBuilder();
                }
            }
        }
        if (sb1.length() > 0) {
            list.add(sb1.toString());
        }

        // 存储到myFields中
        if (sb2.length() > 0) {
            this.myFields.add(new MyKeyWord.Field(field, sb2.toString(), false)); // 中文
        }
        list.forEach(value -> {
            this.myFields.add(new MyKeyWord.Field(field, "*" + value + "*", true)); // 英文和数字
        });

        // 设置最后一个
        if (this.myFields.size() > 0) {
            Field f = this.myFields.get(this.myFields.size() - 1);
            f.setLast(true);
        }
    }
}
