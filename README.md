# Badger2D LibGdx extension module

This is an extension module

## Using Badger2D in a LibGdx project

The supported version of LibGdx is `1.9.12`. 

### Building with a local version of Badger2DCore and Badger2DExtGdx

#### Download the LibGdx setup app

If you already have a LibGdx project set up, it is assumed to use version `1.9.12` and have been generated using the setup app at some point. Otherwise, 
download the [setup app](https://libgdx.badlogicgames.com/download.html) and set up your project.

Make sure all modules of your project builds properly before adding Badger2D.

#### Checkout a local copy of Badger2D

Check out the projects
<br>
https://github.com/SystemInvecklare/Badger2DCore
<br>
and
<br>
https://github.com/SystemInvecklare/Badger2DExtGdx
<br>
locally with git.

The rest of the instructions assume your directory structure is the following:

```
someFolder/
├── MyLibGdxProject/
│   ├── core/
│   │   └── build.gradle
│   ├── desktop/    (possibly)
│   │   └── build.gradle
│   ├── html/       (possibly)
│   │   └── build.gradle
│   ├── android/    (possibly)
│   │   └── build.gradle
│   │
│   ├── build.gradle
│   └── settings.gradle
│
├── Badger2DCore/
│   └── build.gradle
└── Badger2DExtGdx/
    └── build.gradle
```

#### Use local version of Badger2DCore
Add the following in `MyLibGdxProject/build.gradle`:

```gradle
...
allprojects {
    ...
    configurations.all {
	    resolutionStrategy.dependencySubstitution {
	        substitute module("com.github.SystemInvecklare:Badger2DCore") using project(":Badger2DCore") because "Using local"
	    }
	}
}
...

```

#### Include projects in build
Add the following to `MyLibGdxProject/setting.gradle`:

```gradle
include 'Badger2DCore', 'Badger2DExtGdx'

project(':Badger2DCore').projectDir = file("../Badger2DCore")
project(':Badger2DExtGdx').projectDir = file("../Badger2DExtGdx") 
```


#### Add dependencies to core project
Add Badger2D modules as dependencies to the core project in `MyLibGdxProject/build.gradle`. 

```gradle
...
project(":core") {
	...
    dependencies {
        ...
        api project(":Badger2DCore") // <-- This
        api project(":Badger2DExtGdx") // <-- This
    }
    ...
}
...
```

#### (IF HTML MODULE) Add dependencies as GWT-dependencies to core project
Open `MyLibGdxProject\core\src\MyLibGdxProjectGame.gwt.xml` and add

```xml
<module>
	<inherits name='Badger2DCore' /> // <-- This line
	<inherits name='Badger2DExtGdx' /> // <-- This line
	
	<source path="..." />
</module>
```

For Gwt to find `Badger2DCore.gwt.xml` and `Badger2DExtGdx.gwt.xml` we need to add the projects to the source path for Gwt.

#### (IF HTML MODULE) Add Badger project sources to sources for html gwt
Append project source folders in addSources task in
`MyLibGdxProject\html\build.gradle`

```gradle
...
task addSource {
	...
    doLast {
    	...
        sourceSets.main.compileClasspath += files(project(':Badger2DCore').sourceSets.main.allJava.srcDirs)
        sourceSets.main.compileClasspath += files(project(':Badger2DExtGdx').sourceSets.main.allJava.srcDirs)
        ...
    }
}
...
```


## Basics

Extend `AbstractGdxGameApplicationAdapter` for quick and easy integration with LibGdx.

```java
public class MyLibGdxProjectGame extends AbstractGdxGameApplicationAdapter {
	
	public MyLibGdxProjectGame() {
		super(new FlashyGdxEngine());
	}
	
	@Override
	protected IScene getInitialScene() {
		return new EpicGameScene();
	}
}
```