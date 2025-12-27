package eu.sgax.sample;

import eu.sgax.connect.ftp.FTPConnect;
import eu.sgax.connect.ftp.FTPDownloader;
import eu.sgax.connect.ftp.FTPUploader;

/**
 * FTP connection example
 * This class demonstrates how to use the FTP connector to upload and download files
 */
public class FTPSample {

    public static void main(String[] args) {
        // FTP server configuration
        String host = "ftp.example.com";
        int port = 21;
        String username = "your-username";
        String password = "your-password";

        try {
            // Connect to FTP server
            System.out.println("Connecting to FTP server...");
            FTPConnect ftpConnect = new FTPConnect(host, port, username, password);
            System.out.println("Successfully connected to FTP server");

            // Example 1: Upload a file
            System.out.println("\n--- Upload Example ---");
            FTPUploader uploader = new FTPUploader(ftpConnect);
            String localFilePath = "/path/to/local/file.txt";
            String remoteFilePath = "/remote/path/file.txt";
            
            System.out.println("Uploading file: " + localFilePath);
            uploader.upload(localFilePath, remoteFilePath);
            System.out.println("File uploaded successfully to: " + remoteFilePath);

            // Example 2: Download a file
            System.out.println("\n--- Download Example ---");
            FTPDownloader downloader = new FTPDownloader(ftpConnect);
            String remoteDownloadPath = "/remote/path/download.txt";
            String localDownloadPath = "/path/to/local/download.txt";
            
            System.out.println("Downloading file from: " + remoteDownloadPath);
            downloader.download(remoteDownloadPath, localDownloadPath);
            System.out.println("File downloaded successfully to: " + localDownloadPath);

            System.out.println("\nFTP operations completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during FTP operations: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
