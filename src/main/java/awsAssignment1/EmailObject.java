package awsAssignment1;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

public class EmailObject {
	
    private static String SENDER = "jonybravo1604@gmail.com"; // Update with your sender email
    private static String RECIPIENT = "jonybravo1604@gmail.com"; // Update with recipient email
    private static String SUBJECT = "S3 Object Attachment";

    private static String BODY_TEXT = "Hello,\r\n" + "Please see the attached s3 Object.";

    private static String BODY_HTML = "<html><head></head><body><h1>Hello!</h1><p>Please see the attached s3 Object.</p></body></html>";

    public void mail(String path) {	
        try {
            Session session = Session.getDefaultInstance(new Properties());

            MimeMessage message = new MimeMessage(session);
            message.setSubject(SUBJECT, "UTF-8");
            message.setFrom(new InternetAddress(SENDER));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENT));

            MimeMultipart msgBody = new MimeMultipart("alternative");

            MimeBodyPart wrap = new MimeBodyPart();

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setContent(BODY_TEXT, "text/plain; charset=UTF-8");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(BODY_HTML, "text/html; charset=UTF-8");

            msgBody.addBodyPart(textPart);
            msgBody.addBodyPart(htmlPart);

            wrap.setContent(msgBody);

            MimeMultipart msg = new MimeMultipart("mixed");
            message.setContent(msg);
            msg.addBodyPart(wrap);

            // Download the attachment content from the URL
            URL url;
            try {
                URI uri = new URI(path);
                url = uri.toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                throw new RuntimeException("Invalid URL: " + path, e);
            }
            
            try (InputStream inputStream = url.openStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                byte[] attachmentData = outputStream.toByteArray(); // Your attachment data here
                ByteArrayDataSource bds = new ByteArrayDataSource(attachmentData, "application/octet-stream");
               
                // Create a body part for the attachment
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.setDataHandler(new DataHandler(bds));
                attachmentPart.setFileName("Book1.csv");

                msg.addBodyPart(attachmentPart);
            }

            // Send the email
            AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard()
            		.withRegion("ap-south-1").build();
            ByteArrayOutputStream messageOutputStream = new ByteArrayOutputStream();
            message.writeTo(messageOutputStream);
            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(messageOutputStream.toByteArray()));
            SendRawEmailRequest rawEmailRequest = new SendRawEmailRequest(rawMessage);
            client.sendRawEmail(rawEmailRequest);

            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("Email Failed");
            System.err.println("Error message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
