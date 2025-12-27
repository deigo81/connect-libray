package eu.sgax.connect.s3;

import java.nio.file.Path;

import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * Clase para descargar objetos desde S3 y servicios compatibles.
 * Proporciona métodos de instancia y estáticos para descargar archivos.
 * 
 * @author SGAX
 * @version 1.0
 */
public class S3Downloader {

    private final S3Client client;

    /**
     * Constructor para S3Downloader.
     * 
     * @param client cliente S3 para realizar las descargas
     */
    public S3Downloader(S3Client client) {
        this.client = client;
    }

    /**
     * Descarga un objeto desde S3 a un archivo local usando el cliente de instancia.
     * 
     * @param bucket nombre del bucket
     * @param key clave del objeto a descargar
     * @param destination ruta local del archivo destino
     * @return GetObjectResponse con los metadatos de la descarga
     */
    public GetObjectResponse download(String bucket, String key, Path destination) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return client.getObject(request, ResponseTransformer.toFile(destination));
    }

    /**
     * Descarga un objeto desde S3 a un archivo local usando un cliente explícito (método estático).
     * 
     * @param client cliente S3 a utilizar
     * @param bucket nombre del bucket
     * @param key clave del objeto a descargar
     * @param destination ruta local del archivo destino
     * @return GetObjectResponse con los metadatos de la descarga
     */
    public static GetObjectResponse download(S3Client client, String bucket, String key, Path destination) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return client.getObject(request, ResponseTransformer.toFile(destination));
    }
}