JC = javac
JX = java
JLIBS = sqlitejdbc-v056.jar
JARGS = -cp .:${JLIBS}
TT = tt

CLASSES = ParseTimetable.java GUI.java DumpDatabase.java

.SUFFIXES: .java .class

all: ${CLASSES:.java=.class}

.java.class:
	${JC} $*.java

gui: all
	${JX} ${JARGS} GUI

parse: all
	for i in ${TT}/*.html; do echo "parsing $$i..."; ${JX} ${JARGS} ParseTimetable $$i; done

dump: all
	${JX} ${JARGS} DumpDatabase

archive:
	zip -r dtms.zip ${CLASSES} ${TT} ${JLIBS} Makefile README

clean:
	rm -f *.class ptv.db
