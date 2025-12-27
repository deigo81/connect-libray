package eu.sgax.sample;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import eu.sgax.connect.sftp.SFTPConnect;
import eu.sgax.connect.sftp.SFTPUploader;

/**
 * SFTP connection example
 * This class demonstrates how to use the SFTP connector to upload, list, rename and delete files securely
 */
public class SFTPSample {

    public static void main(String[] args) {
        SFTPConnect sftp = null;
        try {
            // SFTP server configuration (using environment variables or defaults)
            String host = System.getenv().getOrDefault("SFTP_HOST", "localhost");
            int port = Integer.parseInt(System.getenv().getOrDefault("SFTP_PORT", "2222"));
            String username = System.getenv().getOrDefault("SFTP_USER", "minioadmin");
            String password = System.getenv().getOrDefault("SFTP_PASS", "minioadmin");

            System.out.println("\n=== Starting SFTP test ===");
            
            // Connect to SFTP server
            sftp = new SFTPConnect(host, port, username, password);
            sftp.connect();
            System.out.println("SFTP connection OK at " + host + ":" + port);

            // Show current directory
            String currentDir = sftp.getCurrentDirectory();
            System.out.println("Current directory: " + currentDir);

            // Try to change to a writable directory
            String workDir = System.getenv().getOrDefault("SFTP_WORK_DIR", "/upload");
            try {
                // Try to change to work directory
                sftp.changeWorkingDirectory(workDir);
                System.out.println("Changed to work directory: " + workDir);
            } catch (Exception e) {
                // If it fails, try to create a directory in home
                try {
                    String testDir = "sftp-test";
                    if (!sftp.exists(testDir)) {
                        sftp.createDirectory(testDir);
                    }
                    sftp.changeWorkingDirectory(testDir);
                    System.out.println("Using created directory: " + sftp.getCurrentDirectory());
                } catch (Exception e2) {
                    System.out.println("Warning: using current directory (" + currentDir + ")");
                    System.out.println("If permission errors occur, configure SFTP_WORK_DIR with a writable directory.");
                }
            }

            // Upload file
            Path src = Path.of("testfile", "text.txt");
            if (!Files.exists(src)) {
                System.err.println("Test file does not exist at: " + src.toString());
                return;
            }
            SFTPUploader uploader = new SFTPUploader(sftp);
            uploader.uploadFile(src, "text.txt");
            System.out.println("Upload OK: text.txt");

            // List files (first time)
            List<String> files = sftp.listFiles();
            System.out.println("Listing 1 - Files found: " + files.size());
            if (files.contains("text.txt")) {
                System.out.println("  ✓ text.txt is on the server");
            }

            // Rename file
            sftp.rename("text.txt", "textr.txt");
            System.out.println("Renamed OK: text.txt -> textr.txt");

            // List files (second time)
            files = sftp.listFiles();
            System.out.println("Listing 2 - Files found: " + files.size());
            if (files.contains("textr.txt")) {
                System.out.println("  ✓ textr.txt is on the server");
            }
            if (!files.contains("text.txt")) {
                System.out.println("  ✓ text.txt no longer exists (renamed correctly)");
            }

            // Delete file
            sftp.deleteFile("textr.txt");
            System.out.println("Deleted OK: textr.txt");

            // Verify deletion
            files = sftp.listFiles();
            if (!files.contains("textr.txt")) {
                System.out.println("  ✓ textr.txt deleted correctly");
            }

            System.out.println("=== SFTP test completed successfully ===\n");
        } catch (Exception e) {
            System.err.println("Failure in SFTP test: " + e.getMessage());
            System.err.println("Verify that the SFTP server is active, endpoint and credentials.");
            e.printStackTrace();
        } finally {
            // Ensure disconnection
            if (sftp != null) {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    // Ignore disconnection errors
                }
            }
        }
    }
}
