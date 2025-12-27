package eu.sgax;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import eu.sgax.connect.ftp.FTPConnect;
import eu.sgax.connect.ftp.FTPUploader;
import eu.sgax.connect.s3.S3Connect;
import eu.sgax.connect.sftp.SFTPConnect;
import eu.sgax.connect.sftp.SFTPUploader;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class Main {
    public static void main(String[] args) {
       //   testS3();
         testFtp();
       // testSftp();
    }

  

    // Prueba completa: subida, listado y descarga con MinIO
    public static void testS3() {
        try {
            String endpoint = System.getenv().getOrDefault("MINIO_ENDPOINT", "http://localhost:9000");
            String accessKey = System.getenv().getOrDefault("MINIO_ACCESS_KEY", "minioadmin");
            String secretKey = System.getenv().getOrDefault("MINIO_SECRET_KEY", "minioadmin");
            String bucket = "test";
            String key = "text.txt";

            AwsCredentialsProvider provider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKey, secretKey));
            S3Connect s3 = new S3Connect(Region.US_EAST_1, provider, URI.create(endpoint));

            // Asegurar que el bucket exista
            try {
                s3.getClient().headBucket(HeadBucketRequest.builder().bucket(bucket).build());
            } catch (Exception hb) {
                s3.getClient().createBucket(CreateBucketRequest.builder().bucket(bucket).build());
                System.out.println("Bucket '" + bucket + "' creado.");
            }

            // Subir el archivo de prueba
            Path src = Path.of("connect-library", "testfile", "text.txt");
            if (!Files.exists(src)) {
                System.err.println("El archivo de prueba no existe en: " + src.toString());
                return;
            }
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("text/plain")
                    .build();
            s3.getClient().putObject(putReq, RequestBody.fromFile(src));
            System.out.println("Subida OK: " + key);

            // Listar para comprobar
            List<String> keys = s3.listObjects(bucket);
            System.out.println("Listado OK. Objetos en '" + bucket + "': " + keys.size());
            if (!keys.isEmpty()) {
                System.out.println("Ejemplos: " + keys.stream().limit(5).toList());
            }

            // Descargar para validar
            Path dest = Path.of("connect-library", "testfile", "downloaded text.txt");
            GetObjectRequest getReq = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            s3.getClient().getObject(getReq, dest);
            String content = Files.readString(dest);
            System.out.println("Descarga OK. Primeros bytes: " + content.substring(0, Math.min(20, content.length())));
        } catch (Exception e) {
            System.err.println("Fallo en test3 (MinIO): " + e.getMessage());
            System.err.println("Verifique MinIO, endpoint, credenciales y permisos del bucket.");
        }
    }

    // Prueba completa FTP: subir, listar, renombrar, listar y borrar
    public static void testFtp() {
        FTPConnect ftp = null;
        try {
            // Configuración para FTP
            String host = System.getenv().getOrDefault("FTP_HOST", "localhost");
            int port = Integer.parseInt(System.getenv().getOrDefault("FTP_PORT", "21"));
            String username = System.getenv().getOrDefault("FTP_USER", "minioadmin");
            String password = System.getenv().getOrDefault("FTP_PASS", "minioadmin");

            System.out.println("\n=== Iniciando prueba FTP ===");
            
            // Conectar
            ftp = new FTPConnect(host, port, username, password);
            ftp.connect();
            System.out.println("Conexión FTP OK en " + host + ":" + port);

            // Subir archivo
            Path src = Path.of("connect-library", "testfile", "text.txt");
            if (!Files.exists(src)) {
                System.err.println("El archivo de prueba no existe en: " + src.toString());
                return;
            }
            FTPUploader uploader = new FTPUploader(ftp);
            uploader.uploadFile(src, "text.txt");
            System.out.println("Subida OK: text.txt");

            // Listar archivos (primera vez)
            List<String> files = ftp.listFiles();
            System.out.println("Listado 1 - Archivos encontrados: " + files.size());
            if (files.contains("text.txt")) {
                System.out.println("  ✓ text.txt está en el servidor");
            }

            // Renombrar archivo
            ftp.rename("text.txt", "textr.txt");
            System.out.println("Renombrado OK: text.txt -> textr.txt");

            // Listar archivos (segunda vez)
            files = ftp.listFiles();
            System.out.println("Listado 2 - Archivos encontrados: " + files.size());
            if (files.contains("textr.txt")) {
                System.out.println("  ✓ textr.txt está en el servidor");
            }
            if (!files.contains("text.txt")) {
                System.out.println("  ✓ text.txt ya no existe (renombrado correctamente)");
            }

            // Borrar archivo
            ftp.deleteFile("textr.txt");
            System.out.println("Borrado OK: textr.txt");

            // Verificar borrado
            files = ftp.listFiles();
            if (!files.contains("textr.txt")) {
                System.out.println("  ✓ textr.txt eliminado correctamente");
            }

            System.out.println("=== Prueba FTP completada exitosamente ===\n");
        } catch (Exception e) {
            System.err.println("Fallo en testFtp: " + e.getMessage());
            System.err.println("Verifique que el servidor FTP esté activo, el endpoint y credenciales.");
            e.printStackTrace();
        } finally {
            // Asegurar desconexión
            if (ftp != null) {
                try {
                    ftp.disconnect();
                } catch (Exception e) {
                    // Ignorar errores al desconectar
                }
            }
        }
    }

    // Prueba completa SFTP: subir, listar, renombrar, listar y borrar
    public static void testSftp() {
        SFTPConnect sftp = null;
        try {
            // Configuración para SFTP
            String host = System.getenv().getOrDefault("SFTP_HOST", "localhost");
            int port = Integer.parseInt(System.getenv().getOrDefault("SFTP_PORT", "2222"));
            String username = System.getenv().getOrDefault("SFTP_USER", "minioadmin");
            String password = System.getenv().getOrDefault("SFTP_PASS", "minioadmin");

            System.out.println("\n=== Iniciando prueba SFTP ===");
            
            // Conectar
            sftp = new SFTPConnect(host, port, username, password);
            sftp.connect();
            System.out.println("Conexión SFTP OK en " + host + ":" + port);

            // Mostrar directorio actual
            String currentDir = sftp.getCurrentDirectory();
            System.out.println("Directorio actual: " + currentDir);

            // Intentar cambiar a un directorio con permisos de escritura
            String workDir = System.getenv().getOrDefault("SFTP_WORK_DIR", "/upload");
            try {
                // Intentar cambiar al directorio de trabajo
                sftp.changeWorkingDirectory(workDir);
                System.out.println("Cambiado a directorio de trabajo: " + workDir);
            } catch (Exception e) {
                // Si falla, intentar crear el directorio en home
                try {
                    String testDir = "sftp-test";
                    if (!sftp.exists(testDir)) {
                        sftp.createDirectory(testDir);
                    }
                    sftp.changeWorkingDirectory(testDir);
                    System.out.println("Usando directorio creado: " + sftp.getCurrentDirectory());
                } catch (Exception e2) {
                    System.out.println("Advertencia: usando directorio actual (" + currentDir + ")");
                    System.out.println("Si hay errores de permisos, configure SFTP_WORK_DIR con un directorio escribible.");
                }
            }

            // Subir archivo
            Path src = Path.of("connect-library", "testfile", "text.txt");
            if (!Files.exists(src)) {
                System.err.println("El archivo de prueba no existe en: " + src.toString());
                return;
            }
            SFTPUploader uploader = new SFTPUploader(sftp);
            uploader.uploadFile(src, "text.txt");
            System.out.println("Subida OK: text.txt");

            // Listar archivos (primera vez)
            List<String> files = sftp.listFiles();
            System.out.println("Listado 1 - Archivos encontrados: " + files.size());
            if (files.contains("text.txt")) {
                System.out.println("  ✓ text.txt está en el servidor");
            }

            // Renombrar archivo
            sftp.rename("text.txt", "textr.txt");
            System.out.println("Renombrado OK: text.txt -> textr.txt");

            // Listar archivos (segunda vez)
            files = sftp.listFiles();
            System.out.println("Listado 2 - Archivos encontrados: " + files.size());
            if (files.contains("textr.txt")) {
                System.out.println("  ✓ textr.txt está en el servidor");
            }
            if (!files.contains("text.txt")) {
                System.out.println("  ✓ text.txt ya no existe (renombrado correctamente)");
            }

            // Borrar archivo
            sftp.deleteFile("textr.txt");
            System.out.println("Borrado OK: textr.txt");

            // Verificar borrado
            files = sftp.listFiles();
            if (!files.contains("textr.txt")) {
                System.out.println("  ✓ textr.txt eliminado correctamente");
            }

            System.out.println("=== Prueba SFTP completada exitosamente ===\n");
        } catch (Exception e) {
            System.err.println("Fallo en testSftp: " + e.getMessage());
            System.err.println("Verifique que el servidor SFTP esté activo, el endpoint y credenciales.");
            e.printStackTrace();
        } finally {
            // Asegurar desconexión
            if (sftp != null) {
                try {
                    sftp.disconnect();
                } catch (Exception e) {
                    // Ignorar errores al desconectar
                }
            }
        }
    }

    
}