package eu.sgax.connect.s3;

import java.nio.file.Path;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

/**
 * Clase para subir objetos a S3 y servicios compatibles.
 * Proporciona métodos para subir archivos con soporte automático de prefijos de directorio.
 * 
 * @author SGAX
 * @version 1.0
 */
public class S3Uploader {

    private final S3Client client;

    /**
     * Constructor para S3Uploader.
     * 
     * @param client cliente S3 para realizar las cargas
     */
    public S3Uploader(S3Client client) {
        this.client = client;
    }

    /**
     * Sube un archivo local a S3.
     * Crea automáticamente los prefijos de directorio necesarios en el bucket.
     * 
     * @param bucket nombre del bucket destino
     * @param key clave del objeto a crear (ej: "uploads/images/photo.jpg")
     * @param source ruta del archivo local a subir
     * @return PutObjectResponse con los metadatos de la carga
     */
    public PutObjectResponse upload(String bucket, String key, Path source) {
        ensurePrefixExists(bucket, key);
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return client.putObject(request, RequestBody.fromFile(source));
    }

    /**
     * Asegura que el prefijo de directorio existe en el bucket.
     * Crea un objeto vacío en la ruta del prefijo si no existe.
     * 
     * @param bucket nombre del bucket
     * @param key clave del objeto completa
     */
    private void ensurePrefixExists(String bucket, String key) {
        String prefix = extractPrefix(key);
        if (prefix == null || prefix.isEmpty()) {
            return;
        }
        String marker = prefix.endsWith("/") ? prefix : prefix + "/";
        ListObjectsV2Request listReq = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(marker)
                .maxKeys(1)
                .build();
        ListObjectsV2Response listRes = client.listObjectsV2(listReq);
        if (listRes.keyCount() == 0) {
            PutObjectRequest folderReq = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(marker)
                    .build();
            client.putObject(folderReq, RequestBody.empty());
        }
    }

    /**
     * Extrae el prefijo de directorio de una clave.
     * 
     * @param key clave completa del objeto
     * @return el prefijo de directorio o null si no hay prefijo
     */
    private String extractPrefix(String key) {
        int idx = key.lastIndexOf('/');
        if (idx <= 0) {
            return null;
        }
        return key.substring(0, idx + 1);
    }
}