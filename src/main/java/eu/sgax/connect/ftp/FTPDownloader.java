package eu.sgax.connect.ftp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Clase para descargar archivos desde un servidor FTP.
 * Proporciona métodos para descargar archivos individuales, directorios completos y obtener streams de archivos.
 * 
 * @author SGAX
 * @version 1.0
 */
public class FTPDownloader {

    private final FTPConnect ftpConnect;

    /**
     * Constructor para FTPDownloader.
     * 
     * @param ftpConnect instancia de FTPConnect para manejar la conexión FTP
     */
    public FTPDownloader(FTPConnect ftpConnect) {
        this.ftpConnect = ftpConnect;
    }

    /**
     * Descarga un archivo desde el servidor FTP a una ruta local.
     * Crea automáticamente los directorios padres si no existen.
     * 
     * @param remoteFilePath ruta del archivo en el servidor FTP
     * @param localPath ruta local donde guardar el archivo
     * @return true si la descarga fue exitosa
     * @throws IOException si ocurre un error durante la descarga
     */
    public boolean downloadFile(String remoteFilePath, Path localPath) throws IOException {
        if (!ftpConnect.isConnected()) {
            throw new IOException("No conectado al servidor FTP.");
        }

        FTPClient ftpClient = ftpConnect.getClient();

        // Crear directorios padres si no existen
        if (localPath.getParent() != null) {
            Files.createDirectories(localPath.getParent());
        }

        try (FileOutputStream outputStream = new FileOutputStream(localPath.toFile())) {
            boolean success = ftpClient.retrieveFile(remoteFilePath, outputStream);
            if (!success) {
                throw new IOException("Fallo al descargar el archivo: " + remoteFilePath);
            }
            return true;
        }
    }

    /**
     * Obtiene un InputStream para descargar un archivo desde el servidor FTP.
     * Útil para procesar archivos sin guardarlos previamente en disco.
     * 
     * @param remoteFilePath ruta del archivo en el servidor FTP
     * @return InputStream del archivo remoto
     * @throws IOException si ocurre un error al obtener el stream
     */
    public InputStream downloadFileAsStream(String remoteFilePath) throws IOException {
        if (!ftpConnect.isConnected()) {
            throw new IOException("No conectado al servidor FTP.");
        }

        FTPClient ftpClient = ftpConnect.getClient();
        InputStream inputStream = ftpClient.retrieveFileStream(remoteFilePath);
        if (inputStream == null) {
            throw new IOException("Fallo al obtener el stream del archivo: " + remoteFilePath);
        }
        return inputStream;
    }

    /**
     * Descarga recursivamente todos los archivos de un directorio remoto.
     * Crea la estructura de directorios localmente y descarga todos los archivos y subdirectorios.
     * 
     * @param remoteDir ruta del directorio remoto a descargar
     * @param localDir ruta local donde guardar el directorio
     * @throws IOException si ocurre un error durante la descarga
     */
    public void downloadDirectory(String remoteDir, Path localDir) throws IOException {
        if (!ftpConnect.isConnected()) {
            throw new IOException("No conectado al servidor FTP.");
        }

        Files.createDirectories(localDir);
        
        // Cambiar al directorio remoto
        String originalDir = ftpConnect.getCurrentDirectory();
        if (!ftpConnect.changeWorkingDirectory(remoteDir)) {
            throw new IOException("No se pudo cambiar al directorio remoto: " + remoteDir);
        }

        try {
            // Listar archivos en el directorio
            for (String fileName : ftpConnect.listFiles()) {
                Path localFilePath = localDir.resolve(fileName);
                downloadFile(fileName, localFilePath);
            }

            // Procesar subdirectorios recursivamente
            for (String dirName : ftpConnect.listDirectories()) {
                if (!dirName.equals(".") && !dirName.equals("..")) {
                    Path localSubDir = localDir.resolve(dirName);
                    downloadDirectory(dirName, localSubDir);
                }
            }
        } finally {
            // Volver al directorio original
            ftpConnect.changeWorkingDirectory(originalDir);
        }
    }

    /**
     * Descarga un archivo desde el servidor FTP con un nombre local personalizado.
     * 
     * @param remoteFilePath ruta del archivo en el servidor FTP
     * @param localPath directorio local donde guardar el archivo
     * @param localFileName nombre con el que guardar el archivo localmente
     * @return true si la descarga fue exitosa
     * @throws IOException si ocurre un error durante la descarga
     */
    public boolean downloadFile(String remoteFilePath, Path localPath, String localFileName) throws IOException {
        Path targetPath = localPath.resolve(localFileName);
        return downloadFile(remoteFilePath, targetPath);
    }
}
