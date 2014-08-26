package com.daqi.tools.mailsender;

import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.daqi.tools.toolunit.Converter;
import com.daqi.tools.toolunit.Logger;
  
/**  
* 简单邮件（不带附件的邮件）发送器  
*/   
public class SimpleMailSender  {   

	private Message mMailMessage;
	private String mConfigFile;
	
	public SimpleMailSender(String configFile) {
		mConfigFile = configFile;
		MailConfigLoader configLoader = new MailConfigLoader(mConfigFile);
		initMailInfo(configLoader);
	}
	
	public void doPrepareWork() {
		MailConfigLoader configLoader = new MailConfigLoader(mConfigFile);
		configLoader.checkBaseConfig();
//		initMailInfo(configLoader);
	}
	
	private Properties createProperties(String host, String port, String validate) {
		Properties p = new Properties();
		p.put("mail.smtp.host", host);
		p.put("mail.smtp.port", port);
		p.put("mail.smtp.auth", validate);
		return p;
	}
	
	private Address[] createAddressFromStandardString(String addressStr) {
		Address[] addresses = null;
		if (addressStr != null && !addressStr.equals("")) {
			String[] addressArray = addressStr.trim().split(",");
			final int count = addressArray.length;
			addresses = new Address[count];
			for (int i = 0; i < count; i++) {
				try {
					// 创建邮件的接收者地址，并设置到邮件消息中
					addresses[i] = new InternetAddress(addressArray[i]);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return addresses;
	}
	
	private void initMailInfo(MailConfigLoader configLoader) {
		
		String host = configLoader.getProperty(MailConfigLoader.MAIL_KEY_HOST);
		String user = configLoader.getProperty(MailConfigLoader.MAIL_KEY_USER);
		String tmpPD = configLoader.getProperty(MailConfigLoader.MAIL_KEY_PASSWD);
		String passwd = Converter.decryptBASE64(tmpPD);
		configLoader.updateProperty(MailConfigLoader.MAIL_KEY_PASSWD, Converter.encryptBASE64(passwd));
		
		String personal = configLoader.getProperty(MailConfigLoader.MAIL_FROM_NAME);
		if (personal != null && !personal.equals("")) {
			personal = Converter.suppoetTozh(personal);
		}
		
		String port = configLoader.getProperty(MailConfigLoader.MAIL_PORT, "25");
		String validate = configLoader.getProperty(MailConfigLoader.MAIL_VALIDATE, "true");
		
		// 判断是否需要身份认证
		SimpleAuthenticator authenticator = null;
		// 如果需要身份认证，则创建一个密码验证器
		if (validate.equals("true")) {
			authenticator = new SimpleAuthenticator(user, passwd);
		}
		
		// 根据邮件会话属性和密码验证器构造一个发送邮件的session
		Properties pro = createProperties(host, port, validate);
		Session sendMailSession = Session.getDefaultInstance(pro, authenticator);
		try {
			// 根据session创建一个邮件消息
			mMailMessage = new MimeMessage(sendMailSession);
			// 创建邮件发送者地址
			Address from = new InternetAddress(user, personal);
			// 设置邮件消息的发送者
			mMailMessage.setFrom(from);
			
		} catch (Exception ex) {
			Logger.println("", ex);
		}
		
	}
	
	public void updateMailToAndCc(String to, String cc) {
		Address[] receiverAddress = createAddressFromStandardString(to);
		if (receiverAddress == null) {
			//如果没有设置收件人，则直接发送给发件人
			try {
				receiverAddress = mMailMessage.getFrom();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (receiverAddress != null) {
			try {
				mMailMessage.setRecipients(Message.RecipientType.TO, receiverAddress);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Address[] ccReceiverAddress = createAddressFromStandardString(cc);
		if (ccReceiverAddress != null) {
			try {
				mMailMessage.setRecipients(Message.RecipientType.CC, ccReceiverAddress);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	private void updateMailInfo(String subject, String content) {
		try {
			// 设置邮件消息的主题   
			mMailMessage.setSubject(subject);
			// 设置邮件消息发送的时间   
			mMailMessage.setSentDate(new Date());   
			// MiniMultipart类是一个容器类，包含MimeBodyPart类型的对象   
			Multipart mainPart = new MimeMultipart();   
			// 创建一个包含HTML内容的MimeBodyPart   
			BodyPart html = new MimeBodyPart();   
			// 设置HTML内容   
			html.setContent(content, "text/html; charset=utf-8");   
			mainPart.addBodyPart(html);   
			// 将MiniMultipart对象设置为邮件内容   
			mMailMessage.setContent(mainPart);   
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendHtmlMail(String content) {
		String subject = "subject";
		try {
			subject = mMailMessage.getSubject();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sendHtmlMail(subject, content);
	}
	
	public void sendHtmlMail(String subject, String content) {

		updateMailInfo(subject, content);
		
	     try {
			Transport.send(mMailMessage);
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 简单的内部类
	 * @author huyong
	 *
	 */
	public class SimpleAuthenticator extends Authenticator{  
	    String userName=null;  
	    String password=null;  
	       
	    public SimpleAuthenticator(String username, String password) {   
	        this.userName = username;   
	        this.password = password;   
	    }   
	    
	    protected PasswordAuthentication getPasswordAuthentication(){  
	        return new PasswordAuthentication(userName, password);  
	    }  
	}  
	   
	
	public static void main(String[] args) {
		new SimpleMailSender("monitor_golauncher.property").sendHtmlMail("subject", "content");
	}
	
}  