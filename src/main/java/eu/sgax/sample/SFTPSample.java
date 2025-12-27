package eu.sgax.sample;

import eu.sgax.connect.sftp.SFTPConnect;
import eu.sgax.connect.sftp.SFTPDownloader;
import eu.sgax.connect.sftp.SFTPUploader;

/**
 * SFTP connection example
 * This class demonstrates how to use the SFTP connector to upload and download files securely
 */
public class SFTPSample {

    public static void main(String[] args) {
        // SFTP server configuration
        String host = "sftp.example.com";
        int port = 22;
        String username = "your-username";
        String password = "your-password";

        try {
            // Connect to SFTP server
            System.out.println("Connecting to SFTP server...");
            SFTPConnect sftpConnect = new SFTPConnect(host, port, username, password);
            System.out.println("Successfully connected to SFTP server");

            // Example 1: Upload a file
            System.out.println("\n--- Upload Example ---");
            SFTPUploader uploader = new SFTPUploader(sftpConnect);
            String localFilePath = "/path/to/local/file.txt";
            String remoteFilePath = "/remote/path/file.txt";
            
            System.out.println("Uploading file: " + localFilePath);
            uploader.upload(localFilePath, remoteFilePath);
            System.out.println("File uploaded successfully to: " + remoteFilePath);

            // Example 2: Download a file
            System.out.println("\n--- Download Example ---");
            SFTPDownloader downloader = new SFTPDownloader(sftpConnect);
            String remoteDownloadPath = "/remote/path/download.txt";
            String localDownloadPath = "/path/to/local/download.txt";
            
            System.out.println("Downloading file from: " + remoteDownloadPath);
            downloader.download(remoteDownloadPath, localDownloadPath);
            System.out.println("File downloaded successfully to: " + localDownloadPath);

            System.out.println("\nSFTP operations completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during SFTP operations: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
