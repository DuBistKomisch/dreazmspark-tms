JC = javac
JX = java
SRC = src
BIN = bin
LIB = lib
JARGS = -cp ${BIN}:${LIB}/*

SOURCES = ${wildcard ${SRC}/*.java}
CLASSES = ${patsubst ${SRC}/%.java,${BIN}/%.class,${SOURCES}}

# compile all the files
all: ${CLASSES}

# how to make a .java into a .class
${CLASSES}: ${BIN}/%.class:${SRC}/%.java
	@mkdir -p ${BIN}
	${JC} -d ${BIN} ${SRC}/$*.java

# generate ptv.db
db: all
	@for i in content/tt/*.html; do echo "parsing $$i..."; ${JX} ${JARGS} ParseTimetable $$i; done

# dump database info
dump: all
	@${JX} ${JARGS} DumpDatabase

# run the GUI
gui: all
	@${JX} ${JARGS} GUI

# delete unnecesary files
clean:
	rm -rf ${BIN} ptv.db
