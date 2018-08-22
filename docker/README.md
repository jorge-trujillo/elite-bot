# Docker Spring Boot base image

This image is meant to be a base for Java applications that will leverage the Spring Boot 2.x framework. It is based on OpenJDK base images available on the Docker Hub.

## TLDR; I have a simple image man! Get to the point!

For *simple* cases, here is probably the minimum you will need to do:

1. Get a jar file into your derived image and set the `$JAR_PATH` parameter. You may just want to do this in your Dockerfile.
1. Import your properties by mounting it in a volume, and setting the `$PROPERTIES_FILE` environment variable.
1. If it's an `application-{env}.yaml` file, set the `$APP_PROPERTIES_FILE` property too, or it shall default to `application-override.properties`
1. Set some reasonable `$HEAP_SIZE` for your app as an environment variable.

That's it! Not that bad...

## Parameters

The following environment variables can be set to control the image or downstream images:

| Name | Description | Required |
| ---- | ----------- | -------- |
| **HEAP_SIZE** | Size of the heap. Feeds into the GC_FLAGS, but will not be used if you override GC_FLAGS. | No, default is `1g` |
| **GC_FLAGS** | Flags to pass the JVM. Default to G1 GC. | No |
| **JAVA_OPTS** | Extra options to pass the JVM | No |
| **SPRING_OPTS** | Extra options to pass to Spring | No |
| **JAR_FILE** | JAR file to execute | Yes, unless you override the CMD in a downstream image |
| **PROPERTIES_FILE** | Path to properties file that has any property overrides for the application. This can be encrypted if needed. | No |
| **APP_PROPERTIES_FILE** | File name for the properties file as it will be imported into the application. | No, defaults to `application-override.properties` |
| **$CERTS_PATH** | Path to optional certificates that should be imported into the keystore. | No, default path is `/apps/install/certs` |
| **TIMEZONE** | Timezone to set the container to. For instance, `America/New_York`. Defaults to CST | No |

## Certificates

Certificates can be automatically imported into the JVM's certificate store. All you need to do is mount them to the image and set the `$CERTS_PATH` if you are using a path other than the default `/apps/install/certs`.

## Usage

To use this base image, reference it in your `FROM` directive, and add code and resources as needed. For example:

```
FROM docker-registry:5000/merch-tools/spring-boot-base:1.5
MAINTAINER Joe Maintainer <joe.maintainer@target.com>

# Add the app
RUN mkdir -p /opt/app/
COPY files/app.jar /opt/app/

ENV JAR_FILE /opt/app/app.jar
```

Note that the CMD has a reasonable preset to run Java with the file provided in `JAR_FILE`. You can override parameters as needed, or override the entire CMD. Even if you override the CMD, you can still use variables like `GC_FLAGS`:

```
# Set start command
CMD java $GC_FLAGS -jar /opt/app/app.jar
```

## Running a container based on this image

Here is a complete example, leveraging a properties file:

```bash
docker run --rm -ti \
  -e PROPERTIES_FILE=/apps/mount/dummy.properties \
  -v /path/to/app.properties.encrypted:/apps/mount/app.properties.encrypted:ro \
  -v private_key.pem:/apps/install/core/private_key.pem:ro \
   docker-registry:5000/merch-tools/docker-java-app:latest
```
