#!/bin/sh

S := $(if $(shell uname|egrep "^CYG"),;,:)
CP=lib/spoon-core-1.3-jar-with-dependencies.jar

SRC=tested
LIB=
ARGS=-p Java2AS3Processor -i $(SRC) --no

J2MECP=(S)lib/midpapi20.jar$(S)lib/cldapi11.jar$(S)lib/commons-math-1.1.jar
RUNCP=$(CP)$(S)$(J2MECP)$(S)lib/junit-4.3.1.jar$(S)src$(S)$(LIB)

compile:
	javac -cp "$(CP)" src/*.java

run:
	rm -rf spooned as3 as3.swc
	java -Xmx512M -cp "$(RUNCP)" spoon.Launcher $(ARGS)

clean:
	rm -rf src/*.class spooned as3 as3.swc catalog.xml library.swf

build:
	sh build.sh
