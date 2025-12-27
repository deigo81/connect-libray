# Connect Library

Libreria de java que permite conectarse a multiples protocolos.
## Descripción

Connect Library libreria para facilitar la  conexion de multiples protocolos. Incluye implementaciones para:

- **FTP** 
- **SFTP** 
- **S3** 
- **Mail**


## Módulos

- `eu.sgax.connect.ftp` - Conexiones FTP
- `eu.sgax.connect.sftp` - Conexiones SFTP
- `eu.sgax.connect.s3` - Integración con Amazon S3
- `eu.sgax.connect.mail` - Operaciones de correo electrónico

## Instalación

### Agregar como dependencia Maven

Para usar esta librería en tu proyecto Maven, agrega lo siguiente a tu `pom.xml`:

1. **Configurar el repositorio de GitHub Packages**:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/deigo81/connect-libray</url>
    </repository>
</repositories>
```

2. **Agregar la dependencia**:

```xml
<dependency>
    <groupId>eu.sgax</groupId>
    <artifactId>connect-library</artifactId>
    <version>1.0</version>
</dependency>
```

3. **Configurar autenticación** en `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>TU_USUARIO_GITHUB</username>
            <password>TU_TOKEN_GITHUB</password>
        </server>
    </servers>
</settings>
```

## Uso

### FTP

```java
import eu.sgax.connect.ftp.FTPConnect;
import eu.sgax.connect.ftp.FTPDownloader;
import eu.sgax.connect.ftp.FTPUploader;

// Conectar a servidor FTP
FTPConnect ftpConnect = new FTPConnect("ftp.example.com", 21, "usuario", "password");

// Descargar archivo
FTPDownloader downloader = new FTPDownloader(ftpConnect);
downloader.download("/remote/path/file.txt", "/local/path/file.txt");

// Subir archivo
FTPUploader uploader = new FTPUploader(ftpConnect);
uploader.upload("/local/path/file.txt", "/remote/path/file.txt");
```

### SFTP

```java
import eu.sgax.connect.sftp.SFTPConnect;
import eu.sgax.connect.sftp.SFTPDownloader;
import eu.sgax.connect.sftp.SFTPUploader;

// Conectar a servidor SFTP
SFTPConnect sftpConnect = new SFTPConnect("sftp.example.com", 22, "usuario", "password");

// Descargar archivo
SFTPDownloader downloader = new SFTPDownloader(sftpConnect);
downloader.download("/remote/path/file.txt", "/local/path/file.txt");

// Subir archivo
SFTPUploader uploader = new SFTPUploader(sftpConnect);
uploader.upload("/local/path/file.txt", "/remote/path/file.txt");
```

### Amazon S3

```java
import eu.sgax.connect.s3.S3Connect;
import eu.sgax.connect.s3.S3Downloader;
import eu.sgax.connect.s3.S3Uploader;

// Conectar a S3
S3Connect s3Connect = new S3Connect("us-east-1", "access-key", "secret-key");

// Descargar archivo
S3Downloader downloader = new S3Downloader(s3Connect);
downloader.download("mi-bucket", "remote-file.txt", "/local/path/file.txt");

// Subir archivo
S3Uploader uploader = new S3Uploader(s3Connect);
uploader.upload("mi-bucket", "/local/path/file.txt", "remote-file.txt");
```

### Mail

```java
import eu.sgax.connect.mail.SendMail;
import eu.sgax.connect.mail.ReadMail;

// Enviar correo
SendMail sendMail = new SendMail("smtp.gmail.com", 587, "tu@email.com", "password");
SendMail.EmailBuilder email = sendMail.new EmailBuilder()
    .to("destinatario@email.com")
    .subject("Asunto del correo")
    .body("Contenido del mensaje")
    .attachment("/path/to/archivo.pdf");
sendMail.send(email);

// Leer correos
ReadMail readMail = new ReadMail("imap.gmail.com", 993, "tu@email.com", "password");
List<ReadMail.EmailMessage> mensajes = readMail.readEmails("INBOX", 10);
```

## Requisitos

- Java 21 o superior
- Maven 3.6 o superior

## Licencia

Este proyecto está licenciado bajo la **Apache License, Version 2.0**. Consulta el archivo `LICENSE` para más detalles.

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Dependencias

Esta librería utiliza las siguientes dependencias de terceros:

- **AWS SDK for Java 2.x** (v2.40.15) - Para integración con Amazon S3
  - **Autor**: Amazon Web Services
  - **Proyecto**: https://github.com/aws/aws-sdk-java-v2
  - **Uso**: Proporciona acceso a servicios de AWS, especialmente para trabajar con Amazon S3

- **Apache Commons Net** (v3.11.1) - Para conexiones FTP
  - **Autor**: Apache Software Foundation
  - **Proyecto**: https://commons.apache.org/proper/commons-net/
  - **Uso**: Implementa protocolos de red como FTP, NNTP, SMTP, POP3 e IMAP

- **JSch** (v0.2.20) - Para conexiones SFTP
  - **Autor**: JCraft, Inc. (mantenido por mwiede)
  - **Proyecto**: https://github.com/mwiede/jsch
  - **Uso**: Proporciona cliente SSH2 para ejecutar comandos remotos y transferencia segura de archivos (SFTP)

- **Jakarta Mail** (v2.0.1) - Para operaciones de correo electrónico
  - **Autor**: Eclipse Foundation
  - **Proyecto**: https://github.com/eclipse-ee4j/mail
  - **Uso**: Implementa especificaciones SMTP, IMAP y POP3 para envío y lectura de correos electrónicos

- **SLF4J Simple** (v2.0.13) - Para logging
  - **Autor**: QOS.ch
  - **Proyecto**: https://www.slf4j.org/
  - **Uso**: Proporciona una capa de abstracción para sistemas de logging en Java

## Propietarios

- **Autor Principal**: SGAX
- **Repositorio**: https://github.com/deigo81/connect-libray
- **Organización**: SGAX
