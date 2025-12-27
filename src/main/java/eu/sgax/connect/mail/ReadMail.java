package eu.sgax.connect.mail;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.search.FlagTerm;

/**
 * Clase para leer correos electrónicos desde servidores IMAP o POP3.
 * Proporciona métodos para conectar, desconectar, listar y descargar mensajes y adjuntos.
 * 
 * @author SGAX
 * @version 1.0
 */
public class ReadMail {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final boolean useSSL;
    private final String protocol; // "imap" o "pop3"
    private Store store;
    private Folder inbox;

    /**
     * Constructor con configuración completa.
     * 
     * @param host dirección del servidor IMAP o POP3
     * @param port puerto del servidor
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     * @param useSSL true para usar SSL
     * @param protocol protocolo a usar ("imap" o "pop3")
     */
    public ReadMail(String host, int port, String username, String password, boolean useSSL, String protocol) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.useSSL = useSSL;
        this.protocol = protocol.toLowerCase();
    }

    /**
     * Constructor simplificado con SSL por defecto e IMAP.
     * 
     * @param host dirección del servidor
     * @param port puerto del servidor
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     */
    public ReadMail(String host, int port, String username, String password) {
        this(host, port, username, password, true, "imap");
    }

    /**
     * Constructor con puerto por defecto (993 para IMAP SSL).
     * 
     * @param host dirección del servidor
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     */
    public ReadMail(String host, String username, String password) {
        this(host, 993, username, password, true, "imap");
    }

    /**
     * Factoría estática para crear una instancia configurada para POP3.
     * 
     * @param host dirección del servidor POP3
     * @param port puerto del servidor
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     * @param useSSL true para usar SSL
     * @return nueva instancia configurada para POP3
     */
    public static ReadMail forPop3(String host, int port, String username, String password, boolean useSSL) {
        return new ReadMail(host, port, username, password, useSSL, "pop3");
    }

    /**
     * Factoría estática para crear una instancia configurada para POP3 con puerto por defecto (995).
     * 
     * @param host dirección del servidor POP3
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     * @return nueva instancia configurada para POP3 con SSL
     */
    public static ReadMail forPop3(String host, String username, String password) {
        return new ReadMail(host, 995, username, password, true, "pop3");
    }

    /**
     * Conecta al servidor IMAP o POP3 y abre la carpeta INBOX.
     * 
     * @throws MessagingException si ocurre un error de conexión
     */
    public void connect() throws MessagingException {
        Properties props = new Properties();
        
        String protocolPrefix = "mail." + protocol;
        
        if (useSSL) {
            props.put(protocolPrefix + ".ssl.enable", "true");
            props.put(protocolPrefix + ".ssl.trust", host);
        } else {
            props.put(protocolPrefix + ".starttls.enable", "true");
        }
        
        props.put(protocolPrefix + ".host", host);
        props.put(protocolPrefix + ".port", String.valueOf(port));
        props.put(protocolPrefix + ".auth", "true");

        Session session = Session.getInstance(props);
        store = session.getStore(protocol);
        store.connect(host, username, password);
        
        // Abrir carpeta INBOX en modo solo lectura (no borra mensajes)
        inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);
    }

    /**
     * Desconecta del servidor cerrando la carpeta y la tienda de correos.
     * 
     * @throws MessagingException si ocurre un error al desconectar
     */
    public void disconnect() throws MessagingException {
        if (inbox != null && inbox.isOpen()) {
            inbox.close(false);
        }
        if (store != null && store.isConnected()) {
            store.close();
        }
    }

    // Verificar si está conectado
    public boolean isConnected() {
        return store != null && store.isConnected() && inbox != null && inbox.isOpen();
    }

    // Obtener todos los mensajes
    public List<EmailMessage> getMessages() throws MessagingException, IOException {
        return getMessages(-1);
    }

    // Obtener últimos N mensajes
    public List<EmailMessage> getMessages(int maxMessages) throws MessagingException, IOException {
        if (!isConnected()) {
            throw new MessagingException("No conectado al servidor IMAP.");
        }

        Message[] messages = inbox.getMessages();
        List<EmailMessage> emailList = new ArrayList<>();

        int start = maxMessages > 0 ? Math.max(0, messages.length - maxMessages) : 0;
        
        for (int i = start; i < messages.length; i++) {
            emailList.add(parseMessage(messages[i]));
        }

        return emailList;
    }

    // Obtener solo mensajes no leídos
    public List<EmailMessage> getUnreadMessages() throws MessagingException, IOException {
        if (!isConnected()) {
            throw new MessagingException("No conectado al servidor IMAP.");
        }

        Flags seen = new Flags(Flags.Flag.SEEN);
        FlagTerm unseenFlagTerm = new FlagTerm(seen, false);
        Message[] messages = inbox.search(unseenFlagTerm);
        
        List<EmailMessage> emailList = new ArrayList<>();
        for (Message message : messages) {
            emailList.add(parseMessage(message));
        }

        return emailList;
    }

    // Parsear mensaje a EmailMessage
    private EmailMessage parseMessage(Message message) throws MessagingException, IOException {
        EmailMessage email = new EmailMessage();
        email.messageNumber = message.getMessageNumber();
        email.from = message.getFrom()[0].toString();
        email.subject = message.getSubject();
        email.sentDate = message.getSentDate();
        email.receivedDate = message.getReceivedDate();
        email.isRead = message.isSet(Flags.Flag.SEEN);
        
        // Parsear contenido y adjuntos
        parseContent(message, email);
        
        return email;
    }

    // Parsear contenido del mensaje y adjuntos
    private void parseContent(Part part, EmailMessage email) throws MessagingException, IOException {
        String contentType = part.getContentType();
        
        if (part.isMimeType("text/plain") && email.textBody == null) {
            email.textBody = part.getContent().toString();
        } else if (part.isMimeType("text/html") && email.htmlBody == null) {
            email.htmlBody = part.getContent().toString();
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                
                String disposition = bodyPart.getDisposition();
                if (disposition != null && (disposition.equalsIgnoreCase(Part.ATTACHMENT) || 
                                           disposition.equalsIgnoreCase(Part.INLINE))) {
                    // Es un adjunto
                    AttachmentInfo attachment = new AttachmentInfo();
                    attachment.fileName = bodyPart.getFileName();
                    attachment.contentType = bodyPart.getContentType();
                    attachment.size = bodyPart.getSize();
                    attachment.bodyPart = (MimeBodyPart) bodyPart;
                    email.attachments.add(attachment);
                } else {
                    // Continuar parseando recursivamente
                    parseContent(bodyPart, email);
                }
            }
        }
    }

    // Descargar adjunto a un archivo
    public void downloadAttachment(AttachmentInfo attachment, Path destinationPath) throws IOException, MessagingException {
        if (attachment.bodyPart == null) {
            throw new IOException("Adjunto no válido o no disponible");
        }

        // Crear directorios padres si no existen
        if (destinationPath.getParent() != null) {
            Files.createDirectories(destinationPath.getParent());
        }

        try (InputStream inputStream = attachment.bodyPart.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(destinationPath.toFile())) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    // Descargar todos los adjuntos de un mensaje
    public void downloadAllAttachments(EmailMessage email, Path destinationDir) throws IOException, MessagingException {
        Files.createDirectories(destinationDir);
        
        for (AttachmentInfo attachment : email.attachments) {
            Path filePath = destinationDir.resolve(attachment.fileName);
            downloadAttachment(attachment, filePath);
        }
    }

    // Cambiar a otra carpeta
    public void openFolder(String folderName) throws MessagingException {
        if (inbox != null && inbox.isOpen()) {
            inbox.close(false);
        }
        inbox = store.getFolder(folderName);
        inbox.open(Folder.READ_ONLY);
    }

    // Listar carpetas disponibles
    public List<String> listFolders() throws MessagingException {
        if (store == null || !store.isConnected()) {
            throw new MessagingException("No conectado al servidor IMAP.");
        }
        
        Folder[] folders = store.getDefaultFolder().list("*");
        List<String> folderNames = new ArrayList<>();
        for (Folder folder : folders) {
            folderNames.add(folder.getFullName());
        }
        return folderNames;
    }

    // Obtener el total de mensajes
    public int getMessageCount() throws MessagingException {
        if (!isConnected()) {
            throw new MessagingException("No conectado al servidor IMAP.");
        }
        return inbox.getMessageCount();
    }

    // Obtener mensajes no leídos (contador)
    public int getUnreadMessageCount() throws MessagingException {
        if (!isConnected()) {
            throw new MessagingException("No conectado al servidor IMAP.");
        }
        return inbox.getUnreadMessageCount();
    }

    // Clase para representar un mensaje de correo
    public static class EmailMessage {
        public int messageNumber;
        public String from;
        public String subject;
        public Date sentDate;
        public Date receivedDate;
        public String textBody;
        public String htmlBody;
        public boolean isRead;
        public List<AttachmentInfo> attachments = new ArrayList<>();

        @Override
        public String toString() {
            return "EmailMessage{" +
                    "messageNumber=" + messageNumber +
                    ", from='" + from + '\'' +
                    ", subject='" + subject + '\'' +
                    ", sentDate=" + sentDate +
                    ", isRead=" + isRead +
                    ", attachments=" + attachments.size() +
                    '}';
        }
    }

    // Clase para información de adjuntos
    public static class AttachmentInfo {
        public String fileName;
        public String contentType;
        public int size;
        private MimeBodyPart bodyPart;

        @Override
        public String toString() {
            return "AttachmentInfo{" +
                    "fileName='" + fileName + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", size=" + size +
                    '}';
        }
    }
}
