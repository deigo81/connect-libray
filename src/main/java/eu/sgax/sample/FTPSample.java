package eu.sgax.sample;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import eu.sgax.connect.ftp.FTPConnect;
import eu.sgax.connect.ftp.FTPUploader;

/**
 * FTP connection example
 * This class demonstrates how to use the FTP connector to upload, list, rename and delete files
 */
public class FTPSample {

    public static void main(String[] args) {
        FTPConnect ftp = null;
        try {
            // FTP server configuration (using environment variables or defaults)
            String host = System.getenv().getOrDefault("FTP_HOST", "localhost");
            int port = Integer.parseInt(System.getenv().getOrDefault("FTP_PORT", "21"));
            String username = System.getenv().getOrDefault("FTP_USER", "minioadmin");
            String password = System.getenv().getOrDefault("FTP_PASS", "minioadmin");

            System.out.println("\n=== Starting FTP test ===");
            
            // Connect to FTP server
            ftp = new FTPConnect(host, port, username, password);
            ftp.connect();
            System.out.println("FTP connection OK at " + host + ":" + port);

            // Upload file
            Path src = Path.of("testfile", "text.txt");
            if (!Files.exists(src)) {
                System.err.println("Test file does not exist at: " + src.toString());
                return;
            }
            FTPUploader uploader = new FTPUploader(ftp);
            uploader.uploadFile(src, "text.txt");
            System.out.println("Upload OK: text.txt");

            // List files (first time)
            List<String> files = ftp.listFiles();
            System.out.println("Listing 1 - Files found: " + files.size());
            if (files.contains("text.txt")) {
                System.out.println("  ✓ text.txt is on the server");
            }

            // Rename file
            ftp.rename("text.txt", "textr.txt");
            System.out.println("Renamed OK: text.txt -> textr.txt");

            // List files (second time)
            files = ftp.listFiles();
            System.out.println("Listing 2 - Files found: " + files.size());
            if (files.contains("textr.txt")) {
                System.out.println("  ✓ textr.txt is on the server");
            }
            if (!files.contains("text.txt")) {
                System.out.println("  ✓ text.txt no longer exists (renamed correctly)");
            }

            // Delete file
            ftp.deleteFile("textr.txt");
            System.out.println("Deleted OK: textr.txt");

            // Verify deletion
            files = ftp.listFiles();
            if (!files.contains("textr.txt")) {
                System.out.println("  ✓ textr.txt deleted correctly");
            }

            System.out.println("=== FTP test completed successfully ===\n");
        } catch (Exception e) {
            System.err.println("Failure in FTP test: " + e.getMessage());
            System.err.println("Verify that the FTP server is active, endpoint and credentials.");
            e.printStackTrace();
        } finally {
            // Ensure disconnection
            if (ftp != null) {
                try {
                    ftp.disconnect();
                } catch (Exception e) {
                    // Ignore disconnection errors
                }
            }
        }
    }
}
