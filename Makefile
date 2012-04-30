JC = javac
JD = javadoc
JX = java
SRC = src
BIN = bin
LIB = lib
DOC = doc
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
	${JX} ${JARGS} ParseTimetable content/tt/train
	${JX} ${JARGS} ParseTimetable content/tt/bus

# dump database info
dump: all
	${JX} ${JARGS} DumpDatabase

# run the GUI
gui: all
	${JX} ${JARGS} GUI

# generate javadocs
doc:
	@mkdir -p ${DOC}
	${JD} -d ${DOC} ${SOURCES}

# delete compiled files
clean:
	rm -rf ${BIN}
	rm -rf ${DOC}

# delete database
cleandb:
	rm -f ptv.db

# print total line count (for funz)
lines:
	cat ${SOURCES} | wc -l
