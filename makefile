ARGS=-p Java2AS3Processor -i tested -f --no

compile:
	javac -cp "lib/spoon-1.2.jar;." src/*.java

run:
	rm -rf spooned
	java -cp "lib/spoon-1.2.jar;src" spoon.Launcher $(ARGS)

clean:
	rm -rf src/*.class spooned
