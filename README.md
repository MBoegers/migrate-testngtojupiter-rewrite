# Migrate Test NG to JUnit Jupiter

This project assembles [Open Rewrite](https://docs.openrewrite.org/) Recipes to migrate unit tests suites from [TestNG](https://github.com/testng-team/testng) to [JUnit Jupiter](https://github.com/junit-team/junit5).

> [!NOTE]  
> For the authors it is very important to state: Using TestNG is perfectly fine! 
> If you decide to migrate for what ever reasons, these recipes are your way to go.

## Getting Help

You can get help to writing Open Rewrite Recipes you can join the [Open Rewrite Slack](https://join.slack.com/t/rewriteoss/shared_invite/zt-nj42n3ea-b~62rIHzb3Vo0E1APKCXEA) .

To get help regarding [TestNG](https://github.com/testng-team/testng) or [JUnit Jupiter](https://github.com/junit-team/junit5) see the project pages.

If you have problems regarding the recipes within this artifact feel free to contact me in the testing channel at the [jvm-german Slack](jvm-german.slack.com) or directly via [@MBoegie@fosstodon.org](https://fosstodon.org/@MBoegie) on Mastodon. 

## Development

For further information see the Open Rewrite docs section [Authoring Recipes](https://docs.openrewrite.org/authoring-recipes).

### Building from Source

Open Rewrite recipe artifacts are usually build with gradle.
The build is 100% taken from the [moderneinc/rewrite-recipe-starter](https://github.com/moderneinc/rewrite-recipe-starter) and depends on Java 11.

### Installing in Local Maven Repository

The artifact can be built and tested with the command ``./gradlew build``.
Publishing to your local maven repository (i.e. _~/.m2/repository_) with ``./gradlew publishToMavenLocal``.

## Contributing

If you find bugs or missing migrations feel free to file an issue or submit a pull request.
