# Lapp

*lapp* is a tool to iteratively build a call graph for maven artifacts and their (transitive) dependencies.

## Building
*lapp* uses maven for building, so building is as easy as executing `mvn clean compile package`.
This will generate a jar with all dependencies in the *target* folder.

## Usage
The lapp application contains multiple sub commands. 

- resolve
- callgraph
- merge

An explanation of the sub commands can be found below.

### resolve
The `lapp resolve` command can resolve a dependency tree for a maven package for a specific moment in time.
Dependencies declared with a version range will resolve to the latest version available at the date given.
By default a version is expected, for this version the release date will be found with libraries.io.
This date will then be used to resolve dependencies to.

The jar files of the artifact with all its transitive dependencies will be downloaded to the output directory.
Also a file called 'classpath.txt' will be created in the output directory. 
This file contains two lines, the first line of this file will contain an absolute path to the jar file of the resolved artifact.
The second line will contain absolute paths to all the dependencies separated by a colon.


Usage help as available with `lapp help resolve`:

```
Usage: lapp resolve [-dhV] -k=<apiKey> [-o=<outputDirectory>]
                    [-s=<apiBaseUrl>] package_identifier version
Resolve and download dependencies for a maven artifact for a specific date or
version in history.
      package_identifier   Maven artifact to analyze (format: [groupId]:[artifactId]
                             eg. com.company.app:my-app).
      version              Package version or if --date is used the date used for
                             finding the latest version.
  -d, --date               Use version as date to determine the version to use
  -h, --help               Show this help message and exit.
  -k, --api-key=<apiKey>   Libraries.io api key, see https://libraries.io/account
  -o, --output=<outputDirectory>
                           Output folder
  -s, --api-source=<apiBaseUrl>
                           Url to use for custom project version-date source,
                             defaults to Libraries.io
  -V, --version            Print version information and exit.
```


### callgraph
The `lapp callgraph` command can take the classpath file output from the 
resolve command and will create an intermediate call graph for a single artifact. 
This intermediate call graph file will be stored in the output directory as `app.dot`

Usage help as available with `lapp help callgraph`:
```
Usage: lapp callgraph [-hV] [-e=<exclusionFile>] [-o=<outputDirectory>]
                      [jars...]
Create a call graph from resolve output
      [jars...]   Application/Libary jars to analyse, first jar will be considered
                    as main jar
  -e, --exclusion=<exclusionFile>
                  Location of exclusion file
  -h, --help      Show this help message and exit.
  -o, --output=<outputDirectory>
                  Output folder
  -V, --version   Print version information and exit.
```
