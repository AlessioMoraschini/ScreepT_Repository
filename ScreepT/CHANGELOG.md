# ScreepT - What's new

<!--
  Release notes shown in the app (Help > What's new) and once after every update.
  Format: "## <version> - <date>", "> highlight", "### New | Improved | Fixed", "- item".
  Newest version first. The raw git history stays in changelog.log.
-->

## 0.9.88 - 2026-11-20
> Crypter with authenticated AES-256/128-GCM, combinable layers and key image as second key; faster spell checker with language menu

### New
- Crypter: Secure modes (AES-256-GCM, AES-128-GCM, Maximum, Secure + key image, Custom). Wrong passwords, damaged or modified files are always detected, the whole password counts and decryption recognises the algorithms by itself.
- Crypter: combinable layers (AES-128, AES-256, StegaCREEPT key image, Shuffle) and key derivation PBKDF2-SHA256/512 or scrypt, from the Options... button. A line under the controls shows how the choice protects the data and the password strength.
- Crypter: folders become one encrypted archive by default (names hidden too); file by file and zip of encrypted files are available. Decrypting also opens zips.
- Spell checker: language menu when it is turned on (any word list in the SpellChecker folder).

### Improved
- Spell checker: dictionaries load in the background and checking follows the visible text while you scroll, also in big files.
- Crypter: the instructions are a rendered page in the selected language (tab Guide); the choice made in the last encryption is remembered; Options... starts from the selected mode.
- Launcher: in the start options the Java selection (label, list and Get Java button) is one line above the Java details, which now use the whole width of the window.

### Fixed
- Crypter: the options bar was squeezed and unreadable; it now adapts to the window.
- Key images added to the vault from the generator now appear immediately in the Crypter tree.
- Legacy AES decryption with shuffle off returned 16 extra bytes at the start of every block of the file.

## 0.9.87 - 2026-11-20
> Plugins can open as tabs of the file editor (PDF viewer, Markdown viewer), tabs come back as they were at the next start, pin a tab with Alt+Shift+J, quick notes you can move and resize, faster searches with filters and regular expressions

### New
- Plugins in panel mode: a plugin can also work as a tab of the file editor. The PDF viewer and the Markdown viewer plugin (1.3.0) are listed in Graphic preferences > File editor > Viewers, where you can choose them as the viewer of an extension (for example .md or .pdf), and in "Open with". Plugins installed while ScreepT runs show up too.
- Tabs that host a viewer (images, LogSearch, HTML, plugins) are reopened as viewer tabs at the next start, not in a separate window.
- Pin or unpin the current tab with Alt+Shift+J (also in the tab menu).
- Shift+click on a tab closes it, like its close button.
- HTML tab: View source / View page switch.
- Quick notes: drag the title to move the panel, drag its left or right edge to change the width. Width and distance from the right edge are remembered.

### Improved
- Search in files with file filters is up to 7 times faster on big folders (38,000 files: 13 s to 1.8 s), and regular expressions that contain a fixed text are up to 14 times faster (37 s to 2.7 s on the same folder).
- Search in files: no "N results found" popup at the end, the results window comes to the front by itself (a message only appears when the results were cut). Cancelling a search shows no popup either.
- File editor toolbar: while a tab with a viewer is selected, the functions that work on text (search, replace, Base64, undo, font...) are disabled, and they come back on text tabs.
- Changing the text size changes every open tab at once, so the text no longer resizes when you switch tab.
- The code map and the clipboard panel open at a fixed width from the right edge, they keep it when the window or the file tree change size, and they no longer jump when you switch tab.
- "ScreepT has been updated" is shown once per version (it came back at every start when the preferences could not be saved), never at the very first start and never over another dialog. The welcome question keeps the focus while the application finishes loading.

## 0.9.86 - 2026-11-20
> Search in files is much faster, with a speed selector, a results filter and lazy highlights; LogSearch can search only on demand; the interactive shell no longer glitches

### New
- Search in files: Speed combo (Gentle, Balanced, Fast, Maximum). The presets scale with the cores of the PC and the choice is remembered.
- Search in files: a filter bar on the results (Ignore case, Whole word, Wildcard) with the count of lines and files.
- Search in files: the files of the results open by themselves while you scroll and close when they go out of view; the ones opened or closed by hand stay as they are. Matches are highlighted.
- Opacity slider (35% to 100%) in Search in files, Find and Replace.
- LogSearch: "Search while typing" check box. When it is off, the search starts with the Search button or Enter; on a file over 5 GB a message offers to turn it off.
- LogSearch: the date filter accepts a time too (dd/MM/yyyy hh:mm:ss.fff).
- File editor toolbar: the eye follows the file as it grows, on or off for the current tab (it was in the Tools menu).

### Improved
- Search in files is several times faster: it reads big blocks with several threads, looks for the rarest bytes of the text first, decodes only the lines that match and skips binary files quickly. On a 2 GB set of files a search takes about 0.3 s (measured on the author's PC, files in the system cache).
- LogSearch: the map on the right is computed in the background; the status bar shrinks with the window and the file details are more compact.
- Interactive shell: the Interactive switch and the magnifier stay side by side on the left of the header.

### Fixed
- Interactive shell: switching Interactive on and off several times corrupted the panel (the terminal was left empty). It now works every time.
- Search in files: the X of the progress bar could freeze the search; the progress is updated at most every 200 ms and a single message follows a stop.
- Search in files: the dialog could be cut off at the edges of its window.

## 0.9.85 - 2026-11-18
> Quick notes with tags, workspaces and export; the file tree filter and the notes search are pop-up bars with wildcards; the recent workspaces are one click away in the file tree

### New
- Quick notes: the + next to the tag row creates tags before any note uses them; they stay in the workspace. The tags of a note are chips on its row: hover one to see Remove, click it to remove the tag. The # opens a combo right under the tags with the existing ones: picking one adds it. A note with many tags shows them one per line when you press +N.
- Quick notes: tags on every note. The # button that appears on hover edits them (comma separated, shown under the note). The search finds the tags too.
- Quick notes: workspaces, separate lists of notes, each one in its own file. Choose one from the combo at the top of the drawer; create, rename or delete them from the menu. A deleted workspace is not erased: its file is moved to the QuickNotes/deleted folder.
- Quick notes: export the notes of a workspace to a Markdown file, and import a file as a new workspace. The import that replaces the current notes is still there.
- File tree filter rules: a "Filters on" switch turns all the rules off and on without losing them, and "Select all" enables or disables every rule at once.
- Interactive terminal: in interactive mode the toolbar and the Live cmd row go away, and the terminal takes the space. The Interactive switch, the search and the pin stay in the header, in their usual place.
- File tree: the filter is a pop-up bar, opened with the magnifier in the tree toolbar. Closing it (or Esc with the field empty) clears the filter too.
- File tree filter: wildcards. * is any text and ? is one character; a text with them must match the whole file name (*.pdf, test?.txt).
- Quick notes: the search is a pop-up bar, opened with Ctrl+F or the magnifier in the header; Esc clears it and then closes it. Wildcards work in the text and in the tags.
- File tree toolbar: the down arrow lists the last workspaces. Picking one loads its folder, unless it is the one already loaded; a folder that no longer exists is removed from the list.
- Quick notes: the tag row under the header never wraps. Click tags to filter by several at once: a note with any of the chosen tags is shown. The row keeps every tag that fits, side by side and adapting to the width, and the arrow at the end opens the full list with the counts and a Clear filter. With many tags the row shows as many as fit, and the tags of a note are cut with an ellipsis (the full list is in the tooltip).
- Quick notes: To do and Done check boxes next to the search show or hide open and completed notes, independently of each other and of the search.
- Quick notes: deleting a note shows Undo in the footer for six seconds, and puts the note back where it was, even after a workspace switch.
- Quick notes: Clear completed notes... in the menu removes the completed notes of the workspace, after a confirmation.
- LogSearch: the status bar shows the speed and time of the last search with a gauge icon (green, blue, orange or red by speed), and the average speed and time of the searches of the same type. Click it for the last 100 searches (duration, bytes scanned, speed, lines, matches), grouped by type of search (text, several terms, regex, with match case and whole word), average, median, best and worst speed per type, a chart and the computer they ran on (CPU, memory, Java, disk); the tables fit their content and scroll sideways if the window is narrow; Reset forgets them and Export report saves a self-contained web page to share. It holds only timings and system data, never the searched text or file names.
- Clipboard panel (Alt+A and the Clipboard tool): same look as the rest of the app, in light and dark themes. Rename and delete clipboards (the main one stays), copy the selection or all the text, paste the system clipboard, empty it, and see characters, words and lines. A badge says Saved or Unsaved.

### Improved
- LogSearch is about twice as fast on big files: the file is read by several threads at once and the text search skips faster along each line. On a 2 GB log (22 million lines) a search takes about 0.65 s instead of 1.2 s, and the index is built in about 0.6 s instead of 1.5 s (measured on the author's PC, file in the system cache; a cold disk is limited by the disk itself). Matches, line numbers and the index are identical to before.
- Clipboard panel: changes are saved by themselves a moment after you stop typing, when you switch clipboard, when the panel is hidden and when the text loses the focus; the "saved!" pop-up is gone. The list updates when you open it, and a clipboard changed outside ScreepT is reloaded when there is nothing unsaved.
- Launcher, Customize parameters: a long JVM name in the combo is cut after its first 15 characters with "...", and the end of the name fills the rest of the space (the whole name when it fits). The window fits all its content, the JVM info wraps instead of running off, and the window is no longer resizable (no maximize).
- File tree filter: the funnel button is faded while the rules are switched off.
- Open with: only the editors that can open the selected files are listed. The text editor is offered only for text files (no binary bytes), and an HTML or image viewer only for the files it can show.
- Open in a window: LogSearch is listed only for text files.

### Fixed
- Text editor: Shift + click selects from the caret to the mouse also after a selection made with the keyboard (Shift + arrows, Ctrl + Shift + arrows). It used to select from a point left over from the last mouse click.

## 0.9.84 - 2026-11-18
> A file tree filter that answers while you type, a Key Image Vault with previews and links, a password strength checker, and a safer Configuration Manager

### New
- Key Image Vault (formerly Key Dictionary): the window and the menu use the new name. Select a key to see its thumbnail and details, and use the Delete button to remove the selected key images after confirmation.
- Key Image Generator and Key Image Vault: a link at the top right opens the other window.
- Key Image Generator: a Stop button stops the generation. No image is produced, as if it had never been generated.
- Password generator: seven strength levels, from "Hacker's birthday gift" to "Now even FBI can't hack you :)". Every password is judged, including the ones derived from a word, with its estimated bits and the tips to make it stronger.
- Password generator: a "Password strength" field checks any password while you type, with a meter and what is missing to reach the next level.
- Configuration Manager: the configuration in use is the first entry of the list, even when it is outside the INI_FILES folder.
- Configuration Manager: before loading another configuration, ScreepT asks whether to save the unsaved changes.
- Configuration Manager: tooltips on the actions.
- File tree filter: the funnel button opens the filter rules. Folders and files to skip (by default .git, .svn, .hg, node_modules, .idea, .gradle, __pycache__ and target), each with a name or wildcard (*.java, TEST*.rar...), the kind it applies to (folder, file, symbolic link, any) and an on/off switch. Add, remove or restore the defaults; the rules are saved.

### Improved
- Password generator, Key Image Generator, Key Image Vault and Configuration Manager use the look of the other tool windows: readable in light and dark themes and with bigger fonts, and the windows fit their content.
- Key Image Vault refreshes by itself when the files in its folder change.
- Key Image Generator: the preview keeps the proportions of the image and fits its card.
- Configuration Manager: the configuration text is read-only, since the changes typed there were never saved.
- File tree filter: the names under the folder are read once in the background into a compact index, and then every letter you type searches memory (a few milliseconds, also with hundreds of thousands of files; a longer text refines the previous result). The question about folders over 5000 elements and the refusal over 200000 are gone. The index is refreshed in the background when the tree is reloaded and released after five minutes without filtering.
- File tree filter: only the folders that lead to the matches are opened, and the result is prepared in the background, so the tree no longer freezes on big results. Changing tab or folder while filtering never shows the result of the previous one.
- Memory card of the menu bar: labels in bold and values in plain text, and the entries now add up to the total shown (heap reserved, JVM internals, thread stacks, direct buffers, and the native code and libraries calculated by difference).
- Launcher settings: a Profile combo (Light, Medium, Fast) sets the initial memory and the garbage collector at once; the Garbage collector combo shows what each one means and how much memory it costs. Choosing a value by hand turns the profile into Custom. Saved in the launcher ini
- Launcher: the default thread stack is 6 MB, the default initial memory is 128 MB (lower than before) and the default garbage collector is G1. Installed launchers get the stack and memory values once after the update (lower values are kept); the collector follows the default unless one was chosen in the launcher

### Fixed
- Key Image Generator: the window could freeze at 100% after generating a key (deadlock with the progress dialog). The progress is now shown in the window.
- Key Image Generator: after 11 saved keys the recent list raised an error even though the file was saved.
- Key Image Generator and Key Image Vault: cancelling a file dialog showed an error popup.
- Password generator: random passwords come from a cryptographically strong source.
- Configuration Manager: Export changed the configuration in memory, so a later Save could write default values to disk.
- Configuration Manager: the configuration in use cannot be deleted from the list.
- Launcher: the loading screen was updated from a background thread, which the interface library reports as a warning at every start: now it is updated on the interface thread
- Launcher: an ini without the stack size produced an invalid "-Xssnullm" argument: now it falls back to the default
- File Editor cleanup (broom in the top right corner): an empty untitled tab no longer stops the cleanup, and when the editor is kept because of unsaved work the message says so

## 0.9.83 - 2026-11-17
> A new File editor toolbar: one tidy row of icons that scrolls when the window is small, and new text tools

### New
- File editor toolbar: the two crowded rows of buttons are now a single row of groups (File, Edit, Find, Text, Panels, Current file, then Tools and View), separated by thin lines. When the window is too narrow the toolbar scrolls sideways: use the thin bar under the buttons or the mouse wheel over the toolbar. The language and theme boxes stay on the right.
- File editor toolbar: all the icons were redrawn in the same line style (sharp at every interface size, they follow hover, pressed, selected and disabled states). The buttons no longer take the keyboard focus away from the text.
- Text menu of the toolbar: sort lines A-Z / Z-A, remove duplicate lines, remove empty lines, trim spaces at the end of the lines, UPPERCASE / lowercase / Title Case. They work on the selection or, when nothing is selected, on the whole text, and can be undone.
- Tools menu of the toolbar: calculator, color sampler, image to text (OCR), PDF to text, read aloud, follow file (tail), encrypted workspace and open in ScreepT, now with their name next to the icon.
- View menu of the toolbar: word wrap, show spaces and tabs, show line ends, show the tab grid, tab size (2, 3, 4, 6, 8) and zoom in / out / reset.
- New button "Copy path" in the toolbar: copies the full path of the current file.

### Improved
- File editor toolbar: the panel buttons (file tree, map, clipboard, console, quick notes, spell checker) show clearly when they are on. Remove spaces, remove content, replace escape sequences and clear text moved to the Text menu; their shortcuts do not change.

## 0.9.82 - 2026-11-17
> Tabs you can pin and move, the hosts file with its own colors, and the real memory of ScreepT in the menu bar

### New
- File editor tabs: right click a tab > Pin tab, or [Ctrl+click] on the tab ([Cmd+click] on macOS). A pinned tab goes to the left, after the other pinned ones, and shows a pin instead of the close button; click the pin, [Ctrl+click] again or right click > Unpin tab to release it. Pinned tabs are remembered: at the next start they are reopened and pinned again. Closing a pinned tab with [Ctrl+W] still works.
- File editor tabs: drag a tab with the mouse to move it before or after the others (a line shows where it will be dropped). Pinned tabs move only among the pinned ones, the others among the not pinned.

### Improved
- File editor: files without extension that are well known by name, like the hosts file and Dockerfile, are recognized: they open with their own syntax coloring and without the "type of file not supported" question.
- Memory in the menu bar: the label shows the total memory used by ScreepT (the working set, the RAM really used, as in the Task Manager) instead of the Java heap only; rest the mouse on the ScreepT part (not on the PC part) to open a card with where it goes: heap (used, reserved, maximum), non-heap with metaspace and code cache, thread stacks and direct buffers. The card updates every second, takes the focus when the mouse enters it (scroll with the wheel, resize from the edges) and closes with Esc, a click outside it or when the mouse leaves.

### Fixed
- System information window: on PCs where the Windows performance counters are broken (error 0xC0000BBD) it stayed on "Loading..." forever. Now it opens, the processor data show as not available, and if the window cannot be opened an error message is shown.
- PDF viewer: opening it from the menu without a selected file (for example from a new tab) raised an error. Now the empty viewer opens.
- File editor: closing the first tab deleted the backup copy of the tab that became the first instead of the closed one.

## 0.9.81 - 2026-11-17
> A quicker and cleaner File editor: slimmer tabs, a filter for the file tree and a calendar where the year can be typed

### New
- Easter egg leaderboard: Snake, Puppynoid, Bug Squasher, Cipher Breaker, Code Typist and Shortcut Trivia save the score at the end of every run, and the ten best of each game are kept on this installation (nothing is sent anywhere). The "Leaderboard" entry on the top right of every game window opens it, with a list of the games and a button to clear one of them. Only the games you have already played (or the one you are in) show their name: the others stay "???" until you find them. Records saved by older versions are kept.
- Puppynoid: 25 levels (they were 4) with a measured, growing difficulty: faster ball, narrower paddle, fewer bonuses, more bricks and bricks that need up to 5 hits (blue and purple). From level 2 a level never starts with your last life. The level counter shows "Level 7/25".
- File tree: filter bar above the tree. Type part of a name and only the matching files and the folders that lead to them stay (case insensitive); the x button or Esc clears it and the folders open as they were. On very big folders (over 5000 elements) it asks before going on, because it can take long.
- Calendar of the date fields (LogSearch): click the year to type it, no more months to scroll by hand.

### Improved
- File properties window, redesigned: it opens at once and fills in while it reads. Type, MIME, owner, attributes and volume space; image size without decoding the image; for text files encoding, lines and line endings; for folders number of files and total size; checksums MD5, SHA-1, SHA-256 and CRC-32 in a single pass with progress and a Stop button (files over 256 MB wait for Calculate); paste a hash in Compare to check the file. It follows the look and feel in use.
- File editor tabs: flatter and more compact, with an accent bar on the selected one, a thin divider between the others and colors taken from the look and feel (light or dark). The custom icons are the same.
- File tree: much faster. Folders are read once instead of on every paint, icons are no longer loaded from disk for each row, and switching tab no longer rebuilds the tree when nothing changed on disk. Folders are now listed before files.
- The last "Text-Editor" texts are now "File Editor".
- Guide, index: the "Index" button on the top left shows or hides the index with a sliding animation (the choice is remembered). The index is no longer a floating panel over the text: it opens at the side and the text stays next to it, on a narrow window it covers the text until closed.
- Guide: updated for the File editor (file tree filter, tabs, calendar), and ready for more languages: when a translated ReadMe_<code>.html is present, the guide opens in the language of the application and shows a language selector.
- System information window, redesigned: live CPU, memory and ScreepT usage with history graphs and a bar for every logical core, system and Java, processor, motherboard, battery, network speed, disks with read and write speed, volumes with free space and monitors and graphics cards (name, driver, video memory; on NVIDIA also load, memory, temperature and power with "Live NVIDIA data", off by default because on laptops it can keep the GPU awake). It is written for Windows, Linux and macOS too (lspci and system_profiler for the graphics cards, pseudo volumes hidden): only Windows was tried. The "Copy report" button copies every value as text, "Pause" stops the refresh, "Always on top" can be turned off. It fits its content (scroll bars when the screen is not enough), follows the theme and the language, and the refresh no longer touches the window from a background thread.
- Every window, plugins included, now opens on the monitor of the main window.

### Fixed
- PDF reader window: it opens sized to its content (toolbar and buttons were cut on the right) and grows up to the monitor limit, with scroll bars only when the screen is too small.
- PDF viewer on Java 19 and 21: text and part of the page content were not drawn (a Java 19 change broke the bundled ICEpdf reader). Fixed with a small compatible patch, checked rendering on Java 8, 17, 19 and 21; Java 8 and older versions work as before.
- System information (hardware plugin) works again on Java 17, 19 and 21: an old limit disabled it above Java 9 even if it runs fine (checked on Java 8, 17, 19 and 21). The limit is now the supported range, also for installations updated from old versions.

## 0.9.80 - 2026-11-16
> A faster and smarter LogSearch (monitor, date filter, more log formats), Image Studio full screen, "Open with" in the file tree, update all plugins at once, and the Text editor is now the File editor

### New
- LogSearch date filter: from / to (both optional, both days included), typed as dd/MM/yyyy or picked from a small calendar in your language. It appears when the lines have a date in one of the usual formats (ISO, dd/MM/yyyy, Apache / nginx, syslog, java.util.logging, Unix time, JSON). A date typed or picked turns it on; the check box turns it off without clearing the dates and the cross removes them. Finding the limits reads a few blocks, so it is instant on huge files.
- File tree: right click on a file > Open in a window: opens it in LogSearch, in the Image Studio window (images) or in the File Hash checker, the tools of the Various menu that read a file; only the ones that can read that type of file are listed.
- LogSearch monitor (on by default): the file is watched, and what other programs write to it (new lines, a log that keeps growing) appears by itself in the lines and in the search results, following the end when the view was at the end; a file that is rewritten or replaced (log rotation) is indexed again. With the monitor off a warning tells that the file changed on disk and the Refresh button reads it again. The file is no longer kept open, so other programs can rotate, rename or delete it while it is shown.
- Image Studio full screen (button or F11, in the window and in the tab): the image fills the screen with zoom (wheel, drag to move) and a menu at the top center with zoom, fit, 100% and rotation (view only). The menu hides 5 seconds after the mouse left it and comes back when the mouse passes over its place; the pin keeps it always visible, and the X at the top right (or Esc) is always there to exit.
- File tree: right click on a file > Open with: opens it once in the file editor or in an integrated viewer (image studio, LogSearch, HTML...), the default viewer of the type is marked and the associations do not change.
- Plugins > Update all plugins: looks for newer versions of the installed plugins and prepares all their updates in one go (installed at the next start, with the offer to restart).
- Auto-check plugin updates (General Options, on by default): at startup ScreepT checks the installed plugins like it does for the software. Both update questions now explain where to turn the check off and have a "Don't show this again" box that turns it off.
- Search in files: opacity slider (from 20% to 100%) and the window always stays above every other window.
- Markdown Viewer plugin 1.2.0 (update it from Install plugins): Enter continues lists, task lists and quotes and an empty item ends the list; Tab / Shift+Tab indent list items and number them again; Backspace after a marker goes up one level; Ctrl+B / Ctrl+I / Alt+S / Ctrl+M toggle bold, italic, strikethrough and math on the selection or the word at the caret; Ctrl+Shift+] / [ heading level; Alt+C task boxes; Shift+Alt+F table formatter (alignment kept); Ctrl+Shift+T table of contents between <!-- TOC --> markers, updated on every save (<!-- omit in toc --> leaves a heading out, GitHub anchors with -1, -2 for duplicates); suggestions of headings and files after "](#" and "]("; a url pasted on a selection makes a link; $math$ and $$blocks$$ in the preview; HTML export with images embedded, links fixed and formulas rendered by KaTeX; F1 lists every shortcut.

### Improved
- LogSearch recognizes the levels of more log systems: Serilog [INF] [WRN] [ERR], .NET fail: / warn:, glog, Android logcat, java.util.logging FINE / FINER / FINEST, syslog NOTICE / ALERT / EMERG, JSON "level". Indexing speed is unchanged.
- LogSearch header is more compact: name, path, warnings and controls on one line, file details in the status bar, level and date filters on one line under the search.
- LogSearch in a file editor tab (.log files, or the extensions chosen in Graphic preferences > File editor > Viewers) is bound to its file: no Open file button and dropping another file does not replace it; the window opened from Various > LogSearch keeps both.
- "Text editor" is now "File editor" everywhere: menus, window titles, options, guide and all languages.
- Interface texts use stable ids shared by every language pack (English is a pack like the others): Crypter, Batch, Metadata cleaner, Hardware info, Key generator, Configuration manager, Plugin manager, LogSearch, launcher, memory dialog, network info, file search and the exit dialog are translated; the launcher also speaks the language chosen in ScreepT.
- Every window opens 10% bigger than its content, so longer texts of other languages do not overlap; Network info is bigger too and scrolls when it does not fit.
- Color picker redesigned: rounded look, big sample with its HEX code, one click copy of every format, contrast of white and black text, recent colors of the session; it now always stays on top of every window, pinned or not.

### Fixed
- Image Studio opened from "Edit in Image Studio" now always comes in front: it is above everything for the first 2 seconds, then it is a normal window again. The question "Keep tab / Close tab" is gone: the tab stays open.
- File editor: with the focus on the file tree, Enter now opens the selected file also when it was chosen with the arrow keys, and after the tab opens the focus comes back to the tree on the same node.
- Search in files and Find / Replace did not open in languages other than English.
- Hash checker: long hashes (SHA-512) no longer widen the window and hide the copy buttons; the full value is in the tooltip.

## 0.9.79 - 2026-11-15
> The whole application speaks 7 languages, a new Theme Editor, live crop in Image Studio, a much faster LogSearch and a clearer text editor footer

### New
- Languages: English, Italian, Spanish, French, German, Portuguese and Chinese (Simplified). New world icon in the top bar (between the cleanup and the pin): choose the language and the whole interface changes at once; it is also remembered in General Options. The packs are embedded in the application (about 50 KB); a text that is not translated yet stays in English.
- Theme Editor, completely redesigned: searchable list of 60+ elements in groups (editor, highlights, gutter and folding, options, embedded languages, syntax tokens), color chips (click to change, right click to copy / paste a hex color or remove it), bold / italic, click on the code of the preview to jump to its element, unsaved changes marker, Ctrl+S, default themes can be edited too (Save as keeps your version).
- Image Studio, Crop: with the Crop section open the whole image is shown with the crop on it; drag the corners, the sides or the whole rectangle (thirds and size shown) and the sliders follow in real time.
- Search files by name: the files of the folder are read once in the background and kept for 5 minutes, so typing is instant; names are matched ignoring case, with * and ? wildcards, and the ones that start with the text come first.
- Quick notes backup: the new menu button of the drawer saves or updates a backup of all the notes in a folder you choose, and imports them back from a backup file; before replacing the current notes it warns you and offers to back them up first.
- Text editor footer: the information is grouped in blocks on one row (file, text, caret, selection), names are darker than values, and it shows the size of the file on disk in readable units (KB, MB, GB).
- LogSearch opened from the Various menu now starts empty: the Open file... button at the top chooses the file, and once loaded the header shows its details (path, size, modified and created dates, lines, read-only). There is only one LogSearch window and one file at a time; opening a file from the text editor still loads it directly.

### Improved
- LogSearch is much faster: a 2 GB log (24 million lines) is indexed in about 1.5 seconds and a search takes about 1.5 seconds, plain text and regular expressions alike (they used to take 10 to 35 seconds). The file is read in big pieces by several threads, text searches run directly on the bytes, regular expressions look for their fixed parts first, and memory no longer grows with the number of matches.
- Switching text editor tab is immediate: theme, options, folding and file checks run when you stop switching. A new tab or a file opened in a new tab already has the theme of the editor (no more white flash).
- "Show Tabs" explains what it does and now really draws the tab guide lines.
- The tab size field is wider.

### Fixed
- Configuration manager: the text of the buttons is always fully visible, whatever the font and the interface size.
- Copy and cut in the text editor no longer scroll to the line and no longer leave characters selected after a cut.
- The dark / light switch button of the text editor (Ctrl+B) is removed.

## 0.9.78 - 2026-11-12
> Image Studio goes pro (54 filters, crop, metadata cleaning, PSD and Camera Raw import), launcher settings from the app, 1.5 GB default memory

### New
- Image Studio filters: 54 in all, in six groups (Color, Looks, Tone, Detail, Art, Wild). New: Noir, Vivid, Cyanotype, Dusk, Infrared, Lomo, Bleach, Golden hour, Night, Film, Cross, Cinematic, Matte, Glow, Soft focus, Film grain, Sharpen +, Clarity, HDR, Denoise, Sketch, Cartoon, Oil paint, Solarize, Glitch. Each filter is a card with a preview of the filter applied to your own image.
- Image Studio: a "Strength" slider lowers the effect of the filters (0 to 100%, the rest is the image before them).
- Image Studio, Crop: cut the sides in percent and/or take the biggest centered area with a ratio (1:1, 4:3, 3:2, 16:9, 5:4, 4:5, 9:16, 2:1), with the resulting size shown.
- Image Studio, Adjustments: exposure, shadows, highlights, vibrance and tint; "Auto enhance" button.
- Image Studio, Effects: sharpness (unsharp mask), blur with radius, vignette and film grain with an amount each.
- Image Studio, Rotate & flip: fine "Straighten" of -45 to 45 degrees that enlarges the image just enough to leave no empty corner.
- Image Studio, Border & text (Nerd): frame with color, rounded corners, and a text watermark (position, size, opacity, color).
- Image Studio, Resize: quick sizes (Full HD, 4K, square, story, icons...).
- Image Studio: "Before" button (key B) shows the image without the pending edits. All the new options are saved in the presets and used by Batch.
- The viewer in the editor tab asks, after "Edit in Image Studio", whether to close the tab (default: keep it).
- Image Studio, metadata: remove EXIF, GPS location, XMP, IPTC, comments, thumbnails and data hidden after the image end from JPEG, PNG, WebP and GIF without re-encoding (no quality loss; the color profile and the orientation can be kept, or only the location removed). From the Metadata button of any image (save a clean copy, or clean the file itself after a confirmation) and from the new "Clean metadata..." tool of the Image Studio window: files and folders (with sub-folders and filters) by buttons or drag and drop, clean copies in a folder (default, with the folder structure and a name pattern) or replacing the originals (never the default, with confirmation and optional backup), keep the file date, progress, cancel and a result for each file.
- Image Studio, import: Camera Raw / Lightroom presets (.xmp, .lrtemplate) are translated to Image Studio settings (exposure, contrast, highlights, shadows, whites, blacks, saturation, vibrance, temperature, tint, clarity, sharpening, vignette, grain, black and white, crop, straighten); what has no equivalent (curves, HSL, masks, lens corrections) is listed in a report. Available in the Presets section and in the Batch window; the accepted types are shown as hint.
- Image Studio, Photoshop files: .psd (and .psb) open in the viewer, the editor tab and the Batch (merged picture, small preview when the merged picture is missing, or a clear "not supported" message for 32 bit files, damaged files or files too big for the memory), with their information in Metadata and a button to read the Camera Raw settings stored inside.
- The image viewer is associated also to .jfif, .jpe, .dib, .tif, .tiff, .psd and .psb.
- Batch: click an image of the list to preview the settings on it (the built-in sample is used when the list is empty), the first image added is shown, the list no longer overflows the sample panel, and "Import settings..." applies a Camera Raw / Lightroom preset to the whole batch.
- New pencil icon next to the memory shown in the menu bar: change the maximum memory of ScreepT between the minimum set in the launcher and what the PC can give now; it is saved in the launcher settings and asks whether to restart now.

### Improved
- The default maximum memory of ScreepT is now 1.5 GB (it was 400 MB). At the first start after this update the launcher sets it once to 1.5 GB, or to the most the PC can give (80% of its memory) if that is less; a marker in the launcher settings makes sure later updates never reset it, so a value you choose afterwards (also with the new pencil in the menu bar) is kept.
- Text editor file tree: it now shows its levels (a thin guide line runs along the children of each folder, the one of the folder that holds the selected row is lit), full row highlight on selection and hover, a wider indentation, and soft colors taken from the theme (its accent, tuned for the tree background) so every theme, light or dark, reads well; the rows of the level that holds the selection are barely tinted.
- Feo and Nano on the home page never go over the text of the prompt or of the console: they stay beside it, and now and then one walks up, touches it with a paw and goes away. They also have new moves that happen at random: rolling, backflips, naps with a stretch when they wake up, zoomies, spinning, sitting.
- Image Studio: 12 wild filters (Rainbow, Acid, Neon edges, Thermal, Pop art, Kaleido, Swirl, Ripple, Bulge, Melt, Mirror, Dream), 54 filters in all, in a new "Wild" group; a Metadata section in the side panel with the same tools as the header (see, save a clean copy, remove, cleaning tool); "Edit in Image Studio" brings the window to the front.
- Windows now fit their content and have vertical and horizontal scroll bars when the content does not fit the screen (Clean metadata, Batch, memory); in the Batch window the border between the file list and the preview can be dragged and the two never overlap.
- Launcher: "Hide launcher at start" is now also in the launcher window. If it was off when the launcher opened and you turn it on and press Start, it asks for confirmation and explains how to bring the launcher back (Preferences > Launcher Settings, Preferences > General Options, or Help > Restart).
- New menu entry Preferences > Launcher Settings: opens the launcher in "settings only" mode, with the same options but no Start button (it only changes the settings, applied from the next start), so the launch options can be changed without leaving ScreepT.
- Loading window: only Nano plays, and keeps moving while the application loads (the animation no longer runs on the thread that is busy loading).
- Images opened in a text editor tab are now a read-only viewer (zoom, pan, details, histogram, view options, copy) with an "Edit in Image Studio" button. The full tool (edits, filters, resize, export, batch) is the Image Studio window.
- Image Studio side panel redesigned: one section open at a time so everything fits without scroll bars, a badge on each header with what is set inside (format, resized size, adjustments, filters, rotation), compact sliders with the value on the same row, a full width Export button and Apply / Undo / Revert that turn on only when they have something to do.
- Image Studio adapts to the window: nothing slips under the right edge, the toolbar gives way to the window size, the canvas shrinks freely.
- Image Studio starts in Nerd mode (complete): all the extra tools above are in the window only, never in the editor tab.

## 0.9.77 - 2026-11-08
> Animated guide header, quicker cats

### Improved
- User Guide: the two static logos at the top are replaced by an animated header in the ScreepT style (teal pixel rain, a light sweeping over the title, blinking cursor, typed tagline), in dark and light theme.
- The cats visit the home page after 10 seconds of quiet (it was 30).

## 0.9.76 - 2026-11-08
> Launcher Java list with details, ScreepT Dark in the editor

### New
- Launcher > Customize: the list of installed Javas shows readable names ("jdk Oracle 21 Java SE", with a progressive number when the same name appears more than once) and, under the list, a box with the path, version, vendor, type (JDK or JRE), architecture, the launch parameters in use, compatibility with ScreepT and links to the documentation.
- Pixel art cats (Feo and Nano) play on the launcher: they run on the loading image, sit on the edge of the controls over the animated backdrop, and visit the ScreepT home after 30 seconds of quiet (a small overlay of the ScreepT window, hidden when ScreepT is not the active window).

### Improved
- Network Info: the speed test now measures the real upload speed too, with an HTTP upload to a public server (with a fallback server).
- Launcher buttons in a pixel art style (8-bit bevel, pixel arrow, monospaced capitals) and a loader with a block progress bar.
- Quick, medium and full automated test levels (run_tests.cmd|.sh, see TESTING.md).
- The text editor moves to the ScreepT Dark theme the first time you start after the update (only once: a theme you choose afterwards is kept).

## 0.9.75 - 2026-11-08
> Image Studio with batch and tabs, viewers integrated in the editor tabs, new LogSearch and terminal search

### New
- Image Studio (replaces Image Viewer and Image Converter, now a single Various > Image Studio entry, Alt+Shift+O; the separate converter window and Alt+Shift+I are gone): several images in tabs, conversion options in an accordion on the right that grows with the mode (Basic: convert and resize; Classic: + adjustments, filters, rotate, flip, copy, paste; Nerd: + histogram, gamma, hue, temperature, resampling choice, pixel grid, metadata). 17 ready made filters (black and white, sepia, vintage, warm, cool, vignette, sharpen, blur, emboss, edges, posterize, pixelate...), live preview, undo, estimated size of the exported file, export to every format of your Java (PNG, JPG with quality, BMP, GIF, TIFF where available).
- Batch processing in Image Studio: choose the settings on a built-in sample or on one of your photos, then apply convert, resize and every edit to a list of images or a whole folder, with name pattern ({name}, {n}, {date}), counter, digits, collision policy and target folder.
- Integrated viewers in the editor tabs, like an IDE: images open in Image Studio, .log files in LogSearch and .html pages in a simple HTML viewer, instead of separate windows. Graphic preferences > Text editor > Viewers chooses the viewer of each extension (filter as you type); only your differences from the defaults are saved in the ini file.
- LogSearch redesigned in the ScreepT Dark style, quicker to use: it searches while you type, with a log-oriented syntax (all the words in any order, "exact phrase", -excluded word) besides regular expression, match case and whole word. Recognises the level of every line (ERROR, WARN, INFO, DEBUG, TRACE; a stack trace follows its entry): coloured stripe in the gutter, errors and warnings on the map beside the scroll bar, level chips with the line counts that filter the matches, F8 / Shift+F8 jump between errors. The matches list shows the context lines you choose (0 to 8) and can be exported to a file. Bookmarks (Ctrl+B, gutter click, F2 / Shift+F2), go to line (Ctrl+G) and zoom (Ctrl +/-/0).
- Interactive terminal: search bar docked above the screen (magnifier button, Ctrl+Shift+H, Cmd+Shift+H on macOS). Highlights every match in the whole history, with match case, regular expression and whole word options, match counter, previous/next buttons (Enter / Shift+Enter) and Esc to close. It also works after the process has ended.
- Interactive terminal: Shift+PageUp / PageDown / Home / End scroll the history from the keyboard. History grown from 5,000 to 10,000 lines.

### Improved
- License window (launcher and app) redesigned: sidebar with ScreepT license, libraries and reports with a filter, find in the text (Ctrl+F, F3, Shift+F3), zoom (Ctrl +/-/0), colors that follow the theme, clear Accept / Decline. Accepting no longer shows an extra confirmation popup.
- Network Info redesigned: cards for interface, addressing and name servers with a copy button per value and "Copy all" (Ctrl+Shift+C), speed test with large readings, F5 to reload the adapters, resizable window. It now lists only physical adapters with their IPv4 address; virtual machines, VPNs, containers, tunnels, IPv6 and link-local addresses appear with the new "Deep search" checkbox (off by default).
- Hash Checker rewritten: several algorithms in a single pass, progress with Stop, drag and drop of files, expected hash with match/mismatch result, copy one or all.
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
- The "ScreepT Dark" text editor theme was missing from the theme list of the editor: it is now listed (first) for everyone, and picking "dark.xml" no longer selects it by mistake.
- The "ScreepT Dark" text editor theme is now guaranteed on every installation: besides the update package, the app restores it at startup from a copy inside the jar if the file is missing (an existing file is never overwritten).
- Plugin Manager: the Install/Uninstall button now reacts to the first click after opening the window (the clickable area was wrong), and a right click no longer starts the action.
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
