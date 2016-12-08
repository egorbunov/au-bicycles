# AU Shell

Pivotl tracker project: https://www.pivotaltracker.com/n/projects/1932763

That is simple REPL command interpreter. It supports basic built-in commands:
* cat
* echo
* wc
* exit
* pwd
* grep 

Also you can:

- Add variables like in sh: `X="value"` and use them also the same way: `$X` or `${X}`.
- Run external commands like `bash` of `ls` (use absolute path or put them in your path before)
- Construct piped commands like `echo HELLO | wc` to redirect output and input

## Build and run

* Run `./gradlew build` from project root directory to build project
* After build runnable jar will be available at `./build/libs` directory, so run it with `java -jar`

### Exaple session

```bash
aush >> /bin/ls
build
build.gradle
gradle
gradlew
gradlew.bat
README.md
settings.gradle
src
aush >> /bin/ls | wc
8 8 76
aush >> PRINT_MY_WORKING_DIRECTORY=pwd
aush >> ${PRINT_MY_WORKING_DIRECTORY}
/home/user/repos/au-bicycles/task1-shell
aush >> exit
```

## Class diagram sketch
![Diagram](diagram.png)