package eu.sgax.connect.mail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

/**
 * Clase para enviar correos electrónicos a través de SMTP.
 * Proporciona métodos para enviar correos simples, con adjuntos, HTML y con CC/BCC.
 * Soporta autenticación SSL y TLS.
 * 
 * @author SGAX
 * @version 1.0
 */
public class SendMail {

    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final boolean useSSL;
    private final boolean useTLS;
    private Session session;

    /**
     * Constructor con configuración completa.
     * 
     * @param smtpHost dirección del servidor SMTP
     * @param smtpPort puerto del servidor SMTP
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     * @param useSSL true para usar SSL
     * @param useTLS true para usar TLS
     */
    public SendMail(String smtpHost, int smtpPort, String username, String password, boolean useSSL, boolean useTLS) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
        this.useTLS = useTLS;
        initSession();
    }

    /**
     * Constructor simplificado con TLS por defecto.
     * 
     * @param smtpHost dirección del servidor SMTP
     * @param smtpPort puerto del servidor SMTP
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     */
    public SendMail(String smtpHost, int smtpPort, String username, String password) {
        this(smtpHost, smtpPort, username, password, false, true);
    }

    /**
     * Constructor con puerto por defecto (587 para TLS).
     * 
     * @param smtpHost dirección del servidor SMTP
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     */
    public SendMail(String smtpHost, String username, String password) {
        this(smtpHost, 587, username, password, false, true);
    }

    // Inicializar sesión de correo
    private void initSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");

        if (useSSL) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(smtpPort));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }

        if (useTLS) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    /**
     * Envía un correo electrónico simple.
     * 
     * @param to dirección de correo del destinatario
     * @param subject asunto del correo
     * @param body cuerpo del correo en texto plano
     * @throws MessagingException si ocurre un error al enviar
     * @throws IOException si ocurre un error de I/O
     */
    public void sendEmail(String to, String subject, String body) throws MessagingException, IOException {
        sendEmail(to, null, null, subject, body, null, false);
    }

    /**
     * Envía un correo con opciones de CC y BCC.
     * 
     * @param to dirección de correo del destinatario
     * @param cc dirección(es) para cópia (puede ser null)
     * @param bcc dirección(es) para cópia oculta (puede ser null)
     * @param subject asunto del correo
     * @param body cuerpo del correo en texto plano
     * @throws MessagingException si ocurre un error al enviar
     * @throws IOException si ocurre un error de I/O
     */
    public void sendEmail(String to, String cc, String bcc, String subject, String body) throws MessagingException, IOException {
        sendEmail(to, cc, bcc, subject, body, null, false);
    }

    /**
     * Envía un correo con adjuntos.
     * 
     * @param to dirección de correo del destinatario
     * @param subject asunto del correo
     * @param body cuerpo del correo en texto plano
     * @param attachments lista de rutas de archivos adjuntos
     * @throws MessagingException si ocurre un error al enviar
     * @throws IOException si ocurre un error de I/O
     */
    public void sendEmailWithAttachments(String to, String subject, String body, List<Path> attachments) throws MessagingException, IOException {
        sendEmail(to, null, null, subject, body, attachments, false);
    }

    /**
     * Envía un correo con contenido HTML.
     * 
     * @param to dirección de correo del destinatario
     * @param subject asunto del correo
     * @param htmlBody cuerpo del correo en HTML
     * @throws MessagingException si ocurre un error al enviar
     * @throws IOException si ocurre un error de I/O
     */
    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException, IOException {
        sendEmail(to, null, null, subject, htmlBody, null, true);
    }

    // Método principal para enviar correo con todas las opciones
    public void sendEmail(String to, String cc, String bcc, String subject, String body, List<Path> attachments, boolean isHtml) 
            throws MessagingException, IOException {
        
        Message message = new MimeMessage(session);
        
        // Remitente
        message.setFrom(new InternetAddress(username));
        
        // Destinatarios principales
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        
        // CC (Con Copia)
        if (cc != null && !cc.isEmpty()) {
            message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
        }
        
        // BCC (Con Copia Oculta / CCO)
        if (bcc != null && !bcc.isEmpty()) {
            message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(bcc));
        }
        
        // Asunto
        message.setSubject(subject);
        
        // Fecha
        message.setSentDate(new Date());
        
        // Cuerpo del mensaje
        if (attachments == null || attachments.isEmpty()) {
            // Sin adjuntos
            if (isHtml) {
                message.setContent(body, "text/html; charset=utf-8");
            } else {
                message.setText(body);
            }
        } else {
            // Con adjuntos
            Multipart multipart = new MimeMultipart();
            
            // Parte del texto
            MimeBodyPart textPart = new MimeBodyPart();
            if (isHtml) {
                textPart.setContent(body, "text/html; charset=utf-8");
            } else {
                textPart.setText(body);
            }
            multipart.addBodyPart(textPart);
            
            // Adjuntar archivos
            for (Path attachment : attachments) {
                if (Files.exists(attachment)) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(attachment.toFile());
                    attachmentPart.setDataHandler(new DataHandler(source));
                    attachmentPart.setFileName(attachment.getFileName().toString());
                    multipart.addBodyPart(attachmentPart);
                }
            }
            
            message.setContent(multipart);
        }
        
        // Enviar
        Transport.send(message);
    }

    // Enviar correo con múltiples destinatarios
    public void sendEmailToMultiple(List<String> toList, String subject, String body) throws MessagingException, IOException {
        String to = String.join(",", toList);
        sendEmail(to, null, null, subject, body, null, false);
    }

    // Builder para construcción fluida de correos
    public static class EmailBuilder {
        private String to;
        private String cc;
        private String bcc;
        private String subject;
        private String body;
        private boolean isHtml = false;
        private List<Path> attachments = new ArrayList<>();
        private SendMail sender;

        public EmailBuilder(SendMail sender) {
            this.sender = sender;
        }

        public EmailBuilder to(String to) {
            this.to = to;
            return this;
        }

        public EmailBuilder cc(String cc) {
            this.cc = cc;
            return this;
        }

        public EmailBuilder bcc(String bcc) {
            this.bcc = bcc;
            return this;
        }

        public EmailBuilder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public EmailBuilder body(String body) {
            this.body = body;
            return this;
        }

        public EmailBuilder htmlBody(String htmlBody) {
            this.body = htmlBody;
            this.isHtml = true;
            return this;
        }

        public EmailBuilder attach(Path attachment) {
            this.attachments.add(attachment);
            return this;
        }

        public EmailBuilder attachAll(List<Path> attachments) {
            this.attachments.addAll(attachments);
            return this;
        }

        public void send() throws MessagingException, IOException {
            if (to == null || to.isEmpty()) {
                throw new MessagingException("El destinatario (to) es obligatorio");
            }
            if (subject == null) {
                subject = "(Sin asunto)";
            }
            if (body == null) {
                body = "";
            }
            sender.sendEmail(to, cc, bcc, subject, body, attachments, isHtml);
        }
    }

    // Crear un builder para construcción fluida
    public EmailBuilder newEmail() {
        return new EmailBuilder(this);
    }

    // Obtener la sesión de correo
    public Session getSession() {
        return session;
    }
}
