# Migrate Test NG to JUnit Jupiter

This project assembles [Open Rewrite](https://docs.openrewrite.org/) Recipes to migrate unit tests suites
from [TestNG](https://github.com/testng-team/testng) to [JUnit Jupiter](https://github.com/junit-team/junit5).

> [!NOTE]  
> For the authors it is very important to state: Using TestNG is perfectly fine!
> If you decide to migrate for what ever reasons, these recipes are your way to go.

## Getting Help

You can get help to writing Open Rewrite Recipes you can join
the [Open Rewrite Slack](https://join.slack.com/t/rewriteoss/shared_invite/zt-nj42n3ea-b~62rIHzb3Vo0E1APKCXEA) .

To get help regarding [TestNG](https://github.com/testng-team/testng)
or [JUnit Jupiter](https://github.com/junit-team/junit5) see the project pages.

If you have problems regarding the recipes within this artifact feel free to contact me in the testing channel at
the [jvm-german Slack](jvm-german.slack.com) or directly via [@MBoegie@fosstodon.org](https://fosstodon.org/@MBoegie) on
Mastodon.

## Development

For further information see the Open Rewrite docs
section [Authoring Recipes](https://docs.openrewrite.org/authoring-recipes).

### Building from Source

Open Rewrite recipe artifacts are usually build with gradle or maven.
This repository uses maven, the build is 100% taken from
the [moderneinc/rewrite-recipe-starter](https://github.com/moderneinc/rewrite-recipe-starter) and depends on Java 17.

### Main Ideas

The Recipes in this artifact follow a few main ideas, to kickstart other developer they are shortly describe here.

#### Annotation Migration

The TestNG annotation ``@org.testng.annotations.Test`` is responsible to configure the test.
Within JUnit Jupiter all configurations are made thought dedicated annotations.
This implies that all configuration have to be migrated **before** the Test annotations can be exchanged.
To respect this
the [MigrateTestAnnotation](src/main/java/io/github/mboegers/openrewrite/testngtojupiter/MigrateTestAnnotation.java)
recipe only migrates Test annotations without any parametes and the recipes that migrate the configurations (
f.e. [MigrateEnabledArgument](src/main/java/io/github/mboegers/openrewrite/testngtojupiter/MigrateEnabledArgument.java))
have to remove the configuration after migration

#### Semantic Assumptions

The semantics of TestNG and JUnit Jupiter does not alight 100% straight forward.
There is a list maintained by the JUnit Team addressing the migration from TestNG to JUnit
Jupiter [](https://github.com/junit-team/junit5/wiki/Migrating-from-TestNG-to-JUnit-Jupiter).
In any case these summary should be consulted first.

## Contributing

If you find bugs or missing migrations feel free to file an issue or submit a pull request.
