#!/bin/bash

# Change to core dir
cd /apps/install/core

# Set timezone if provided
if [ ! -z "$TIMEZONE" ]; then
  echo "Setting timezone to $TIMEZONE"
  ln -snf /usr/share/zoneinfo/$TIMEZONE /etc/localtime && echo $TIMEZONE > /etc/timezone
fi

# Set memory
if [ -z "$HEAP_SIZE" ]; then
  export HEAP_SIZE=1g
  echo "Defaulting HEAP_SIZE to $HEAP_SIZE"
fi

# Set GC settings
if [ -z "$GC_FLAGS" ]; then
  export GC_FLAGS="-Xmx$HEAP_SIZE -Xms$HEAP_SIZE -XX:+UseG1GC -XX:MaxGCPauseMillis=200"
  echo "Defaulting GC_FLAGS to $GC_FLAGS"
fi

# Log other settings
if [ ! -z "$JAVA_OPTS" ]; then
  echo "JAVA_OPTS is set to $JAVA_OPTS"
fi

# Decrypt parameters and properties file if needed
if [ ! -z "$PROPERTIES_FILE" ]; then
  echo "Found properties file $PROPERTIES_FILE ..."
  mkdir -p $APP_PROPERTIES_PATH
  echo "Copying $PROPERTIES_FILE to target $APP_PROPERTIES_PATH/$APP_PROPERTIES_FILE"
  yes | cp -f $PROPERTIES_FILE $APP_PROPERTIES_PATH/$APP_PROPERTIES_FILE

  # Set the classpath variable
  export EXTERNAL_CONFIG="--spring.config.additional-location=$APP_PROPERTIES_PATH/"
fi

# Install certs from default location
if [ -d "$CERTS_PATH" ]; then
  for filename in $CERTS_PATH/*; do
    cert_name=$(echo "$filename" | sed "s/.*\///")
    echo "Importing certificate $cert_name into Java keystore..."
    $JAVA_HOME/bin/keytool -noprompt \
      -importcert -keystore $JAVA_HOME/lib/security/cacerts \
      -storepass changeit \
      -file "$filename" -alias "$cert_name"
  done
fi

# Run cmd
echo "Executing CMD: $@"
exec "$@"
