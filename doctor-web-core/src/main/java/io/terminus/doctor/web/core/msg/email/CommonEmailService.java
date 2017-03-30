package io.terminus.doctor.web.core.msg.email;

import com.google.common.base.Throwables;
import io.terminus.common.model.Response;
import io.terminus.common.utils.JsonMapper;
import io.terminus.doctor.common.utils.RespHelper;
import io.terminus.lib.email.EmailException;
import io.terminus.lib.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.codemonkey.simplejavamail.Email;
import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Mailer;
import org.codemonkey.simplejavamail.TransportStrategy;

import javax.mail.Message;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Desc: 普通短信网关实现
 * Mail: houly@terminus.io
 * Data: 下午2:07 16/5/30
 * Author: houly
 */
@Slf4j
public class CommonEmailService implements EmailService {

    private final CommonMailToken commonMailToken;
    protected final ExecutorService emailExecutor;
    protected Mailer mailer;

    public CommonEmailService(String host, Integer port, String account, String password, Integer protocol) {
        this.commonMailToken = CommonMailToken.builder()
                .host(host)
                .port(port)
                .account(account)
                .password(password)
                .protocol(protocol)
                .build();
        this.emailExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

        if (commonMailToken.getProtocol() == 1){
            mailer = new Mailer(commonMailToken.getHost(), commonMailToken.getPort(), commonMailToken.getAccount(), commonMailToken.getPassword(), TransportStrategy.SMTP_SSL);
        } else if (commonMailToken.getProtocol() == 2) {
            mailer = new Mailer(commonMailToken.getHost(), commonMailToken.getPort(), commonMailToken.getAccount(), commonMailToken.getPassword(), TransportStrategy.SMTP_TLS);
        } else {
            mailer = new Mailer(commonMailToken.getHost(), commonMailToken.getPort(), commonMailToken.getAccount(), commonMailToken.getPassword());
        }
    }
    @Override
    public String send(String subject, String content, String toes, String attachments) throws EmailException {
        toes=singleString2JsonFormat(toes);

        List<String> toList= JsonMapper.JSON_NON_EMPTY_MAPPER.fromJson(toes, List.class);

        RespHelper.orServEx(send(subject, content, toList, false));
        return "SUCCESS";
    }



    public Response<Boolean> send(String subject, String content, String to, Boolean checkSubscribe) {
        Response<Boolean> resp = new Response<Boolean>();
        try {
            Email email = buildEmail(subject, content);
            email.addRecipient(to, to, Message.RecipientType.TO);
            return doSendMail(email);
        } catch (Exception e){
            log.error("failed to send email(subject={}, content={}, to={}), cause: {}",
                    subject, content, to, Throwables.getStackTraceAsString(e));
            resp.setError("email.send.fail");
        }
        return resp;
    }

    public Response<Boolean> send(String subject, String content, List<String> toes, Boolean checkSubscribe) {
        Response<Boolean> resp = new Response<Boolean>();
        try {
            Email email = buildEmail(subject, content);
            for (String to : toes){
                email.addRecipient(to, to, Message.RecipientType.TO);
            }
            return doSendMail(email);
        } catch (Exception e){
            log.error("failed to send email(subject={}, content={}, toes={}), cause: {}",
                    subject, content, toes, Throwables.getStackTraceAsString(e));
            resp.setError("email.send.fail");
        }
        return resp;
    }

    /**
     * build an email
     * @param subject email subject
     * @param content email content
     * @return Email object
     */
    private Email buildEmail(String subject, String content) {
        Email email = new Email();
        email.setFromAddress(commonMailToken.getAccount(), commonMailToken.getAccount());
        email.setSubject(subject);
        email.setTextHTML(content);
        return email;
    }

    private Response<Boolean> doSendMail(Email email) {
        try {
            mailer.sendMail(email);
            return Response.ok(Boolean.TRUE);
        } catch (MailException e) {
            log.error("failed to send email({}), cause: {}", email, Throwables.getStackTraceAsString(e));
            return Response.fail(e.getMessage());
        }
    }
    private String singleString2JsonFormat(String from){
        if(!from.contains("[")){
            from="[\""+from+"\"]";
        }
        return from;
    }
}
