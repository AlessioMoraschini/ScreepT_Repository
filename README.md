<div align="center">

# ScreepT

**A smart crypter... and much more.** A free, portable desktop toolbox for people who live in text files: editor, crypter, huge-log viewer, quick notes, calculator, plugins, and an offline assistant that knows its own manual.

[![Download](https://img.shields.io/badge/Download-ScreepT%200.9.67-ff7a1a?style=for-the-badge)](https://www.am-design-development.com/ScreepT/Release/ScreepT_Download.html)

![Version](https://img.shields.io/badge/version-0.9.67-blue) ![Java](https://img.shields.io/badge/Java-8%20to%2021-orange) ![Platform](https://img.shields.io/badge/platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey) ![Offline](https://img.shields.io/badge/works-100%25%20offline-brightgreen)

<img src="https://www.am-design-development.com/Static/IMAGES/articoli/0.9.67/screept-home-assistant.gif" alt="ScreepT home assistant answering questions about the application" width="760">

*Ask the home terminal how to do something: it answers from the real guide, offline, in English or Italian.*

</div>

## Why ScreepT

ScreepT started as a crypter and grew into the small toolbox I wanted open all day: one portable app, no installer, no admin rights, no cloud, no telemetry. It is not trying to replace your IDE or Notepad++; it is the tool you reach for when you need to **edit, hide, search, calculate or jot down something right now**.

- **Portable and private**: unzip and run. Nothing is installed system-wide, nothing leaves your PC, no account, no keys.
- **Actually useful for technical work**: encrypt text and files, open a 3 GB log without freezing, compare code, hash files, pick colors, take quick notes.
- **Runs on Java 8 to 21**. If no Java is found, the starters offer to download Eclipse Temurin 21 (checksum verified) next to the app.
- **Self-updating**: *Help > Check for software updates* installs new versions (and can reinstall a clean copy if something breaks).
- **Has a personality**: a dark theme made with care, a talking About window and ten hidden easter eggs.

## See it in action

### LogSearch: huge files, small memory

Opens multi-gigabyte logs read-only, a portion at a time (`Ctrl+Alt+L`). Text or regex search, matching lines tinted, listed and drawn on a map beside the scrollbar, `F3` to jump between matches. The editor offers it by itself when you open a file over the size limit.

<img src="https://www.am-design-development.com/Static/IMAGES/articoli/0.9.67/screept-logsearch.gif" alt="LogSearch running a regex search on a 400,000-line log" width="760">

### ScreepT Dark: app and editor

A dark look and feel with a dark title bar, interface size presets (Mini to XXL), and a matching editor theme for every supported language.

<img src="https://www.am-design-development.com/Static/IMAGES/articoli/0.9.67/screept-editor-theme-java.png" alt="ScreepT Dark editor theme with Java code" width="760">

### More

| Quick notes (`Ctrl+Alt+N`) | Built-in guide viewer |
|---|---|
| <img src="https://www.am-design-development.com/Static/IMAGES/articoli/0.9.67/screept-quick-notes-dark.png" alt="Quick notes drawer" width="380"> | <img src="https://www.am-design-development.com/Static/IMAGES/articoli/0.9.67/screept-guide-viewer.png" alt="Guide viewer with index and search" width="380"> |
| An auto-saving to-do drawer, stored as plain Markdown. | Searchable guide, themed like the app. |

## What's inside

- **Text editor**: tabs, syntax highlighting for many languages, rectangular selection (Alt+drag), search/replace, text transformations, spell checker, auto-completion, minimap, tail -f mode and automatic temp saves.
- **Crypter**: encrypt/decrypt text and files, optionally combined with a *key image* (steganographic second factor).
- **LogSearch**: read-only viewer and searcher for huge files.
- **Quick notes**: auto-saving to-do drawer in Markdown.
- **Utilities**: file hasher, color picker, calculator (keyboard friendly), clipboard history, code comparator, Markdown viewer, media converters, network info.
- **Home assistant**: offline terminal that answers questions about ScreepT from its own guide and changelog.
- **Plugins** and an **in-app updater**.
- Easter eggs, because why not.

## Get it

1. **[Download ScreepT](https://www.am-design-development.com/ScreepT/Release/ScreepT_Download.html)** and unzip it anywhere.
2. Run `ScreepT.exe` (or `ScreepT_64.exe`) on Windows, `ScreepT.sh` on Linux and macOS. Accept the license on first start.
3. Not sure where to start? Type a question on the home page, or open *Help > Tutorial*.

**If `ScreepT.exe` is blocked** (company PCs, antivirus false positives), you don't need it: run `ScreepT.bat` on Windows or `ScreepT.sh` on Linux and macOS (`chmod +x ScreepT.sh` first), or from the `Application` folder `java -jar ScreepT_Launcher.jar`. To get a desktop shortcut with the ScreepT icon (`Readme/Images/ScreepT.ico`) that you can pin to the taskbar, paste this in PowerShell, setting `$app` to your ScreepT folder:

```powershell
$app = "C:\ScreepT"
$shell = New-Object -ComObject WScript.Shell
$link = $shell.CreateShortcut("$([Environment]::GetFolderPath('Desktop'))\ScreepT.lnk")
$link.TargetPath = "$env:WINDIR\System32\cmd.exe"
$link.Arguments = "/c `"`"$app\ScreepT.bat`"`""
$link.WorkingDirectory = $app
$link.IconLocation = "$app\Readme\Images\ScreepT.ico"
$link.WindowStyle = 7
$link.Save()
```

The full guide (section 1.4, also in *Help > Tutorial*) explains how to pin it to the taskbar.

Release notes: [`ScreepT/CHANGELOG.md`](ScreepT/CHANGELOG.md), also shown in the app under *Help > What's new*. News and articles: [am-design-development.com](https://www.am-design-development.com).

## Project status

This is a **personal side project**, built purely out of love for software made for technical people. It has no commercial goal and no roadmap promised to anyone: things get done when I have time and desire. It runs on Windows, Linux and macOS (anywhere Java does); a few features are Windows-only, such as native file dialogs and the interactive terminal. Bug reports and ideas are welcome (see [Contributing](#contributing-and-pull-requests)).

---

# For developers

## Requirements

| Tool | Version | Notes |
|------|---------|-------|
| JDK | **8** (e.g. Oracle 8u162) | Always Java 8 to build. To run, ScreepT supports Java 8 up to 21 (LTS); the starters download Temurin 21 when no Java is found. |
| Maven | **3.8.x** | Maven 3.9 fails: the unversioned antrun plugin resolves to a release that rejects `<tasks>`. |
| Git | any | |
| OS | Windows, Linux, macOS | Windows gets native file dialogs (JNA/IFileDialog) and the interactive terminal; other systems use the Java ones. |

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

The starters in `ScreepT/ROOT_DIR_APP_FILES` (`ScreepT.exe`, `ScreepT_64.exe`, `ScreepT.bat`, `ScreepT.sh`) offer to download Eclipse Temurin 21 JRE into `jre/` when no Java 8+ is found. The two exe are built from `ScreepT_Launcher/native/ScreepT.cs` with `build_exe.bat` (C# compiler included in Windows): this is NOT part of the Maven build, rebuild and commit them only when the source changes. To test the download without removing Java: set `SCREEPT_FORCE_NO_JAVA=1` and delete `jre/`.

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

Bug reports are welcome too: please include the ScreepT version (*Help > About*), OS and Java versions, steps to reproduce and the relevant part of the log (`LOG` folder).

## License

ScreepT is distributed under the End User License Agreement in [`ScreepT/EULA_ScreepT_License.txt`](ScreepT/EULA_ScreepT_License.txt). Third-party libraries keep their own licenses (see `ScreepT/ROOT_DIR_APP_FILES/Libraries_licenses` in the application).

## Author

Alessio Moraschini, AM-Design-Development: [am-design-development.com](https://www.am-design-development.com)
