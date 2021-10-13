/**
 * MailerService.java
 *
 * Created on 5. 10. 2021, 20:07:22 by burgetr
 */
package cz.vutbr.fit.layout.web.ejb;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import cz.vutbr.fit.layout.web.data.RepositoryInfo;


/**
 * 
 * @author burgetr
 */
@Stateless
public class MailerService
{
    @Inject
    @ConfigProperty(name = "fitlayout.smtp.host", defaultValue = "localhost")
    String smtpHost;
    
    @Inject
    @ConfigProperty(name = "fitlayout.smtp.port", defaultValue = "25")
    String smtpPort;
    
    @Inject
    @ConfigProperty(name = "fitlayout.smtp.username")
    Optional<String> smtpUsername;
    
    @Inject
    @ConfigProperty(name = "fitlayout.smtp.password")
    Optional<String> smtpPassword;
    
    @Inject
    @ConfigProperty(name = "fitlayout.mail.sender")
    String fromEmail;

    @Inject
    @ConfigProperty(name = "fitlayout.repository.url")
    String repoUrl;
    
    @Inject
    @ConfigProperty(name = "fitlayout.product", defaultValue = "FitLayout")
    String productName;
    

    public void sendRepositoryInfo(String email, List<RepositoryInfo> list) throws MessagingException
    {
        final String subject = productName + " repository list";
        String message = 
                "Dear " + productName + " user,\n\n" +
                "someone (probably you) has requested a list of " + productName + " repositories associated.\n" +
                "with your e-mail address. We provide the list below:\n\n";
        
        for (RepositoryInfo item : list)
        {
            message += repoUrl + item.getId() + '\n';
            if (item.getDescription() != null && !item.getDescription().isEmpty())
                message += item.getDescription() + '\n';
            message += '\n';
        }
        
        message += "\nBest regards\n" + 
                "Your " + productName + " team";
        sendEmail(email, subject, message);
    }
    
    public void sendEmail(String toEmail, String subject, String text) throws MessagingException
    {
        final Message msg = new MimeMessage(createSession());
        msg.setFrom(new InternetAddress(fromEmail));
        InternetAddress[] toAddresses = { new InternetAddress(toEmail) };
        msg.setRecipients(Message.RecipientType.TO, toAddresses);
        msg.setSubject(subject);
        msg.setSentDate(new Date());
        msg.setText(text);
        Transport.send(msg);
    }
    
    private Session createSession()
    {
        Authenticator authenticator = null;
        Properties properties = new Properties();
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", Integer.valueOf(smtpPort));
        if (!smtpUsername.isPresent() || !smtpPassword.isPresent())
        {
            properties.put("mail.smtp.auth", false);
        }
        else
        {
            properties.put("mail.smtp.auth", true);
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory"); 
            //properties.put("mail.smtp.starttls.enable", false);
            authenticator = new Authenticator() {
                private PasswordAuthentication pa = new PasswordAuthentication(smtpUsername.get(), smtpPassword.get());
                @Override
                public PasswordAuthentication getPasswordAuthentication() {
                    return pa;
                }
            };
        }
        Session session = Session.getInstance(properties, authenticator);
        return session;
    }
    
}
