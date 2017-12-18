package io.terminus.doctor.event.service;

import com.google.common.collect.Lists;
import io.terminus.common.model.Response;
import io.terminus.doctor.event.test.BaseServiceTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by xjn on 17/12/12.
 * email:xiaojiannan@terminus.io
 */
public class DoctorDailyReportV2ServiceTest extends BaseServiceTest {
    @Autowired
    private DoctorDailyReportV2Service doctorDailyReportV2Service;

    @Test
    public void flushGroupDaily() {
        Response<Boolean> response = doctorDailyReportV2Service.flushGroupDaily(1L,"2017-01-01", "2017-01-02");
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.getResult());
    }

    @Test
    public void flushGroupDailyFroType() {
        Response<Boolean> response = doctorDailyReportV2Service.flushGroupDaily(1L, 2,
                "2017-01-01", "2017-01-02");
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.getResult());

    }

    @Test
    public void flushPigDaily() {
        Response<Boolean> response = doctorDailyReportV2Service.flushPigDaily(8L,"2016-07-12", "2016-07-20");
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.getResult());
    }

    @Test
    public void generateYesterdayAndToday() {
        List<Long> farmIds = Lists.newArrayList(
        1L, 2L, 8L, 16L, 18L, 19L, 20L, 21L, 25L, 30L, 31L, 32L, 33L, 34L, 35L, 44L, 45L, 57L, 60L, 62L, 67L, 68L, 70L, 71L, 72L, 74L, 75L, 79L, 80L, 81L, 82L, 84L, 86L, 87L, 92L, 94L, 95L, 96L, 97L, 98L, 99L, 103L, 105L, 118L, 119L, 126L, 133L, 134L, 138L, 139L, 142L, 145L, 146L, 154L, 155L, 156L, 158L, 159L, 160L, 161L, 163L, 164L, 165L, 175L, 177L, 178L, 184L, 185L, 186L, 187L, 188L, 189L, 190L, 191L, 195L, 198L, 200L, 202L, 205L, 211L, 212L, 214L, 215L, 220L, 222L, 224L, 230L, 231L, 245L, 247L, 248L, 249L, 250L, 252L, 257L, 260L, 261L, 262L, 264L, 265L, 266L, 274L, 276L, 278L, 279L, 280L, 281L, 284L, 285L, 288L, 291L, 294L, 295L, 297L, 301L, 303L, 310L, 311L, 313L, 316L, 318L, 323L, 324L, 327L, 333L, 336L, 338L, 341L, 344L, 346L, 347L, 362L, 363L, 364L, 366L, 367L, 374L, 376L, 377L, 378L, 379L, 381L, 382L, 383L, 386L, 390L, 391L, 392L, 393L, 394L, 395L, 396L, 398L, 400L, 403L, 404L, 407L, 411L, 418L, 422L, 423L, 426L, 431L, 440L, 445L, 447L, 448L, 460L, 470L, 471L, 478L, 479L, 481L, 484L, 488L, 494L, 497L, 504L, 506L, 507L, 513L, 516L, 532L, 545L, 547L, 555L, 556L, 557L, 559L, 562L, 568L, 575L, 577L, 578L, 581L, 582L, 588L, 593L, 595L, 596L, 597L, 601L, 603L, 617L, 621L, 622L, 623L, 624L, 626L, 631L, 643L, 645L, 648L, 650L, 651L, 652L, 654L, 656L, 658L, 659L, 672L, 674L, 675L, 679L, 681L, 684L, 685L, 692L, 701L, 713L, 718L, 719L, 720L, 722L, 723L, 724L, 725L, 726L, 729L, 734L, 735L, 736L, 745L, 748L, 751L, 753L, 756L, 760L, 762L, 764L, 766L, 767L, 768L, 775L, 787L, 790L, 791L, 793L, 812L);
        Response<Boolean> response = doctorDailyReportV2Service.generateYesterdayAndToday(farmIds);
        Assert.assertTrue(response.isSuccess());
        Assert.assertTrue(response.getResult());

    }
}
