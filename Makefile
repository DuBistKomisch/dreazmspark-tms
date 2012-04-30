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
	${JC} ${JARGS} -d ${BIN} ${SRC}/$*.java

# generate ptv.db
db: all
	${JX} ${JARGS} ParseTimetable content/tt

# dump database info
dump: all
	${JX} ${JARGS} DumpDatabase

# run the GUI
gui: all
	${JX} ${JARGS} GUI

# delete compiled files
clean:
	rm -rf ${BIN}

cleandb:
	rm -f ptv.db

lines:
	cat ${SOURCES} | wc -l
