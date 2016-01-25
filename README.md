__Status__:
[![Build Status](https://travis-ci.org/Codarama/diet-engine.svg?branch=master)](https://travis-ci.org/Codarama/diet-engine)
[![Coverage Status](https://coveralls.io/repos/Codarama/Diet/badge.png)](https://coveralls.io/r/Codarama/Diet)
[![Static Code Analysis Status](https://scan.coverity.com/projects/6324/badge.svg)](https://scan.coverity.com/projects/codarama-diet-engine)
[![Dependency Status](https://www.versioneye.com/user/projects/557ed6bc61626613800000f7/badge.svg?style=flat)](https://www.versioneye.com/user/projects/557ed6bc61626613800000f7)

__Chat with us__:
[![Chat at https://gitter.im/Codarama/diet-engine](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/Codarama/diet-engine?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Diet
======

Diet tries to create a jar from all of the jars used by a project.
The result jar should contain only classes the project actually uses, nothing else.

This is currently just a library, but it should [evolve to a Maven plugin](https://github.com/Codarama/diet-maven-mojo), an eclpse plugin and perhaps a web project.

It's main target group is currently android development as space conservation is mostly uneeded for web development.

## Features

### Source Dependency Resolution:

Diet can resolve the dependencies of .java source files. 
Currently like this:

```java
final Set<ClassName> dependencies = Dependencies
    .ofSource(SourceFile.fromFilepath("/abs/path/to/Source.java"))
    .set();
```

### Binary Class Dependency Resolution:

Diet can also resolve the dependencies of compiled binary .class files:

```java
final Set<ClassName> dependencies = Dependencies
    .ofClass(ClassFile.fromFilepath("/abs/path/to/Class.class"))
    .set();
```

The `Dependencies` API can also work with the classpath or with Core API `File` objects (no streams yet though).
For more info on the `Dependencies` API have [a look at the wiki](https://github.com/ayld/Facade/wiki/Dependencies-API).

### Library minimization:

This will try to find all the 'actual dependencies' that a set of sources use, package them in a Jar and return it.

```java
final JarFile outJar = Minimizer
    .sources("/abs/path/to/src/dir")
    .libs("/abs/path/to/libs") // this can also be a Maven ~/.m2/repository
    .getJar();
```

You can also set the output dir for the minimizer, have [a look at the wiki](https://github.com/ayld/Facade/wiki/Library-Minimization) for info.

### Component Events:

Diet can notify you for updates on what it is currently doing. For instanse if you want to get detailed info while 
resolving the dependencies of a binary class you can:

```java
ListenerRegistrar.listeners(new Object() {
			
    @Subscribe
	public void listenOnStart(ClassDependencyResolutionStartEvent e) {
	    // this will be called when the resolution starts
	}
			
	@Subscribe
	public void listenOnEnd(ClassDependencyResolutionEndEvent e) {
	    // this will be called when the resolution ends
	}
}).register();
```

There is a whole hierarchy of events you can listen to, there is [a wiki page](https://github.com/ayld/Facade/wiki/Component-Events-and-Listeners) on this also.

### Usage:

In order to use the library you can either:

 * [Download the latest binary .jar](https://github.com/codarama/Diet/releases/tag/v0.6-alpha.1),
in which case you will also need the dependencies. You can either find them [in the POM](https://github.com/Codarama/diet-engine/blob/master/pom.xml),
or download them from [the dependencies project](https://github.com/ayld/facade-dependencies).
 * Build [the latest tag](https://github.com/codarama/Diet/releases/tag/v0.6-alpha.1) with [Maven 3.x](http://maven.apache.org/). Just
clone it and run `mvn clean install` in your local copy. This way Maven will get the dependencies for you.
 * [Gradle](https://gradle.org/) can also be used to build the sources, [we have a separate branch for it](https://github.com/Codarama/diet-engine/tree/gradle-build) for now.

You can also build the master branch (on your own risk) in the same way you build the latest tag.

### Notes:

Keep in mind that we're currently in very early alpha and the API changes constantly and can change dramatically :)
Also wildcard imports in source files like `import com.something.*;` are currently not supported. So calling source
dependency resolution on such a file or on a set containing one will result in an exception.

### Donations:

[![Fund me on Gittip](https://raw.github.com/gittip/www.gittip.com/master/www/assets/gittip.png)](https://www.gittip.com/ayld/ "Fund me on Gittip")
