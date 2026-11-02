# ScreepT - What's new

<!--
  Release notes shown in the app (Help > What's new) and once after every update.
  Format: "## <version> - <date>", "> highlight", "### New | Improved | Fixed", "- item".
  Newest version first. The raw git history stays in changelog.log.
-->

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
