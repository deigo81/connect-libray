# Guía de Distribución - Connect Library

## Distribución en GitHub Packages

El proyecto está configurado para publicarse en GitHub Packages.

### Requisitos Previos

1. **Token de GitHub**: Necesitas un Personal Access Token (PAT) con permisos:
   - `write:packages`
   - `read:packages`

   Puedes crear uno en: https://github.com/settings/tokens

### Configuración de Maven

Agrega la siguiente configuración a tu archivo `~/.m2/settings.xml`:

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>github</id>
            <username>TU_USUARIO_GITHUB</username>
            <password>TU_TOKEN_PERSONAL</password>
        </server>
    </servers>
</settings>
```

### Publicar en GitHub Packages

Ejecuta el siguiente comando para publicar:

```bash
mvn clean deploy
```

### Usar la Librería en Otros Proyectos

Para usar esta librería en otro proyecto Maven, agrega lo siguiente a su `pom.xml`:

1. **Configurar el repositorio**:

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

## Distribución en Maven Central (Opcional)

Si deseas publicar en Maven Central, necesitarás:

1. **Cuenta en Sonatype**: Registrarse en https://issues.sonatype.org/
2. **Verificar dominio**: Verificar la propiedad del groupId `eu.sgax`
3. **GPG Key**: Para firmar los artefactos
4. **Actualizar pom.xml** con configuración de Sonatype:

```xml
<distributionManagement>
    <repository>
        <id>ossrh</id>
        <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
    </repository>
    <snapshotRepository>
        <id>ossrh</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
    </snapshotRepository>
</distributionManagement>
```

5. **Agregar plugin GPG**:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-gpg-plugin</artifactId>
    <version>3.2.7</version>
    <executions>
        <execution>
            <id>sign-artifacts</id>
            <phase>verify</phase>
            <goals>
                <goal>sign</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Generar Artefactos Localmente

Para generar los JARs (binario, fuentes y javadoc) sin publicar:

```bash
mvn clean package
```

Los artefactos se generarán en:
- `target/connect-library-1.0.jar` - JAR principal
- `target/connect-library-1.0-sources.jar` - Código fuente
- `target/connect-library-1.0-javadoc.jar` - Documentación

## Instalar Localmente

Para instalar en tu repositorio Maven local (~/.m2/repository):

```bash
mvn clean install
```

Luego puedes usarla en otros proyectos locales sin necesidad de publicar en un repositorio remoto.
