This project aims at created automated knowledge base from corpus with causal events. 
In order to run this project, the following java libraries have to be placed inside the 
lib folder:

ejml-0.23.jar
jackson-all-1.7.3.jar
jwnl-1.3.3.jar
opennlp-maxent-3.0.3.jar
opennlp-tools-1.5.3.jar
opennlp-tools-1.6.0.jar
opennlp-uima-1.5.3.jar
opennlp-uima-1.6.0.jar
slf4j-api.jar
slf4j-simple.jar
sqlite-jdbc-3.15.1.jar
stanford-corenlp-3.7.0-models.jar
stanford-corenlp-3.7.0.jar
stanford-parser-3.6.0-javadoc.jar
stanford-parser-3.6.0-models.jar
stanford-parser-3.6.0-sources.jar
stanford-parser.jar
stanford-postagger-3.6.0-javadoc.jar
stanford-postagger-3.6.0-sources.jar
stanford-postagger-3.6.0.jar
stanford-postagger.jar

In order to run the project on an Ubuntu machine:
1. execute compile.sh
2. execute run.sh

In order to run the project on an Windows machine:
1. execute compile.bat
2. execute run.bat
Command line argument for run : run.bat <test/full> <debug/no>
test/full : to run the Program on test/full corpus
debug/no : To show debug logs or not

After the program has been executed, a file will be created inside the output folder which contains a JSON file 
, named unordered_event.json, with the narrative chain.
A sample output JSON document is:

{
	"eventChains": [{
		"eventChain": [{
			"object": "ball",
			"verb": "throw",
			"subject": "john"
		}, {
			"object": "william",
			"verb": "hit",
			"subject": "ball"
		}]
	}]
}

The eventChains object in the document will contain an array of event chains. 