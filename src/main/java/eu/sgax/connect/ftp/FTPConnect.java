package eu.sgax.connect.ftp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FTPConnect {

    private final FTPClient ftpClient;
    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private boolean connected = false;

    /**
     * Constructor con credenciales completas.
     * 
     * @param host dirección del servidor FTP
     * @param port puerto del servidor FTP
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     */
    public FTPConnect(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.ftpClient = new FTPClient();
    }

    /**
     * Constructor con puerto por defecto (21).
     * 
     * @param host dirección del servidor FTP
     * @param username nombre de usuario para autenticación
     * @param password contraseña para autenticación
     */
    public FTPConnect(String host, String username, String password) {
        this(host, 21, username, password);
    }

    /**
     * Conecta al servidor FTP y se autentica con las credenciales proporcionadas.
     * 
     * @throws IOException si ocurre un error durante la conexión o autenticación
     */
    public void connect() throws IOException {
        if (connected) {
            return;
        }
        ftpClient.connect(host, port);
        int reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("Fallo al conectar al servidor FTP. Código de respuesta: " + reply);
        }

        if (!ftpClient.login(username, password)) {
            ftpClient.disconnect();
            throw new IOException("Fallo al autenticar en el servidor FTP.");
        }

        ftpClient.enterLocalPassiveMode();
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        connected = true;
    }

    /**
     * Desconecta del servidor FTP de manera segura.
     * 
     * @throws IOException si ocurre un error durante la desconexión
     */
    public void disconnect() throws IOException {
        if (connected && ftpClient.isConnected()) {
            ftpClient.logout();
            ftpClient.disconnect();
            connected = false;
        }
    }

    /**
     * Obtiene el cliente FTP subyacente para operaciones avanzadas.
     * 
     * @return el objeto FTPClient
     */
    public FTPClient getClient() {
        return ftpClient;
    }

    /**
     * Verifica si está conectado al servidor FTP.
     * 
     * @return true si está conectado, false en caso contrario
     */
    public boolean isConnected() {
        return connected && ftpClient.isConnected();
    }

    /**
     * Lista los archivos en el directorio actual.
     * 
     * @return lista con los nombres de los archivos
     * @throws IOException si ocurre un error de conexión
     */
    public List<String> listFiles() throws IOException {
        return listFiles(null);
    }

    /**
     * Lista los archivos en un directorio específico.
     * 
     * @param remotePath ruta del directorio remoto
     * @return lista con los nombres de los archivos
     * @throws IOException si ocurre un error de conexión
     */
    public List<String> listFiles(String remotePath) throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }

        FTPFile[] files = remotePath == null ? ftpClient.listFiles() : ftpClient.listFiles(remotePath);
        List<String> fileNames = new ArrayList<>();
        for (FTPFile file : files) {
            if (file.isFile()) {
                fileNames.add(file.getName());
            }
        }
        return fileNames;
    }

    /**
     * Lista los directorios en el directorio actual.
     * 
     * @return lista con los nombres de los directorios
     * @throws IOException si ocurre un error de conexión
     */
    public List<String> listDirectories() throws IOException {
        return listDirectories(null);
    }

    /**
     * Lista los directorios en un directorio específico.
     * 
     * @param remotePath ruta del directorio remoto
     * @return lista con los nombres de los directorios
     * @throws IOException si ocurre un error de conexión
     */
    public List<String> listDirectories(String remotePath) throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }

        FTPFile[] files = remotePath == null ? ftpClient.listFiles() : ftpClient.listFiles(remotePath);
        List<String> dirNames = new ArrayList<>();
        for (FTPFile file : files) {
            if (file.isDirectory()) {
                dirNames.add(file.getName());
            }
        }
        return dirNames;
    }

    /**
     * Crea un directorio en el servidor FTP.
     * 
     * @param remotePath ruta del directorio a crear
     * @return true si se creó exitosamente, false en caso contrario
     * @throws IOException si ocurre un error de conexión
     */
    public boolean createDirectory(String remotePath) throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }
        return ftpClient.makeDirectory(remotePath);
    }

    /**
     * Borra un archivo del servidor FTP.
     * 
     * @param remoteFilePath ruta del archivo a borrar
     * @return true si se borró exitosamente, false en caso contrario
     * @throws IOException si ocurre un error de conexión
     */
    public boolean deleteFile(String remoteFilePath) throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }
        return ftpClient.deleteFile(remoteFilePath);
    }

    /**
     * Borra un directorio del servidor FTP.
     * 
     * @param remotePath ruta del directorio a borrar
     * @return true si se borró exitosamente, false en caso contrario
     * @throws IOException si ocurre un error de conexión
     */
    public boolean deleteDirectory(String remotePath) throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }
        return ftpClient.removeDirectory(remotePath);
    }

    /**
     * Renombra un archivo o directorio en el servidor FTP.
     * 
     * @param fromPath ruta actual del archivo o directorio
     * @param toPath nueva ruta del archivo o directorio
     * @return true si se renombró exitosamente, false en caso contrario
     * @throws IOException si ocurre un error de conexión
     */
    public boolean rename(String fromPath, String toPath) throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }
        return ftpClient.rename(fromPath, toPath);
    }

    /**
     * Cambia el directorio de trabajo en el servidor FTP.
     * 
     * @param remotePath ruta del directorio al que cambiar
     * @return true si se cambió exitosamente, false en caso contrario
     * @throws IOException si ocurre un error de conexión
     */
    public boolean changeWorkingDirectory(String remotePath) throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }
        return ftpClient.changeWorkingDirectory(remotePath);
    }

    /**
     * Obtiene el directorio de trabajo actual en el servidor FTP.
     * 
     * @return ruta del directorio actual
     * @throws IOException si ocurre un error de conexión
     */
    public String getCurrentDirectory() throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }
        return ftpClient.printWorkingDirectory();
    }

    /**
     * Verifica si un archivo existe en el servidor FTP.
     * 
     * @param remoteFilePath ruta del archivo a verificar
     * @return true si el archivo existe, false en caso contrario
     * @throws IOException si ocurre un error de conexión
     */
    public boolean fileExists(String remoteFilePath) throws IOException {
        if (!connected) {
            throw new IOException("No conectado al servidor FTP.");
        }
        FTPFile[] files = ftpClient.listFiles(remoteFilePath);
        return files.length > 0;
    }
}
