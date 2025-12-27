package eu.sgax.connect.sftp;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Clase para manejar conexiones SFTP (SSH File Transfer Protocol).
 * Proporciona métodos para conectar, desconectar, listar, crear, renombrar y eliminar archivos y directorios.
 * Soporta autenticación por contraseña y por clave privada.
 * 
 * @author SGAX
 * @version 1.0
 */
public class SFTPConnect {

    private final JSch jsch;
    private Session session;
    private ChannelSftp sftpChannel;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private String privateKeyPath;
    private boolean connected = false;

    /**
     * Constructor con credenciales de usuario y contraseña.
     * 
     * @param host dirección del servidor SFTP
     * @param port puerto del servidor SFTP (típicamente 22)
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     */
    public SFTPConnect(String host, int port, String username, String password) {
        this.jsch = new JSch();
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    /**
     * Constructor con puerto por defecto (22).
     * 
     * @param host dirección del servidor SFTP
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     */
    public SFTPConnect(String host, String username, String password) {
        this(host, 22, username, password);
    }

    /**
     * Constructor con autenticación por clave privada.
     * 
     * @param host dirección del servidor SFTP
     * @param port puerto del servidor SFTP
     * @param username nombre de usuario para autenticación
     * @param privateKeyPath ruta a la clave privada
     * @param passphrase contraseña de la clave privada (puede ser null)
     * @throws JSchException si ocurre un error al cargar la clave
     */
    public SFTPConnect(String host, int port, String username, String privateKeyPath, String passphrase) throws JSchException {
        this.jsch = new JSch();
        this.host = host;
        this.port = port;
        this.username = username;
        this.privateKeyPath = privateKeyPath;
        if (passphrase != null && !passphrase.isEmpty()) {
            jsch.addIdentity(privateKeyPath, passphrase);
        } else {
            jsch.addIdentity(privateKeyPath);
        }
        this.password = null;
    }

    /**
     * Conecta al servidor SFTP.
     * 
     * @throws JSchException si ocurre un error de conexión
     */
    public void connect() throws JSchException {
        if (connected) {
            return;
        }

        session = jsch.getSession(username, host, port);
        
        if (password != null) {
            session.setPassword(password);
        }

        // Configuración para evitar verificación estricta de host
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);

        session.connect();
        sftpChannel = (ChannelSftp) session.openChannel("sftp");
        sftpChannel.connect();
        connected = true;
    }

    /**
     * Desconecta del servidor SFTP de manera segura.
     */
    public void disconnect() {
        if (sftpChannel != null && sftpChannel.isConnected()) {
            sftpChannel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        connected = false;
    }

    /**
     * Obtiene el canal SFTP subyacente para operaciones avanzadas.
     * 
     * @return el objeto ChannelSftp
     */
    public ChannelSftp getChannel() {
        return sftpChannel;
    }

    /**
     * Verifica si está conectado al servidor SFTP.
     * 
     * @return true si está conectado, false en caso contrario
     */
    public boolean isConnected() {
        return connected && sftpChannel != null && sftpChannel.isConnected();
    }

    // Listar archivos en el directorio actual o especificado
    public List<String> listFiles() throws SftpException {
        return listFiles(".");
    }

    @SuppressWarnings("unchecked")
    public List<String> listFiles(String remotePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        Vector<ChannelSftp.LsEntry> files = sftpChannel.ls(remotePath);
        List<String> fileNames = new ArrayList<>();
        for (ChannelSftp.LsEntry entry : files) {
            if (!entry.getAttrs().isDir()) {
                String name = entry.getFilename();
                if (!name.equals(".") && !name.equals("..")) {
                    fileNames.add(name);
                }
            }
        }
        return fileNames;
    }

    // Listar directorios en el directorio actual o especificado
    public List<String> listDirectories() throws SftpException {
        return listDirectories(".");
    }

    @SuppressWarnings("unchecked")
    public List<String> listDirectories(String remotePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        Vector<ChannelSftp.LsEntry> files = sftpChannel.ls(remotePath);
        List<String> dirNames = new ArrayList<>();
        for (ChannelSftp.LsEntry entry : files) {
            if (entry.getAttrs().isDir()) {
                String name = entry.getFilename();
                if (!name.equals(".") && !name.equals("..")) {
                    dirNames.add(name);
                }
            }
        }
        return dirNames;
    }

    // Crear un directorio
    public void createDirectory(String remotePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        sftpChannel.mkdir(remotePath);
    }

    // Borrar un archivo
    public void deleteFile(String remoteFilePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        sftpChannel.rm(remoteFilePath);
    }

    // Borrar un directorio
    public void deleteDirectory(String remotePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        sftpChannel.rmdir(remotePath);
    }

    // Renombrar un archivo o directorio
    public void rename(String fromPath, String toPath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        sftpChannel.rename(fromPath, toPath);
    }

    // Cambiar el directorio de trabajo
    public void changeWorkingDirectory(String remotePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        sftpChannel.cd(remotePath);
    }

    // Obtener el directorio de trabajo actual
    public String getCurrentDirectory() throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        return sftpChannel.pwd();
    }

    // Verificar si un archivo o directorio existe
    @SuppressWarnings("unchecked")
    public boolean exists(String remotePath) {
        if (!connected) {
            return false;
        }
        try {
            // Intentar obtener atributos del archivo
            sftpChannel.lstat(remotePath);
            return true;
        } catch (SftpException e) {
            return false;
        }
    }

    // Verificar si es un archivo
    public boolean isFile(String remotePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        return !sftpChannel.lstat(remotePath).isDir();
    }

    // Verificar si es un directorio
    public boolean isDirectory(String remotePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        return sftpChannel.lstat(remotePath).isDir();
    }

    // Obtener el tamaño de un archivo
    public long getFileSize(String remoteFilePath) throws SftpException {
        if (!connected) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }
        return sftpChannel.lstat(remoteFilePath).getSize();
    }
}
