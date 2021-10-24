all: 
	make compile
	make run

compile:
	java -jar ../jtb132di.jar -te minijava.jj
	java -jar ../javacc5.jar minijava-jtb.jj
	javac Main.java
	javac Pair.java
	javac symbolTable.java
	javac offset_table.java
	javac v_tableVisitor.java
	javac CodeGenerator.java
	javac myOffsets.java
	

run:
	java Main foo/bar/Classes.java foo/bar/If.java

clean:
	rm -f *.class *.ll *~

