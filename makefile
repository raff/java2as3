SRC=tested
ARGS=-p Java2AS3Processor -i $(SRC) --no
S := $(if $(shell uname|egrep "^CYG"),;,:)

compile:
	javac -cp "lib/spoon-1.2.jar" src/*.java

run:
	rm -rf spooned as3 as3.swc
	java -Xmx512M -cp "lib/spoon-1.2.jar$(S)src" spoon.Launcher $(ARGS)

clean:
	rm -rf src/*.class spooned as3 as3.swc catalog.xml library.swf

build:
	sh build.sh
