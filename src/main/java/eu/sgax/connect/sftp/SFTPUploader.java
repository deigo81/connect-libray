package eu.sgax.connect.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

/**
 * Clase para subir archivos a un servidor SFTP.
 * Proporciona métodos para subir archivos individuales, directorios completos y streams de archivos.
 * 
 * @author SGAX
 * @version 1.0
 */
public class SFTPUploader {

    private final SFTPConnect sftpConnect;

    /**
     * Constructor para SFTPUploader.
     * 
     * @param sftpConnect instancia de SFTPConnect para manejar la conexión SFTP
     */
    public SFTPUploader(SFTPConnect sftpConnect) {
        this.sftpConnect = sftpConnect;
    }

    /**
     * Sube un archivo local al servidor SFTP.
     * 
     * @param localPath ruta del archivo local a subir
     * @param remoteFilePath ruta donde guardar el archivo en el servidor SFTP
     * @throws SftpException si ocurre un error SFTP
     * @throws IOException si ocurre un error de I/O
     */
    public void uploadFile(Path localPath, String remoteFilePath) throws SftpException, IOException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        if (!Files.exists(localPath)) {
            throw new IOException("El archivo local no existe: " + localPath);
        }

        ChannelSftp channel = sftpConnect.getChannel();
        try (InputStream inputStream = Files.newInputStream(localPath)) {
            channel.put(inputStream, remoteFilePath);
        }
    }

    /**
     * Sube un archivo desde un InputStream al servidor SFTP.
     * Útil para subir datos directamente sin crear archivos temporales.
     * 
     * @param inputStream stream de datos del archivo a subir
     * @param remoteFilePath ruta donde guardar el archivo en el servidor SFTP
     * @throws SftpException si ocurre un error SFTP
     */
    public void uploadFile(InputStream inputStream, String remoteFilePath) throws SftpException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        ChannelSftp channel = sftpConnect.getChannel();
        channel.put(inputStream, remoteFilePath);
    }

    /**
     * Sube un directorio completo al servidor SFTP de forma recursiva.
     * Crea la estructura de directorios en el servidor y sube todos los archivos.
     * 
     * @param localDir ruta del directorio local a subir
     * @param remoteDir ruta del directorio remoto donde crear la estructura
     * @throws SftpException si ocurre un error SFTP
     * @throws IOException si ocurre un error de I/O
     */
    public void uploadDirectory(Path localDir, String remoteDir) throws SftpException, IOException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        if (!Files.exists(localDir) || !Files.isDirectory(localDir)) {
            throw new IOException("El directorio local no existe o no es un directorio: " + localDir);
        }

        // Crear el directorio remoto si no existe
        if (!sftpConnect.exists(remoteDir)) {
            sftpConnect.createDirectory(remoteDir);
        }

        // Cambiar al directorio remoto
        String originalDir = sftpConnect.getCurrentDirectory();
        sftpConnect.changeWorkingDirectory(remoteDir);

        try {
            // Subir archivos
            Files.list(localDir).forEach(path -> {
                try {
                    if (Files.isRegularFile(path)) {
                        uploadFile(path, path.getFileName().toString());
                    } else if (Files.isDirectory(path)) {
                        String subDirName = path.getFileName().toString();
                        uploadDirectory(path, subDirName);
                    }
                } catch (IOException | SftpException e) {
                    throw new RuntimeException("Error al subir: " + path, e);
                }
            });
        } finally {
            // Volver al directorio original
            sftpConnect.changeWorkingDirectory(originalDir);
        }
    }

    /**
     * Sube un archivo local al servidor SFTP con un nombre remoto personalizado.
     * 
     * @param localPath ruta del archivo local a subir
     * @param remoteDir directorio remoto donde guardar el archivo
     * @param remoteFileName nombre con el que guardar el archivo en el servidor
     * @throws SftpException si ocurre un error SFTP
     * @throws IOException si ocurre un error de I/O
     */
    public void uploadFile(Path localPath, String remoteDir, String remoteFileName) throws SftpException, IOException {
        String remoteFilePath = remoteDir.endsWith("/") 
            ? remoteDir + remoteFileName 
            : remoteDir + "/" + remoteFileName;
        uploadFile(localPath, remoteFilePath);
    }

    /**
     * Sube un archivo local al servidor SFTP sobrescribiendo si ya existe.
     * 
     * @param localPath ruta del archivo local a subir
     * @param remoteFilePath ruta donde guardar el archivo en el servidor SFTP
     * @throws SftpException si ocurre un error SFTP
     * @throws IOException si ocurre un error de I/O
     */
    public void uploadFileOverwrite(Path localPath, String remoteFilePath) throws SftpException, IOException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        // Borrar el archivo remoto si existe
        if (sftpConnect.exists(remoteFilePath)) {
            sftpConnect.deleteFile(remoteFilePath);
        }

        uploadFile(localPath, remoteFilePath);
    }

    // Subir con monitor de progreso
    public void uploadFileWithProgress(Path localPath, String remoteFilePath, ProgressMonitor monitor) throws SftpException, IOException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        if (!Files.exists(localPath)) {
            throw new IOException("El archivo local no existe: " + localPath);
        }

        ChannelSftp channel = sftpConnect.getChannel();
        channel.put(localPath.toString(), remoteFilePath, monitor);
    }

    // Interfaz para monitor de progreso
    public interface ProgressMonitor extends com.jcraft.jsch.SftpProgressMonitor {
        @Override
        default void init(int op, String src, String dest, long max) {
            // Implementación por defecto vacía
        }

        @Override
        default boolean count(long count) {
            // Retornar true para continuar, false para cancelar
            return true;
        }

        @Override
        default void end() {
            // Implementación por defecto vacía
        }
    }

    // Subir múltiples archivos específicos
    public void uploadFiles(List<Path> localPaths, String remoteDir) throws SftpException, IOException {
        // Crear el directorio remoto si no existe
        if (!sftpConnect.exists(remoteDir)) {
            sftpConnect.createDirectory(remoteDir);
        }

        for (Path localPath : localPaths) {
            String fileName = localPath.getFileName().toString();
            String remoteFilePath = remoteDir.endsWith("/") 
                ? remoteDir + fileName 
                : remoteDir + "/" + fileName;
            uploadFile(localPath, remoteFilePath);
        }
    }

    // Subir archivo con modo de transferencia específico
    public void uploadFile(Path localPath, String remoteFilePath, int mode) throws SftpException, IOException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        if (!Files.exists(localPath)) {
            throw new IOException("El archivo local no existe: " + localPath);
        }

        ChannelSftp channel = sftpConnect.getChannel();
        try (InputStream inputStream = Files.newInputStream(localPath)) {
            channel.put(inputStream, remoteFilePath, mode);
        }
    }
}
