FROM centos:6

ENV JAVA_VERSION 8u31
ENV BUILD_VERSION b13

RUN rpm --rebuilddb && yum update -y && yes | yum install -y rpm-build && yum -y install wget && yum install -y rpm

#install maven & set MAVEN_HOME
RUN cd /opt
RUN wget https://archive.apache.org/dist/maven/maven-3/3.5.0/binaries/apache-maven-3.5.0-bin.tar.gz
RUN tar xzf apache-maven-3.5.0-bin.tar.gz -C /usr/local
RUN mv /usr/local/apache-maven-3.5.0 /usr/local/maven
ENV M2_HOME /usr/local/maven
ENV PATH ${M2_HOME}/bin:${PATH}

# install java 8
# Downloading Java
RUN wget --no-cookies --no-check-certificate --header "Cookie: oraclelicense=accept-securebackup-cookie" "http://download.oracle.com/otn-pub/java/jdk/$JAVA_VERSION-$BUILD_VERSION/jdk-$JAVA_VERSION-linux-x64.rpm" -O /tmp/jdk-8-linux-x64.rpm

RUN yum -y install /tmp/jdk-8-linux-x64.rpm

RUN alternatives --install /usr/bin/java jar /usr/java/latest/bin/java 200000
RUN alternatives --install /usr/bin/javaws javaws /usr/java/latest/bin/javaws 200000
RUN alternatives --install /usr/bin/javac javac /usr/java/latest/bin/javac 200000

ENV JAVA_HOME /usr/java/latest

CMD cd /src && mvn clean site package
