if "%~1"=="" (
    echo "Correct Usage: run.bat <test/full> <debug/no>" 
    goto :eof
)
if "%~2"=="" (
    echo "Correct Usage: run.bat <test/full> <debug/no>"
    goto :eof
)

java -cp classes/;.;lib/* -Xmx1024M -DWNSEARCHDIR=lib/wordnet-3.0/dict edu.asu.nlp.fall.CreateDependencyParse %1 %2