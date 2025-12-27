package eu.sgax.sample;

import eu.sgax.connect.s3.S3Connect;
import eu.sgax.connect.s3.S3Downloader;
import eu.sgax.connect.s3.S3Uploader;

/**
 * Amazon S3 connection example
 * This class demonstrates how to use the S3 connector to upload and download files to Amazon S3
 */
public class S3Sample {

    public static void main(String[] args) {
        // AWS S3 configuration
        String region = "us-east-1";
        String accessKey = "your-access-key";
        String secretKey = "your-secret-key";
        String bucketName = "your-bucket-name";

        try {
            // Connect to S3
            System.out.println("Connecting to Amazon S3...");
            S3Connect s3Connect = new S3Connect(region, accessKey, secretKey);
            System.out.println("Successfully connected to S3");

            // Example 1: Upload a file
            System.out.println("\n--- Upload Example ---");
            S3Uploader uploader = new S3Uploader(s3Connect);
            String localFilePath = "/path/to/local/file.txt";
            String s3Key = "uploads/file.txt";
            
            System.out.println("Uploading file to S3 bucket: " + bucketName);
            uploader.upload(bucketName, localFilePath, s3Key);
            System.out.println("File uploaded successfully with key: " + s3Key);

            // Example 2: Download a file
            System.out.println("\n--- Download Example ---");
            S3Downloader downloader = new S3Downloader(s3Connect);
            String downloadKey = "downloads/remote-file.txt";
            String localDownloadPath = "/path/to/local/download.txt";
            
            System.out.println("Downloading file from S3 bucket: " + bucketName);
            downloader.download(bucketName, downloadKey, localDownloadPath);
            System.out.println("File downloaded successfully to: " + localDownloadPath);

            System.out.println("\nS3 operations completed successfully!");

        } catch (Exception e) {
            System.err.println("Error during S3 operations: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
