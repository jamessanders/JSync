bin_PROGRAMS = native/target/jsync

java/target/JSync-$(VERSION)-jar-with-dependencies.jar: java/target/JSync-$(VERSION).jar
	(cd java; $(MAVEN) assembly:single -Dversion=$(VERSION))

java/target/JSync-$(VERSION).jar: native/target/libjsync.so $(SRC_FILES) java/src/main/java/com/jsync/*.java
	$(MAVEN) install -Dversion=$(VERSION)

native/target/libjsync.so: native/src/libjsync.c
	$(MAVEN) install -Dversion=$(VERSION)

native/target/jsync: native/target java/target/JSync-$(VERSION)-jar-with-dependencies.jar mkexec.sh
	sh mkexec.sh $(JAVA_EXEC_PATH) $(libdir)/JSync/JSync.jar $(libdir)/JSync > native/target/jsync

native/target:
	mkdir native/target

install: 
	-mkdir $(libdir)/JSync
	install -o root -g `id -n -g root` java/target/JSync-$(VERSION)-jar-with-dependencies.jar $(libdir)/JSync/JSync.jar
	install -o root -g `id -n -g root` native/target/libjsync.so $(libdir)/JSync/
	install -o root -g `id -n -g root` native/target/jsync $(bindir)