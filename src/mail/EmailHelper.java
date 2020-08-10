package mail;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class EmailHelper extends Thread {
	private static String mail = "nvtanh4vipm@gmail.com";
	private static String pass = "vipmember";
	private String to, subject, body;

	public EmailHelper(String to, String subject, String body) {
		this.to = to;
		this.subject = subject;
		this.body = body;
	}

	@Override
	public void run() {
		super.run();
		sendMail();
	}

	private void sendMail() {
		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", "smtp.gmail.com");
		props.put("mail.smtp.port", "587");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(mail, pass);
			}
		});

		try {
			MimeMessage message = new MimeMessage(session);
			message.setHeader("Content-Type", "text/plain; charset=UTF-8");
			message.setFrom(new InternetAddress(mail));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			message.setSubject(this.subject, "UTF-8");
			message.setText(this.body, "UTF-8");
			Transport.send(message);
			System.out.println("sent mail successfully...");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}


}
