# ScreepT - What's new

<!--
  Release notes shown in the app (Help > What's new) and once after every update.
  Format: "## <version> - <date>", "> highlight", "### New | Improved | Fixed", "- item".
  Newest version first. The raw git history stays in changelog.log.
-->

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
