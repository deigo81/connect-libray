package eu.sgax.connect.s3;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;

/**
 * Clase para manejar conexiones a servicios compatibles con S3 (AWS S3, MinIO, LocalStack, etc.).
 * Proporciona diferentes formas de inicializar el cliente S3 con diferentes tipos de credenciales.
 * 
 * @author SGAX
 * @version 1.0
 */
public class S3Connect {

    public final S3Client client;

    /**
     * Constructor que usa la cadena de credenciales por defecto del SDK.
     * Utiliza credenciales configuradas en variables de entorno o archivos de configuración del sistema.
     * 
     * @param region región de AWS donde conectar
     */
    public S3Connect(Region region) {
        this.client = S3Client.builder()
                .region(region)
                .build();
    }

    /**
     * Constructor con proveedor de credenciales explícito.
     * 
     * @param region región de AWS donde conectar
     * @param credentialsProvider proveedor de credenciales (ej: StaticCredentialsProvider, DefaultCredentialsProvider)
     */
    public S3Connect(Region region, AwsCredentialsProvider credentialsProvider) {
        this.client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /**
     * Constructor con acceso directo mediante clave de acceso y clave secreta.
     * 
     * @param region región de AWS donde conectar
     * @param accessKeyId clave de acceso de AWS
     * @param secretAccessKey clave secreta de AWS
     */
    public S3Connect(Region region, String accessKeyId, String secretAccessKey) {
        AwsCredentialsProvider provider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        this.client = S3Client.builder()
                .region(region)
                .credentialsProvider(provider)
                .build();
    }

    /**
     * Constructor con anulación de endpoint (endpoint override).
     * Útil para conectar a servicios compatibles con S3 como MinIO, LocalStack o S3 compatible.
     * 
     * @param region región de AWS donde conectar
     * @param credentialsProvider proveedor de credenciales
     * @param endpointOverride URI del endpoint personalizado (ej: http://localhost:9000 para MinIO)
     */
    public S3Connect(Region region, AwsCredentialsProvider credentialsProvider, URI endpointOverride) {
        this.client = S3Client.builder()
                .region(region)
                .credentialsProvider(credentialsProvider)
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .endpointOverride(endpointOverride)
                .build();
    }

    /**
     * Obtiene el cliente S3 subyacente para operaciones avanzadas.
     * 
     * @return el objeto S3Client
     */
    public S3Client getClient() {
        return client;
    }

    /**
     * Lista todos los objetos en un bucket.
     * 
     * @param bucket nombre del bucket
     * @return lista con las claves de todos los objetos
     */
    public List<String> listObjects(String bucket) {
        return listObjects(bucket, null);
    }

    /**
     * Lista los objetos en un bucket, opcionalmente filtrando por prefijo.
     * 
     * @param bucket nombre del bucket
     * @param prefix prefijo para filtrar objetos (ej: "uploads/images/")
     * @return lista con las claves de los objetos que coinciden con el prefijo
     */
    public List<String> listObjects(String bucket, String prefix) {
        List<String> keys = new ArrayList<>();
        ListObjectsV2Request.Builder builder = ListObjectsV2Request.builder()
                .bucket(bucket);
        if (prefix != null && !prefix.isEmpty()) {
            builder.prefix(prefix);
        }
        var paginator = client.listObjectsV2Paginator(builder.build());
        paginator.stream().forEach(resp -> resp.contents().forEach(obj -> keys.add(obj.key())));
        return keys;
    }
}