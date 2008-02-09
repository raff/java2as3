#!/bin/sh

S := $(if $(shell uname|egrep "^CYG"),;,:)
CP=lib/spoon-core-1.4-jar-with-dependencies.jar

SRC=tested

JAR=Java2AS3.jar
RUNCP=$(CP)$(S)$(JAR)

jar: compile
	cd classes; jar cvf ../$(JAR) *.class

compile:
	javac -cp "$(CP)" -d classes src/*.java

run:
	rm -rf spooned as3 as3.swc
	java -Xmx512M -cp "$(RUNCP)" Main -i $(SRC)

clean:
	rm -rf $(JAR) classes/*.class spooned as3 as3.swc catalog.xml library.swf

build:
	sh build.sh
