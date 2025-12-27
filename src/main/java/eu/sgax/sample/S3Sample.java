package eu.sgax.sample;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import eu.sgax.connect.s3.S3Connect;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Amazon S3 / MinIO connection example
 * This class demonstrates how to use the S3 connector to upload, list and download files to S3 or MinIO
 */
public class S3Sample {

    public static void main(String[] args) {
        try {
            // S3/MinIO configuration (using environment variables or defaults)
            String endpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://localhost:9000");
            String accessKey = System.getenv().getOrDefault("MINIO_ACCESS_KEY", "minioadmin");
            String secretKey = System.getenv().getOrDefault("MINIO_SECRET_KEY", "minioadmin");
            String bucket = "test";
            String key = "text.txt";

            AwsCredentialsProvider provider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
            S3Connect s3 = new S3Connect(Region.US_EAST_1, provider, URI.create(endpoint));

            // Ensure bucket exists
            try {
                s3.getClient().headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            } catch (Exception hb) {
                s3.getClient().createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                System.out.println("Bucket '" + bucket + "' created.");
            }

            // Upload test file
            Path src = Path.of("testfile", "text.txt");
            if (!Files.exists(src)) {
                System.err.println("Test file does not exist at: " + src.toString());
                return;
            }
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("text/plain")
                    .build();
            s3.getClient().putObject(putReq, RequestBody.fromFile(src));
            System.out.println("Upload OK: " + key);

            // List to verify
            List<String> keys = s3.listObjects(bucket);
            System.out.println("Listing OK. Objects in '" + bucket + "': " + keys.size());
            if (!keys.isEmpty()) {
                System.out.println("Examples: " + keys.stream().limit(5).toList());
            }

            // Download to validate
            Path dest = Path.of("testfile", "downloaded text.txt");
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3.getClient().getObject(getReq, dest);
            String content = Files.readString(dest);
            System.out.println("Download OK. First bytes: " + content.substring(0, Math.min(20, content.length())));
        } catch (Exception e) {
            System.err.println("Failure in S3 test (MinIO): " + e.getMessage());
            System.err.println("Verify MinIO, endpoint, credentials and bucket permissions.");
        }
    }
}
