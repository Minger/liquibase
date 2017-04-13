liquibase-rpm
==============

Run :

mvn site


and take a look at

target/site/build-rpm.html

to get requirements to be able to build liquibase's rpm package.

## Docker
A Dockerfile has been added for building the rpm for Centos 6 on other operating systems.

```
docker run -it -v `pwd`:/src rpm-build
```

will build the rpm
```
src/target/rpm/liquibase/RPMS/noarch/liquibase-3.5.3-1.noarch.rpm 
```

## RPM

This RPM has been modified, among other things, to require `open-jdk-1.8.0`.  Yum will install this dependency if necessary.

One difference in running this RPM's liquibase compared to the tarball's is that the postgres jdbc driver is bundled as an included dependency: while you may need to specify a `classpath` in `liquibase.properties` running the tarball's liquibase, you must comment out that line running the RPM's.

