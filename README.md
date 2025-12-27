# Connect Library

A Java library that allows connection to multiple protocols.

## Description

Connect Library is a library to facilitate connection to multiple protocols. It includes implementations for:

- **FTP** 
- **SFTP** 
- **S3** 
- **Mail**


## Modules

- `eu.sgax.connect.ftp` - FTP Connections
- `eu.sgax.connect.sftp` - SFTP Connections
- `eu.sgax.connect.s3` - Amazon S3 Integration
- `eu.sgax.connect.mail` - Email Operations

## Installation

### Add as Maven Dependency

To use this library in your Maven project, add the following to your `pom.xml`:

1. **Configure the GitHub Packages repository**:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/deigo81/connect-libray</url>
    </repository>
</repositories>
```

2. **Add the dependency**:

```xml
<dependency>
    <groupId>eu.sgax</groupId>
    <artifactId>connect-library</artifactId>
    <version>1.01</version>
</dependency>
```

3. **Configure authentication** in `~/.m2/settings.xml`:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_GITHUB_TOKEN</password>
        </server>
    </servers>
</settings>
```

## Usage

### FTP

```java
import eu.sgax.connect.ftp.FTPConnect;
import eu.sgax.connect.ftp.FTPDownloader;
import eu.sgax.connect.ftp.FTPUploader;

// Connect to FTP server
FTPConnect ftpConnect = new FTPConnect("ftp.example.com", 21, "user", "password");

// Download file
FTPDownloader downloader = new FTPDownloader(ftpConnect);
downloader.download("/remote/path/file.txt", "/local/path/file.txt");

// Upload file
FTPUploader uploader = new FTPUploader(ftpConnect);
uploader.upload("/local/path/file.txt", "/remote/path/file.txt");
```

### SFTP

```java
import eu.sgax.connect.sftp.SFTPConnect;
import eu.sgax.connect.sftp.SFTPDownloader;
import eu.sgax.connect.sftp.SFTPUploader;

// Connect to SFTP server
SFTPConnect sftpConnect = new SFTPConnect("sftp.example.com", 22, "user", "password");

// Download file
SFTPDownloader downloader = new SFTPDownloader(sftpConnect);
downloader.download("/remote/path/file.txt", "/local/path/file.txt");

// Upload file
SFTPUploader uploader = new SFTPUploader(sftpConnect);
uploader.upload("/local/path/file.txt", "/remote/path/file.txt");
```

### Amazon S3

```java
import eu.sgax.connect.s3.S3Connect;
import eu.sgax.connect.s3.S3Downloader;
import eu.sgax.connect.s3.S3Uploader;

// Connect to S3
S3Connect s3Connect = new S3Connect("us-east-1", "access-key", "secret-key");

// Download file
S3Downloader downloader = new S3Downloader(s3Connect);
downloader.download("my-bucket", "remote-file.txt", "/local/path/file.txt");

// Upload file
S3Uploader uploader = new S3Uploader(s3Connect);
uploader.upload("my-bucket", "/local/path/file.txt", "remote-file.txt");
```

### Mail

```java
import eu.sgax.connect.mail.SendMail;
import eu.sgax.connect.mail.ReadMail;

// Send email
SendMail sendMail = new SendMail("smtp.gmail.com", 587, "your@email.com", "password");
SendMail.EmailBuilder email = sendMail.new EmailBuilder()
    .to("recipient@email.com")
    .subject("Email subject")
    .body("Message content")
    .attachment("/path/to/file.pdf");
sendMail.send(email);

// Read emails
ReadMail readMail = new ReadMail("imap.gmail.com", 993, "your@email.com", "password");
List<ReadMail.EmailMessage> messages = readMail.readEmails("INBOX", 10);
```

## Requirements

- Java 21 or higher
- Maven 3.6 or higher

## License

This project is licensed under the **Apache License, Version 2.0**. See the `LICENSE` file for more details.

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

## Dependencies

This library uses the following third-party dependencies:

- **AWS SDK for Java 2.x**
  - **Author**: Amazon Web Services
  - **Project**: https://github.com/aws/aws-sdk-java-v2

- **Apache Commons Net**
  - **Author**: Apache Software Foundation
  - **Project**: https://commons.apache.org/proper/commons-net/

- **JSch**
  - **Author**: JCraft, Inc. (maintained by mwiede)
  - **Project**: https://github.com/mwiede/jsch

- **Jakarta Mail**
  - **Author**: Eclipse Foundation
  - **Project**: https://github.com/eclipse-ee4j/mail

- **SLF4J Simple**
  - **Author**: QOS.ch
  - **Project**: https://www.slf4j.org/

## Owners

- **Main Author**: SGAX
- **Repository**: https://github.com/deigo81/connect-libray
- **Organization**: SGAX
