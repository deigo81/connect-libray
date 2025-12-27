# Connect Library

Una biblioteca Java versátil que proporciona conectores para múltiples protocolos y servicios de transferencia de archivos y comunicación.

## Descripción

Connect Library es una librería diseñada para facilitar la integración con diversos sistemas de almacenamiento y transferencia de datos. Incluye implementaciones para:

- **FTP**: Transferencia de archivos mediante File Transfer Protocol
- **SFTP**: Transferencia segura de archivos mediante SSH File Transfer Protocol
- **S3**: Integración con Amazon S3 y servicios compatibles
- **Mail**: Lectura y envío de correos electrónicos

Cada módulo proporciona funcionalidades de conexión, descarga y carga de archivos.

## Características

- Soporte para múltiples protocolos de transferencia
- API unificada para operaciones comunes
- Manejo de autenticación y conexiones seguras
- Monitoreo de progreso en transferencias
- Manejo robusto de errores

## Módulos

- `eu.sgax.connect.ftp` - Conexiones FTP
- `eu.sgax.connect.sftp` - Conexiones SFTP
- `eu.sgax.connect.s3` - Integración con Amazon S3
- `eu.sgax.connect.mail` - Operaciones de correo electrónico

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

## Requisitos

- Java 21 o superior
- Maven 3.6 o superior

## Construcción

```bash
mvn clean install
```

## Autor

SGAX
