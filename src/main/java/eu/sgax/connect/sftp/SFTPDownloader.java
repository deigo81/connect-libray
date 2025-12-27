package eu.sgax.connect.sftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpException;

/**
 * Clase para descargar archivos desde un servidor SFTP.
 * Proporciona métodos para descargar archivos individuales, directorios completos y streams de archivos.
 * 
 * @author SGAX
 * @version 1.0
 */
public class SFTPDownloader {

    private final SFTPConnect sftpConnect;

    /**
     * Constructor para SFTPDownloader.
     * 
     * @param sftpConnect instancia de SFTPConnect para manejar la conexión SFTP
     */
    public SFTPDownloader(SFTPConnect sftpConnect) {
        this.sftpConnect = sftpConnect;
    }

    /**
     * Descarga un archivo desde el servidor SFTP a una ruta local.
     * Crea automáticamente los directorios padres si no existen.
     * 
     * @param remoteFilePath ruta del archivo en el servidor SFTP
     * @param localPath ruta local donde guardar el archivo
     * @throws SftpException si ocurre un error SFTP
     * @throws IOException si ocurre un error de I/O
     */
    public void downloadFile(String remoteFilePath, Path localPath) throws SftpException, IOException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        ChannelSftp channel = sftpConnect.getChannel();

        // Crear directorios padres si no existen
        if (localPath.getParent() != null) {
            Files.createDirectories(localPath.getParent());
        }

        try (OutputStream outputStream = Files.newOutputStream(localPath)) {
            channel.get(remoteFilePath, outputStream);
        }
    }

    /**
     * Obtiene un InputStream para descargar un archivo desde el servidor SFTP.
     * Útil para procesar archivos sin guardarlos previamente en disco.
     * 
     * @param remoteFilePath ruta del archivo en el servidor SFTP
     * @return InputStream del archivo remoto
     * @throws SftpException si ocurre un error SFTP
     */
    public InputStream downloadFileAsStream(String remoteFilePath) throws SftpException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        ChannelSftp channel = sftpConnect.getChannel();
        return channel.get(remoteFilePath);
    }

    /**
     * Descarga recursivamente todos los archivos de un directorio remoto.
     * Crea la estructura de directorios localmente y descarga todos los archivos y subdirectorios.
     * 
     * @param remoteDir ruta del directorio remoto a descargar
     * @param localDir ruta local donde guardar el directorio
     * @throws SftpException si ocurre un error SFTP
     * @throws IOException si ocurre un error de I/O
     */
    public void downloadDirectory(String remoteDir, Path localDir) throws SftpException, IOException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        Files.createDirectories(localDir);
        
        // Cambiar al directorio remoto
        String originalDir = sftpConnect.getCurrentDirectory();
        sftpConnect.changeWorkingDirectory(remoteDir);

        try {
            // Listar archivos en el directorio
            List<String> files = sftpConnect.listFiles();
            for (String fileName : files) {
                Path localFilePath = localDir.resolve(fileName);
                downloadFile(fileName, localFilePath);
            }

            // Procesar subdirectorios recursivamente
            List<String> directories = sftpConnect.listDirectories();
            for (String dirName : directories) {
                Path localSubDir = localDir.resolve(dirName);
                downloadDirectory(dirName, localSubDir);
            }
        } finally {
            // Volver al directorio original
            sftpConnect.changeWorkingDirectory(originalDir);
        }
    }

    /**
     * Descarga un archivo desde el servidor SFTP con un nombre local personalizado.
     * 
     * @param remoteFilePath ruta del archivo en el servidor SFTP
     * @param localPath directorio local donde guardar el archivo
     * @param localFileName nombre con el que guardar el archivo localmente
     * @throws SftpException si ocurre un error SFTP
     * @throws IOException si ocurre un error de I/O
     */
    public void downloadFile(String remoteFilePath, Path localPath, String localFileName) throws SftpException, IOException {
        Path targetPath = localPath.resolve(localFileName);
        downloadFile(remoteFilePath, targetPath);
    }

    /**
     * Descarga un archivo con monitoreo de progreso.
     * 
     * @param remoteFilePath ruta del archivo en el servidor SFTP
     * @param localPath ruta local donde guardar el archivo
     * @param monitor monitor de progreso para mostrar el avance
     * @throws SftpException si ocurre un error SFTP
     * @throws IOException si ocurre un error de I/O
     */
    public void downloadFileWithProgress(String remoteFilePath, Path localPath, ProgressMonitor monitor) throws SftpException, IOException {
        if (!sftpConnect.isConnected()) {
            throw new SftpException(0, "No conectado al servidor SFTP.");
        }

        ChannelSftp channel = sftpConnect.getChannel();

        // Crear directorios padres si no existen
        if (localPath.getParent() != null) {
            Files.createDirectories(localPath.getParent());
        }

        channel.get(remoteFilePath, localPath.toString(), monitor);
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

    // Descargar múltiples archivos específicos
    public void downloadFiles(List<String> remoteFilePaths, Path localDir) throws SftpException, IOException {
        Files.createDirectories(localDir);
        
        for (String remoteFilePath : remoteFilePaths) {
            String fileName = remoteFilePath.substring(remoteFilePath.lastIndexOf('/') + 1);
            Path localFilePath = localDir.resolve(fileName);
            downloadFile(remoteFilePath, localFilePath);
        }
    }
}
