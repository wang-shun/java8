package io.terminus.doctor.open.common;

import com.github.cage.Cage;
import com.github.cage.image.EffectConfig;
import com.github.cage.image.Painter;
import com.github.cage.token.RandomCharacterGeneratorFactory;
import com.github.cage.token.RandomTokenGenerator;

import javax.servlet.http.HttpSession;
import java.util.Random;

import static com.github.cage.token.RandomCharacterGeneratorFactory.ARABIC_NUMERALS;


/**
 * Mail: xiao@terminus.io <br>
 * Date: 2016-03-15 6:15 PM  <br>
 * Author: xiao
 */
public class CaptchaGenerator {
    public static final String CAPTCHA_TOKEN = "captchaToken";

    private final Cage cage;

    public CaptchaGenerator() {
        Painter painter = new Painter(150, 70, null, null, new EffectConfig(true, true, true, true, null), null);
        RandomTokenGenerator tokenGenerator = new RandomTokenGenerator(null, new RandomCharacterGeneratorFactory(ARABIC_NUMERALS, null, new Random()), 4, 0);
        cage = new Cage(painter, null, null, null, Cage.DEFAULT_COMPRESS_RATIO, tokenGenerator, null);
    }

    /**
     * 生成验证码到HttpSession中
     * @param session HttpSession
     * @return 图片字节码数组
     */
    public byte[] captcha(HttpSession session) {
        // create the text for the image
        String token = cage.getTokenGenerator().next();
        session.setAttribute(CAPTCHA_TOKEN, token);
        return cage.draw(token);
    }

    public byte[] captcha(String token) {
        return cage.draw(token);
    }


    /**
     * 获取HttpSession中的验证码字符串
     * @param session HttpSession
     * @return HttpSession中验证码字符串
     */
    public String getGeneratedKey(HttpSession session) {
        String token = (String) session.getAttribute(CAPTCHA_TOKEN);
        //when token retrieved ,it should be removed
        session.removeAttribute(CAPTCHA_TOKEN);
        return token;
    }

}
