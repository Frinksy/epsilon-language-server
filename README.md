# Epsilon Language Server

A language server implementation for the [Epsilon](epsilon) family of languages. It currently only supports EOL.


# Build instructions

Build and install the static-analysis framework (forked):
```sh
git clone https://github.com/Frinksy/static-analysis -b fix/build
cd static-analysis/org.eclipse.epsilon.eol.staticanalyser
mvn clean install
```


Then from the root of the `epsilon-language-server` repository:
```sh
mvn package
```

The jar file will be in `target/epsilonls-0.0.1-SNAPSHOT-jar-with-dependencies.jar`



[epsilon]: https://www.eclipse.org/epsilon/