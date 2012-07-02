JAVA_EXEC_PATH=$(dirname $1); shift;
JSYNC_JAR_PATH=$1; shift;
LIBJSYNC_PATH=$1; shift;

cat <<EOF
#!/bin/sh
PATH=\$PATH:$JAVA_EXEC_PATH;
export LIBJSYNC_PATH=$LIBJSYNC_PATH;
exec java -cp "$JSYNC_JAR_PATH" com.jsync.JSync "\$@";
EOF