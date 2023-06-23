# ChatGPT plugin for Eclipse / [EquoIDE](https://github.com/equodev/equo-ide)

[![Maven Plugin](https://img.shields.io/maven-central/v/dev.equo.ide/equo-ide-chatgpt?color=blue&label=dev.equo.ide%3Aequo-ide-chatgpt)](https://search.maven.org/artifact/dev.equo.ide/equo-ide-chatgpt)
[![Changelog](https://img.shields.io/badge/changelog-here-blue)](CHANGELOG.md)

This plugin makes it easy to use ChatGPT as a coding assistant with Eclipse. You do not need an OpenAI API key, and if you are a ChatGPT Plus subscriber then you can already use GPT4, which in our experience is currently far more useful than the other available chat engines.

You can drag files from the package explorer and drop them right into prompts. For a demo of this plugin in action along with GitHub Copilot-style autocompletion provided by Tabnine, see [equo-ide-ai-example](https://github.com/equodev/equo-ide-ai-example).

![screenshot of the ChatGPT plugin in action](screeenshot.png)

## Installation

This plugin is currently available on MavenCentral (TODO), but [not yet published as a p2 update site](https://github.com/equodev/equo-ide-chatgpt/issues/1).

It is included in the EquoIDE plugin [catalog](https://github.com/equodev/equo-ide/blob/main/CATALOG.md#chatgpt), so you can install it like so in Gradle and maven

```gradle
equoIde {    // in maven: <configuration>
  chatGpt()  //             <chatGpt/>
```

## Contributing

PR's are welcome, and will be merged and released in a timely fashion. Run `gradlew equoIde` and you'll have an Eclipse IDE with this project imported. If you make any changes to this plugin, you can close the IDE and run `equoIde` again to see your changes instantly.

## License 
This component's licensing is a bit unusual.

In order to bridge the Eclipse IDE and ChatGPT's web interface, we use [Equo Chromium](https://www.equo.dev/chromium), which is available under either a commercial license or GPL. This plugin is therefore licensed under the GPL with explicit permission to combine the work with Eclipse, as per the [FSF's recommendation](https://www.fsf.org/blogs/licensing/using-the-gpl-for-eclipse-plug-ins). 