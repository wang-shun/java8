import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class test {
    @Test
    public void test() {
        String date="2017-1";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        try {
            Date dd = sdf.parse(date);
            System.out.println(dd);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
