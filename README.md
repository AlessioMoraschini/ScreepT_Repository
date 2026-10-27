# ScreepT

A desktop toolbox for technicians and developers: a text/script editor with a built-in crypter, steganographic key images, a plugin system and a handful of small utilities. Swing, Java 8, Windows desktop.

This is a **personal side project**, built purely out of love for software made for technical people. It has no commercial or utilitarian goal and no roadmap promised to anyone: things get done when I have time and desire.

## What's inside

- **Text editor**: tabs, syntax highlighting for many languages (RSyntaxTextArea), rectangular selection (Alt+drag), search/replace, text transformations, spell checker, themes (including the **ScreepT Dark** editor theme), auto-completion, minimap.
- **Crypter**: encrypt/decrypt text and files, optionally combined with a *key image* (steganographic second factor).
- **Quick notes**: an auto-saving to-do drawer (Ctrl+Alt+N) stored as plain Markdown.
- **Utilities**: file hasher, color picker, calculator, clipboard history, code comparator, Markdown viewer, media converters.
- **Look and feel**: *ScreepT Dark* (FlatLaf based) plus other LAFs, adjustable UI size, rich Graphic Preferences.
- **Home page assistant**: a small offline terminal on the home page that answers questions about ScreepT, using the guide and the changelog (no cloud, no keys).
- **Plugins** and a built-in **in-app updater**.
- A few easter eggs, because why not.

User-facing release notes live in [`ScreepT/CHANGELOG.md`](ScreepT/CHANGELOG.md) (also shown in the app under *Help > What's new*). The old tutorial, written for v0.9.17 and partly outdated, is kept in [`TUTORIAL.md`](TUTORIAL.md).

## Requirements

| Tool | Version | Notes |
|------|---------|-------|
| JDK | **8** (with JavaFX, e.g. Oracle 8u162) | Always Java 8, both to build and to run. A newer `java` in `PATH` is **not** supported. |
| Maven | **3.8.x** | Maven 3.9 fails: the unversioned antrun plugin resolves to a release that rejects `<tasks>`. |
| Git | any | |
| OS | Windows | Native file dialogs use JNA/IFileDialog. |

## Workspace layout

The two repositories must sit **side by side** in the same root folder (no spaces in folder names):

```
Root_DIR
├── ScreepT_Repository   <- this repo: ScreepT (main app), GuiTextEditorCommons, NetworkCommons, ScreepT_Crypter, ScreepT_Launcher
├── ScreepT-public       <- common libraries and plugins (CommonParentGlobal, ScreepT_Parent, GuiCommons, ...)
└── ScreepT_Build        <- created by the builds (application, update data). Not a git repo.
```

```
git clone https://github.com/AlessioMoraschini/ScreepT_Repository.git
git clone https://github.com/AlessioMoraschini/ScreepT-public.git
```

Both repositories use the `master` branch. The application version is defined in one place: `screept.version` in `ScreepT-public/CommonParentGlobal/pom.xml`.

## Build

Put the JDK 8 first in `PATH` (the launcher starts the app with the `java` found in `PATH`), then, from `ScreepT-public/ScreepT_Parent`:

```bat
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_162
set PATH=%JAVA_HOME%\bin;<maven-3.8.x>\bin;%PATH%
set MAVEN_OPTS=-Dfile.encoding=UTF-8

mvn clean install -f pomWithPlugins.xml -DskipTests -Dproject.build.sourceEncoding=UTF-8
```

- Add `-o` to work offline when dependencies are already in `~/.m2`.
- Add `-P defaultAndEmbeddedPlugins` to also build the embedded plugins.
- Outside Eclipse the two encoding flags are needed, because the poms use a custom `sourceEncoding` property.
- The changelog generation step (`generate_ChangeLogLauncher.bat`) fails from the command line. If the build stops there, finish it from `ScreepT_Repository/ScreepT`:

  ```bat
  mvn install -o -Dskip=true -DskipTests -Dproject.build.sourceEncoding=UTF-8
  ```
- Some plugins are not version-pinned (antrun, the license plugin): avoid building online unless needed.

Output goes to `ScreepT_Build`:

- `ScreepT_V<version>/Application`: the runnable application
- `LATEST_UPDATES_DATA`: files for the in-app updater
- `LATEST_PLUGINS_DATA`: plugins, when built with plugins enabled

## Run

```bat
cd ScreepT_Build\ScreepT_V<version>\Application
javaw -jar ScreepT_Launcher.jar
```

`<version>` is the current `screept.version`. Check the folder and the jar date: older version folders may still be around. See also `ScreepT.bat`.

## Working on the code

### Modules

| Where | Module | Purpose |
|-------|--------|---------|
| `ScreepT_Repository` | `ScreepT` | Main application (`maingUI.MainGuiGeneralAppStarter`) and its resources |
| | `GuiTextEditorCommons` | Editor components (RSyntaxTextArea, themes, rectangular selection) |
| | `ScreepT_Crypter` | Crypter |
| | `ScreepT_Launcher` | Launcher that picks up updates and starts the app |
| | `NetworkCommons` | Network helpers |
| `ScreepT-public` | `CommonParentGlobal`, `ScreepT_Parent` | Versions and build reactor |
| | `GuiCommons`, `VariousCommonsLight`, `Initializer_Configs`, `OsCommons`, `GeneralResources`, `HardwareCommons` | Shared libraries (GUI, config `.ini`, OS, resources) |
| | `UpdaterModule`, `PluginsParent` + plugins | Updater and plugin system |

### Where things are

- **Configuration**: `INItializer` reads the `.ini` (`getGuiOpt()`, `getFileOpt()`, `getTextEditorOpt()`). Personal settings are in `ScreepT/Resources_ScreepT/CONFIG.ini` and are not committed; defaults for new installs are in `ScreepT/ROOT_VANILLA_FILES_EXPORT`.
- **Editor themes**: XML files in `ScreepT/Resources_ScreepT/themes/default` are shipped with every build and update; users' own go in `themes/custom`.
- **Look and feel**: LAF list in `GUIStyelSettingsExtended`; the dark theme is `gui.commons.guistyle.laf.ScreepTDarkLaf` (FlatLaf, palette in `ScreepTDarkLaf.properties`).
- **File dialogs**: always `NativeFileChooser` (via `GenericFileChooserDialog`), never `JFileChooser`.
- **Release notes**: write `ScreepT/CHANGELOG.md` by hand for every version (`## <ver> - <date>`, `> highlight`, `### New|Improved|Fixed`, `- item`).
- **Documentation is data**: the home assistant (`screept.gui.panels.home`) indexes `ScreepT/ROOT_DIR_APP_FILES/Readme/ReadMe.html` (one section per heading, one line per list item) and `CHANGELOG.md`. Every user-facing change must update both, with a short "how to use it" line in the guide: that is all the assistant knows.

### Conventions

- **Java 8** source level; no APIs above Java 8.
- Files use **CRLF** line endings and **tabs**; keep the copyright header; logging is **log4j 1.2**. Beware that `sed -i` converts to LF (use `perl` or `unix2dos`).
- Clean, tidy code with descriptive names. Match the style of the surrounding code, including comment density.
- Don't commit generated or personal files: `buildNumber.properties`, `changelog.log`, `manifest.properties`, `test.ini`, license lists, `.classpath`, `CONFIG.ini` with your personal settings.
- Never commit credentials. Release/upload tooling (`FTP.properties`) is maintainer-only.

### Testing a change

There is no complete automated test suite yet. Build, start the app from `ScreepT_Build`, and try the feature by hand, including with the Dark theme and a light LAF. If you add logic that is easy to test in isolation, a unit test is welcome.

## Contributing and pull requests

> **Pull requests are treated as suggestions.**

ScreepT is a hobby: no deadlines, no commercial purpose, no support obligations. In practice:

- Feel free to open an issue or a pull request with an idea, a fix or a different approach. I read them with interest.
- **There is no guarantee that a PR will be merged, reviewed within a given time, or merged as it is.** I may take the idea and reimplement it my own way, adapt it, or simply leave it open. Please don't take it personally.
- Please don't invest a lot of work before talking to me: open an issue first for anything big.
- Keep PRs small and focused (one topic each), against `master`, and follow the conventions above. Describe *what* changes and *why*, and how you tried it.
- Don't mix in unrelated reformatting or generated files.
- By submitting a PR you agree that your contribution may be used, modified or rewritten within the project, under the project license.

Bug reports are welcome too: please include the ScreepT version (*Help > About*), Windows and Java versions, steps to reproduce and the relevant part of the log (`LOG` folder).

## License

ScreepT is distributed under the End User License Agreement in [`ScreepT/EULA_ScreepT_License.txt`](ScreepT/EULA_ScreepT_License.txt). Third-party libraries keep their own licenses (see `ScreepT/ROOT_DIR_APP_FILES/Libraries_licenses` in the application).

## Author

Alessio Moraschini, AM-Design-Development: [am-design-development.com](https://www.am-design-development.com)
