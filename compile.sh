find ./src  -name '*.java' | xargs javac -cp ".:lib/*" -d ./classes
