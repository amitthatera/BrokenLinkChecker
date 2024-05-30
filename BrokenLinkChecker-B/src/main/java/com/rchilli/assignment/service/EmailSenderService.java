package com.rchilli.assignment.service;

import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

@Service
public class EmailSenderService {

	@Value("${spring.mail.port}")
	private String emailPort;

	@Value("${spring.mail.host}")
	private String emailHost;

	@Value("${spring.mail.username}")
	private String emailUsername;

	@Value("${spring.mail.password}")
	private String emailPassword;

	private final Logger logger = LoggerFactory.getLogger(BrokenLinkDetectionService.class);

	public void sendEmail(String recipientEmail, String logFile) {
		Properties props = new Properties();
		props.put("mail.smtp.host", emailHost);
		props.put("mail.smtp.port", emailPort);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");

		// Create a Session object with authentication
		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(emailUsername, emailPassword);
			}
		});

		try {
			// Create a MimeMessage object
			MimeMessage message = new MimeMessage(session);

			// Set the recipient email address
			message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));

			// Set the subject
			message.setSubject("Broken Link Detection Logs");

			// Create a multipart message
			MimeMultipart multipart = new MimeMultipart();
			
			MimeBodyPart messagePart = new MimeBodyPart();
			String messageText = "Logs of broken links attached with this email. \nThis is a system-generated email. Please do not reply.";
			messagePart.setText(messageText);
			multipart.addBodyPart(messagePart);


			// Create and add the attachment
			MimeBodyPart attachmentPart = new MimeBodyPart();
			attachmentPart.attachFile(logFile);
			multipart.addBodyPart(attachmentPart);

			// Set the content of the message to the multipart object
			message.setContent(multipart);

			// Send the email
			Transport.send(message);

			logger.info("Email sent successfully!");
		} catch (MessagingException | IOException e) {
			logger.error("Email can not be sent!");
		}
	}
}
