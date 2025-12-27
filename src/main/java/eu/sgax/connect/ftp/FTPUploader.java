package eu.sgax.connect.ftp;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.net.ftp.FTPClient;

/**
 * Clase para subir archivos a un servidor FTP.
 * Proporciona métodos para subir archivos individuales, directorios completos y streams de archivos.
 * 
 * @author SGAX
 * @version 1.0
 */
public class FTPUploader {

    private final FTPConnect ftpConnect;

    /**
     * Constructor para FTPUploader.
     * 
     * @param ftpConnect instancia de FTPConnect para manejar la conexión FTP
     */
    public FTPUploader(FTPConnect ftpConnect) {
        this.ftpConnect = ftpConnect;
    }

    /**
     * Sube un archivo local al servidor FTP.
     * 
     * @param localPath ruta del archivo local a subir
     * @param remoteFilePath ruta donde guardar el archivo en el servidor FTP
     * @return true si la carga fue exitosa
     * @throws IOException si ocurre un error durante la carga
     */
    public boolean uploadFile(Path localPath, String remoteFilePath) throws IOException {
        if (!ftpConnect.isConnected()) {
            throw new IOException("No conectado al servidor FTP.");
        }

        if (!Files.exists(localPath)) {
            throw new IOException("El archivo local no existe: " + localPath);
        }

        FTPClient ftpClient = ftpConnect.getClient();

        try (FileInputStream inputStream = new FileInputStream(localPath.toFile())) {
            boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
            if (!success) {
                throw new IOException("Fallo al subir el archivo: " + localPath);
            }
            return true;
        }
    }

    /**
     * Sube un archivo desde un InputStream al servidor FTP.
     * útil para subir datos directamente sin crear archivos temporales.
     * 
     * @param inputStream stream de datos del archivo a subir
     * @param remoteFilePath ruta donde guardar el archivo en el servidor FTP
     * @return true si la carga fue exitosa
     * @throws IOException si ocurre un error durante la carga
     */
    public boolean uploadFile(InputStream inputStream, String remoteFilePath) throws IOException {
        if (!ftpConnect.isConnected()) {
            throw new IOException("No conectado al servidor FTP.");
        }

        FTPClient ftpClient = ftpConnect.getClient();
        boolean success = ftpClient.storeFile(remoteFilePath, inputStream);
        if (!success) {
            throw new IOException("Fallo al subir el archivo desde InputStream");
        }
        return true;
    }

    /**
     * Sube un directorio completo al servidor FTP de forma recursiva.
     * Crea la estructura de directorios en el servidor y sube todos los archivos.
     * 
     * @param localDir ruta del directorio local a subir
     * @param remoteDir ruta del directorio remoto donde crear la estructura
     * @throws IOException si ocurre un error durante la carga
     */
    public void uploadDirectory(Path localDir, String remoteDir) throws IOException {
        if (!ftpConnect.isConnected()) {
            throw new IOException("No conectado al servidor FTP.");
        }

        if (!Files.exists(localDir) || !Files.isDirectory(localDir)) {
            throw new IOException("El directorio local no existe o no es un directorio: " + localDir);
        }

        // Crear el directorio remoto si no existe
        if (!ftpConnect.fileExists(remoteDir)) {
            ftpConnect.createDirectory(remoteDir);
        }

        // Cambiar al directorio remoto
        String originalDir = ftpConnect.getCurrentDirectory();
        if (!ftpConnect.changeWorkingDirectory(remoteDir)) {
            throw new IOException("No se pudo cambiar al directorio remoto: " + remoteDir);
        }

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
                } catch (IOException e) {
                    throw new RuntimeException("Error al subir: " + path, e);
                }
            });
        } finally {
            // Volver al directorio original
            ftpConnect.changeWorkingDirectory(originalDir);
        }
    }

    /**
     * Sube un archivo local al servidor FTP con un nombre remoto personalizado.
     * 
     * @param localPath ruta del archivo local a subir
     * @param remoteDir directorio remoto donde guardar el archivo
     * @param remoteFileName nombre con el que guardar el archivo en el servidor
     * @return true si la carga fue exitosa
     * @throws IOException si ocurre un error durante la carga
     */
    public boolean uploadFile(Path localPath, String remoteDir, String remoteFileName) throws IOException {
        String remoteFilePath = remoteDir.endsWith("/") 
            ? remoteDir + remoteFileName 
            : remoteDir + "/" + remoteFileName;
        return uploadFile(localPath, remoteFilePath);
    }

    // Subir y sobrescribir un archivo existente
    public boolean uploadFileOverwrite(Path localPath, String remoteFilePath) throws IOException {
        if (!ftpConnect.isConnected()) {
            throw new IOException("No conectado al servidor FTP.");
        }

        // Borrar el archivo remoto si existe
        if (ftpConnect.fileExists(remoteFilePath)) {
            ftpConnect.deleteFile(remoteFilePath);
        }

        return uploadFile(localPath, remoteFilePath);
    }
}
