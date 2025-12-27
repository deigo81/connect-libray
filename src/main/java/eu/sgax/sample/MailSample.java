package eu.sgax.sample;

import eu.sgax.connect.mail.ReadMail;
import eu.sgax.connect.mail.SendMail;
import java.util.List;

/**
 * Email operations example
 * This class demonstrates how to use the Mail connector to send and read emails
 */
public class MailSample {

    public static void main(String[] args) {
        // Email configuration
        String smtpHost = "smtp.gmail.com";
        int smtpPort = 587;
        String imapHost = "imap.gmail.com";
        int imapPort = 993;
        String email = "your@email.com";
        String password = "your-password";

        try {
            // Example 1: Send an email
            System.out.println("--- Send Email Example ---");
            SendMail sendMail = new SendMail(smtpHost, smtpPort, email, password);
            
            SendMail.EmailBuilder emailBuilder = sendMail.new EmailBuilder()
                .to("recipient@example.com")
                .subject("Test Email from Connect Library")
                .body("This is a test email sent using the Connect Library.\n\nBest regards!");
            
            // Optional: Add attachment
            // emailBuilder.attachment("/path/to/attachment.pdf");
            
            System.out.println("Sending email...");
            sendMail.send(emailBuilder);
            System.out.println("Email sent successfully!");

            // Example 2: Read emails
            System.out.println("\n--- Read Emails Example ---");
            ReadMail readMail = new ReadMail(imapHost, imapPort, email, password);
            
            System.out.println("Reading last 10 emails from INBOX...");
            List<ReadMail.EmailMessage> messages = readMail.readEmails("INBOX", 10);
            
            System.out.println("Found " + messages.size() + " emails:");
            for (int i = 0; i < messages.size(); i++) {
                ReadMail.EmailMessage msg = messages.get(i);
                System.out.println("\n--- Email " + (i + 1) + " ---");
                System.out.println("From: " + msg.getFrom());
                System.out.println("Subject: " + msg.getSubject());
                System.out.println("Date: " + msg.getSentDate());
                System.out.println("Has attachments: " + (msg.getAttachments().size() > 0));
                if (msg.getAttachments().size() > 0) {
                    System.out.println("Attachments:");
                    for (ReadMail.AttachmentInfo attachment : msg.getAttachments()) {
                        System.out.println("  - " + attachment.getFileName() + " (" + attachment.getSize() + " bytes)");
                    }
                }
            }

            System.out.println("\nMail operations completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during mail operations: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
