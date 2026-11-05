# ScreepT - What's new

<!--
  Release notes shown in the app (Help > What's new) and once after every update.
  Format: "## <version> - <date>", "> highlight", "### New | Improved | Fixed", "- item".
  Newest version first. The raw git history stays in changelog.log.
-->

## 0.9.75 - 2026-11-05
> New LogSearch, search and scroll in the interactive terminal

### New
- LogSearch redesigned in the ScreepT Dark style, quicker to use: it searches while you type, with a log-oriented syntax (all the words in any order, "exact phrase", -excluded word) besides regular expression, match case and whole word. Recognises the level of every line (ERROR, WARN, INFO, DEBUG, TRACE; a stack trace follows its entry): coloured stripe in the gutter, errors and warnings on the map beside the scroll bar, level chips with the line counts that filter the matches, F8 / Shift+F8 jump between errors. The matches list shows the context lines you choose (0 to 8) and can be exported to a file. Bookmarks (Ctrl+B, gutter click, F2 / Shift+F2), go to line (Ctrl+G) and zoom (Ctrl +/-/0).
- Interactive terminal: search bar docked above the screen (magnifier button, Ctrl+Shift+H, Cmd+Shift+H on macOS). Highlights every match in the whole history, with match case, regular expression and whole word options, match counter, previous/next buttons (Enter / Shift+Enter) and Esc to close. It also works after the process has ended.
- Interactive terminal: Shift+PageUp / PageDown / Home / End scroll the history from the keyboard. History grown from 5,000 to 10,000 lines.

### Improved
- ScreepT Dark is the default look and feel. The first time you update to this version your look and feel is switched to ScreepT Dark; from the following updates on, whatever you choose in Graphics preferences (Nimbus, light...) is kept and never changed again.
- LogSearch: with the ScreepT Dark theme the window uses its own soft dark palette (light gray text in the search field, a medium gray list of matches that stands out from the file view, the same scroll bars as the rest of the application) and follows the theme even when it is opened before the theme is applied; the other themes are unchanged. Header, toolbar and status bar stand out from the file view, and the map beside the scroll bar shows how dense the matches are.
- Crypter: a warning shown when the module is opened says that it is in beta (experimental, not extensively tested), with a "Don't show this again" option. The user guide says the same, with a notice at the top, and the statements about the security of the Crypter are now plain and objective.
- Every window now has an icon: the ones that had none get an icon that fits (LogSearch, restart, what's new, search, scale, font chooser) or the one of ScreepT, in the application and in the launcher.
- Interactive terminal: with the focus in it, the terminal owns the keyboard: the shortcuts of the menus and of the other panels (including the global ones, Esc to leave full screen, easter eggs) no longer fire while you type in it. Its own shortcuts are not used by any other function.
- Interactive terminal: while you read the history the view stays on the same lines when new output arrives (it used to slide), and typing brings you back to the bottom. Resizing the window or opening the search bar gives back the lines of the history that fit again.
- Full screen programs in the terminal (editors, pagers, TUI tools) receive the mouse wheel as they ask for it (mouse reports) or as arrow keys, instead of ignoring it.
- User guide: a "Why ScreepT?" section at the top sums up what ScreepT does best (huge files, integrated terminal, editor, portability, everyday tools, keyboard and dark mode) for who does not know it yet.
- User guide: new look (dark and light theme, reading progress, back to top), collapsible chapters, a side index that appears while you scroll and fades after 4 seconds of rest, and a search that filters the sections (match case, whole word, regular expression, all words) with highlighted matches and previous/next that wraps around. The guide no longer depends on the application folder, so it also works from the website.


## 0.9.74 - 2026-11-04
> Cleaner Plugin Manager

### Improved
- Plugin Manager: new look in the ScreepT Dark theme. Header with the counters (plugins, installed, to update), filter with search icon, a colored tile for every plugin, status with icon (installed, update available with the versions, not installed), compact Install / Update / Uninstall buttons with icons and tooltips, warnings with an icon. The buttons react only when clicked, Esc closes the window. Same functions as before.

### Fixed
- Code Compare plugin 1.1.1: it opened with an error on Java 9 and newer (JAXB, used by JMeld to read its settings, is no longer part of Java). The libraries now travel with the plugin; it keeps working on Java 8.

## 0.9.73 - 2026-11-04
> Restart with the launcher, and a new Markdown viewer

### New
- Restart and Restart as administrator (Help menu, Alt+F5 / Alt+F6): when "Hide launcher at start" is on, a dialog asks if the launcher must be shown on this restart. "Keep as default" turns "Hide launcher at start" off in the launcher configuration (the same property as Generic Options); without it the launcher is shown just this time. When the launcher is not hidden, the restart goes through it without questions. Restarts for updates or plugins waiting to be installed work as before.
- Markdown viewer plugin 1.1.0: markdown editor with live preview (tables, task lists, strikethrough, autolinks, table of contents), outline of the headings, formatting toolbar with icons and tooltips, HTML source tab, text zoom (Ctrl + mouse wheel), export to a standalone HTML page, copy HTML, open in the browser. Esc closes the window and asks what to do with unsaved changes.
- Launcher: new look with the ScreepT Dark theme and an animated pixel art background in the style of the home page (code rain that draws the ScreepT icon). The window is bigger and the Start button is large and always visible.

### Fixed
- After installing or updating plugins, ScreepT restarted the launcher again every time it was closed. The files of a plugin that has just been installed are no longer left in the "delete at next start" list, old entries of installed plugins are ignored, and the launcher waits a few seconds for the previous run to release the files it has to delete.
- Launcher, Plugin Manager, progress bars and dialogs now always open on the monitor where the main ScreepT window opens (they could open on the other one).

## 0.9.72 - 2026-11-04
> Code Compare text zoom, PDF reader fixed, shortcuts also for Mac and Linux

### New
- Code Compare plugin 1.1.0: text size zoom with Ctrl + mouse wheel (Cmd + wheel on Mac), Ctrl/Cmd and + / - / 0, or the new "Text size" control in the top toolbar (minus, slider, plus, percentage; click the percentage to reset). The zoom is remembered.
- Code Compare plugin: new "Blank" toolbar button, compares two empty documents (type or paste text on both sides). JMeld "New" only compares existing files. The temporary files are deleted when the last Code Compare window is closed.
- Keyboard shortcuts now work with Cmd on macOS wherever they used Ctrl (menus, text editor, tree, shell, find windows); Ctrl keeps working. Nothing changes on Windows and Linux.

### Improved
- Code Compare plugin: updated to JMeld 3.4 (still runs on Java 8 up to 21).
- Code Compare plugin: readable on dark themes (diff colors and the side map follow the look of the window), and the zoom keeps the line you are reading.
- Code Compare plugin: closing with unsaved changes now offers "Save selected", "Don't save" (close without saving anything) and "Cancel"; Esc closes the window (Esc inside a search box still only closes the search).
- PDF and image readers: every button now has a tooltip explaining what it does.

### Fixed
- Plugins no longer change the look of the application: Code Compare used to install its own look and feel (JGoodies) in the whole program, causing odd graphics in other windows. It now inherits the look of the main window. Checked all plugins: only Code Compare did it.
- PDF reader: graphic glitches, freezes and unreadable pages while scrolling. The viewer was placed inside a second scroll area; it now uses the whole window and its own scrolling.

## 0.9.71 - 2026-11-02
> Start at login without the exe, and .log files ask where to open

### Improved
- Log files (.log) are now a recognised type of the text editor, and opening one (also with "Open With ScreepT") asks whether to open it in the ScreepT Text Editor or with LogSearch. Big files still get the existing big file question.

### Fixed
- "Start at login" (autostart): it no longer writes a symbolic link to ScreepT.exe in the Startup folder of all users, which needed administrator rights and failed when the exe was blocked. It now creates a real shortcut in the Startup folder of your user that starts ScreepT.bat through cmd.exe (the exe only when there is no bat), with the ScreepT icon and a minimized window. The old entry for all users is still recognised and removed when possible.

## 0.9.70 - 2026-11-02
> Right click menu and "Restart as administrator" work without ScreepT.exe

### Fixed
- "Restart as administrator" no longer relaunches ScreepT.exe (blocked on some company PCs): it asks Windows to start ScreepT.bat as administrator through cmd.exe, and uses the exe only when there is no ScreepT.bat. It checks that a starter exists before closing ScreepT.
- "Add to Windows context menu after right click on file" no longer relaunches ScreepT.exe as administrator, so it works when the exe is blocked (company PCs) and needs no administrator rights. It is written directly for your user, with the ScreepT icon (shipped in Resources_ScreepT/Icons, so it is always there), and you choose what it starts: ScreepT.bat (works everywhere) or ScreepT.exe. The result is checked and reported, instead of always saying "Done!".
- "Remove from Windows file right click context menu" removes the entry and says clearly if an old entry created for all users is left, because removing it needs administrator rights.

## 0.9.69 - 2026-11-02
> The Java 21 downloaded by ScreepT is now really used

### Improved
- Guide: new section 1.4 "If the exe is blocked" for company PCs: how to start ScreepT with ScreepT.bat, ScreepT.sh or the jar when ScreepT.exe is blocked, and how to create a shortcut with the ScreepT icon (included in Readme/Images/ScreepT.ico) and pin it to the taskbar.

### Fixed
- Launcher: choosing the Java 21 downloaded with "Get Java 21" (or any Java in a folder not named after its version, such as "jre") and saving the options silently replaced it with the Java 8 found automatically, so ScreepT kept starting with Java 8. The version is now read from the "release" file of the Java, not from the folder name.
- Launcher: the preferred Java is now matched by folder, so differences in upper/lower case or in the form of the path no longer make it be ignored.

## 0.9.68 - 2026-11-02
> A faster start, a restyled Crypter and a "Get Java 21" button

### New
- Launcher: new "Get Java 21" button next to the Preferred JVM list (Customize launch parameters). It downloads the latest Eclipse Temurin 21 JRE (checksum verified) into the "jre" folder of ScreepT and selects it. Nothing is installed system wide and no administrator rights are needed.
- Launcher: the "jre" folder next to ScreepT is now searched for Java, so the Java you download (or put there) is found automatically.

### Improved
- Faster start: with "hide launcher at start" the launcher now reuses the starter file (LauncherBatch.bat / LauncherBash.sh) of the last clean launch instead of building itself again, and the application starts about a second sooner. The launcher panel still opens when that file is missing, outdated, the Java it points to is gone or the application does not come up, and always when "hide launcher at start" is off.
- Faster start: the CPU and RAM indicator in the menu bar loads in background, so the main window opens sooner. The launcher splash no longer waits for a decorative animation.
- Crypter restyled with the ScreepT Dark palette: flat panels with thin separators, a highlighted Creept button, readable tabs, key list and selected files, and a dashed drop zone. Behaviour is unchanged.
- Java detection is more complete: besides Oracle and OpenJDK it now finds Eclipse Adoptium (Temurin), Amazon Corretto, Microsoft, Azul Zulu, BellSoft Liberica, IBM Semeru, JAVA_HOME, the PATH, and on Linux and macOS the usual system folders, SDKMAN, IntelliJ and Homebrew.
- The Java version is now read from the "release" file of each Java instead of the folder name, so folders with unusual names (like "jre") are recognised correctly.

### Fixed
- Crypter: when the key dictionary folder (IMG/KEY_DICTIONARY) was missing, a background thread looped without pause and wrote hundreds of thousands of error lines in the log.
- Launcher: a Java located manually was rejected when its "release" file did not start with the version line (Temurin, for example).
- Launcher: the Java the launcher itself runs on could be listed twice because of upper/lower case in the path.

## 0.9.67 - 2026-11-02
> The Java chosen in the launcher is really used

### New
- Help > Check for software updates: when ScreepT is already up to date it now offers to reinstall the current version from the server. It replaces the application files with a clean copy (useful if something got broken), your settings and personal data are kept.

### Fixed
- Launcher: choosing a newer Java (for example 17, 19 or 21) in the launcher settings no longer ends up starting ScreepT with Java 8. Installations created by older versions kept in their launcher ini the old limit "max Java 12", so any newer Java was refused and replaced by Java 8: that limit is now ignored when lower than the newest supported Java (21).
- Launcher: the "restore defaults" button in the launcher settings looked only for Java 8, now it considers every supported Java.
- Text Editor: on Java 17 and newer it did not open (an internal error while styling its buttons) and the home page kept the busy mouse pointer. The style code now skips the protected fields of the Java classes.
- Updates: updating to the very same version already installed no longer deletes the new main jar (ScreepT would not start afterwards).

## 0.9.66 - 2026-11-02
> Java 21 ready, a new built-in guide viewer, no more JavaFX

### New
- ScreepT now runs on Java 8 and on newer Java up to 21 (LTS): the old Java 8 limit is gone. Plugins loading, the JavaScript code formatter and the file dialogs work on both.
- The guide (Help > Tutorial) has a new viewer written only in Swing, themed like the application (dark or light): table of contents on the side, search with highlighted results (Ctrl+F, Enter / Shift+Enter, F3), zoom (Ctrl + wheel, Ctrl + / -, Ctrl+0), selectable and copyable text, external links open in the browser.
- A small label with the Java version in use is shown in the top bar, next to the ScreepT resources. Hover it for a popup with the Java path, version, vendor, VM and JVM arguments: the text is selectable and a button copies it all.

### Improved
- When no Java is found, the starters (ScreepT.exe, ScreepT_64.exe, ScreepT.bat, ScreepT.sh) now offer to download Eclipse Temurin 21 JRE (about 50 MB, SHA-256 checked) into the "jre" folder. On a 32 bit Windows, where Temurin 21 does not exist, Amazon Corretto 8 is used.
- JavaFX is no longer needed: it is gone from the application.

## 0.9.65 - 2026-11-02
> Starters that fetch Java when it is missing

### New
- Starters (ScreepT.exe, ScreepT_64.exe, ScreepT.bat, ScreepT.sh): when no Java 8 or newer is found (local jre, JAVA_HOME, PATH, registry) they offer to download Amazon Corretto 8 (checksum verified) into the "jre" folder next to the starter, then start ScreepT. Nothing is installed system wide and no administrator rights are needed. Whoever already has Java is not asked anything.

### Improved
- ScreepT.exe and ScreepT_64.exe are rewritten: small plain launchers (no packer), with version info and the ScreepT icon, to avoid antivirus false positives. Source and build script in ScreepT_Launcher/native.

## 0.9.64 - 2026-11-02
> The About window talks back

### New
- About: the terminal line is now an ironic assistant, with movie quotes (Star Wars, The Lord of the Rings, The Godfather, Old School, Scary Movie...) and true information about ScreepT that fits what you say. Angry face when you swear, thinking face in every other case.
- About: it hints at the easter eggs every two or three answers and says "acqua", "fuochino" or "fuoco" depending on how close your sentence is to waking one up.
- About: naming an easter egg unlocks just that one (the padlock opens a little); the egg list shows the others locked, with a teaser of what each one does. The master phrase still opens everything: the padlock opens completely and an egg hatches next to the title, with Feo and Nano inside.

### Improved
- About: the window grows, never beyond the monitor, to fit the answers and the egg.
## 0.9.63 - 2026-11-02
> LogSearch for huge files, a real terminal in the shell and option descriptions

### New
- LogSearch (Various > LogSearch, Ctrl+Alt+L): opens a huge text file read-only, a portion at a time, so memory stays low whatever the size. Search as text or regular expression, case sensitive or not; matching lines are tinted, listed below and marked on a map beside the scrollbar. F3 / Shift+F3 jump between matches.
- Opening in the editor a file over the size limit now proposes LogSearch (or loading it anyway, or cancelling).
- Graphic preferences > Text editor: "Max file size (MB)" sets the size above which a file is not loaded in the editor directly.
- Shell (System Console and editor panel): new Interactive checkbox. On, the panel is a real terminal with a real shell (Git Bash on Windows when installed, else PowerShell; your own shell on Linux and macOS): colors, history, line editing and full-screen programs work, Claude Code included. Off, it works as before in text mode. The choice is saved in the configuration file when ScreepT closes.
- Graphic preferences and Generic options: rest the mouse on any option to see a description card. It closes with Esc, a click outside or moving the mouse away, scrolls when focused and can be resized (default about a quarter of the monitor).
- Home assistant: answers now also list the related keyboard shortcuts, when the guide has some.
- Home assistant: ask for a section by name ("text editor", "calculator", "home page"...) and it tells how to open it, with the menu path and the shortcut read from the real menus. It understands Italian and small typos, and says so (with a smile) when it does not understand.

### Improved
- Margin, font size, inline tabs and adapt text width moved from the text editor toolbar to the graphic preferences (Text editor section). Shortcuts are unchanged.
- Graphic preferences: margin and interline now share one compact row, each with a slider and a spinner.
- About stays above every window, also of other applications, for as long as it is open.
- Shell terminal: bold, dim, 256 colors and true colors, block characters drawn exactly (logos and bars), symbol font for missing glyphs.

## 0.9.62 - 2026-10-28
> Six new easter eggs

### New
- Six new easter eggs: Bug Squasher, Cipher Breaker, Code Typist, Shortcut Trivia (questions from the guide), a fortune cookie and a glitch effect. Hidden behind a letter typed three times on the home page (B, C, W, T, F, G), or summoned by name from the home terminal.
- About: the terminal line accepts text, and some words get an answer. Tell ScreepT how you feel and find out. The egg picker keeps the About window above every other window, also of other applications.
- Home terminal: new commands bugs, cipher, typing, trivia, fortune and glitch.

## 0.9.61 - 2026-10-28
> A home page you can talk to, and a lot of polish

### New
- Home page assistant: type on the home page and a terminal wakes up. Ask about any ScreepT function in English or Italian, fully offline (it reads the guide and this changelog). Commands: help, news, shortcuts, topics, eggs. The home image dims while you type and comes back when idle. It keeps working while you use other sections, and history and answers are there when you come back.
- ScreepT Dark editor theme: soft, deep and matte, matching the dark interface, for every language. Default for new installations.
- Quick notes button in the text editor toolbar (right of the spell checker), same as Ctrl+Alt+N.
- Help > Open current installation folder.
- Puppynoid shows a how-to-play guide with the keys, in the start window and on top of the game.

### Improved
- Calculator: works from the keyboard on any layout (numpad, Enter, Backspace, + - * / ^ ( )) and every mode is readable with the dark theme.
- Quick notes and release notes follow the colors of the selected theme, also with Nimbus.
- Network info opens without freezing the loader.
- Dialogs, easter eggs and the first-start name request open on the monitor you used last.

### Fixed
- Progress windows (and other frames) switched the whole application to Nimbus.
- Shortcuts and easter eggs stopped working on the home page after switching section with a shortcut.
- Updater: the update question no longer fails with an error when the updater runs without any open window.

## 0.9.60 - 2026-10-27
> Quick notes at your fingertips and a brand new "What's new"

### New
- Quick notes: a to-do drawer on the right side of the window (Various > Quick notes, or Ctrl+Alt+N). Round checkboxes, inline editing, instant search with highlights, completed notes grouped at the bottom.
- Quick notes are saved automatically at every change, in a readable Markdown file (Resources_ScreepT/QuickNotes/quick_notes.md) with a backup copy.
- Release notes viewer: every version as a card with New / Improved / Fixed sections, searchable, with the installed version highlighted.
- "What's new" window, shown once after each update.

### Improved
- Launcher and main window reopen on the monitor you used last, falling back to the primary one when it is disconnected.
- The text editor follows the window on monitors of any size and always fits the frame, without outer scrollbars.

### Fixed
- Calculator: content no longer cut and the Mode / Help menu is visible again with the dark theme and any interface size.

## 0.9.59 - 2026-10-27
> Animated home, redesigned options and About, rectangular selection

### New
- Animated home page: a code rain draws the ScreepT logo glyph by glyph. Double click to replay it.
- Rectangular selection rewritten, Notepad++ style (Alt+drag): type on a column of carets, Backspace and Delete per row, cut / copy / paste of blocks.
- CPU and RAM indicator in the menu bar.
- A new easter egg in the home page. Hint: cats.

### Improved
- Graphic Preferences and General Options redesigned, with collapsible and searchable sections (Ctrl+F). The editor font is set there and applied live.
- About window redesigned.
- Help guide updated and searchable.
- Secondary windows stay on top only when pinned.
- Dark theme is now the default style.

### Fixed
- Puppynoid no longer switches the whole application to the light Nimbus theme.
- The color picker no longer blocks Alt+Tab.

## 0.9.58 - 2026-10-25
> Native file dialogs, ScreepT Dark theme, interface size

### New
- ScreepT Dark theme, with a dark title bar.
- Interface size presets (from Mini to XXL, or Custom) to scale fonts and controls.
- Native Windows file dialogs everywhere.

### Improved
- ESC closes any secondary window, like the title bar X. Games keep ESC for themselves.
- No more "operation cancelled" popups after you cancel an action.

### Fixed
- Configuration not loaded when the saved window position was missing.
- Error when the main window was moved during startup.

## 0.9.57.2 - 2026-03-05
> Multi-monitor dialog fixes

### Fixed
- Dialog fixes on multi-monitor setups.

## 0.9.57 - 2026-02-28
> Window position memory

### Improved
- The main window position is remembered across restarts, also on multi-monitor setups.

### Fixed
- Multi-monitor fixes.

## 0.9.56 - 2026-02-28
> Multi-monitor fixes

### Fixed
- Close dialogs behave correctly on multi-monitor setups.

## 0.9.55 - 2025-03-12
> Updated libraries and network information fixes

### Improved
- Updated libraries, including RSyntaxTextArea 3.5.4 and HttpClient 4.5.14.

### Fixed
- Network information window fixes.
- Random freeze of the window while checking for updates.
