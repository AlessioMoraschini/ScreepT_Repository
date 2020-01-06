 ScreepT - Tutorial   

![](./Images/icon.jpg)

ScreepT
=======

![](./Images/icon.jpg)

  

A smart crypter... and much more!

By A.M. Design and Development - 2019 - Version 0.9.17

**(This version was built on: 2020-01-06T23:21:38Z)**

* * *

Welcome to ScreepT tutorial, here you can find all infos about how to use ScreepT and his functions. ScreepT was born as a crypter... but it became a multi-tool written for fun :) Hope you'll enjoy it! If you find something you'lll like to improve, feel free to write me, suggestions are welcome :)

* * *

  
[](#indexList)

INDEX
=====

1.  [Installation and First launch](#InstallationNusage)
    
    *   [Launcher and compatibility](#subLauncherCompatibility)
        
    
    *   [Launcher settings](#subSettings)
        
    
    *   [OS dependency](#subOSdepLauncher)
        
2.  [Functions description and tips](#FunctionsDescNtips)
    
    *   [File](#funcFileSubMenu)
        
        *   [CONFIGURATION MANAGER](#subSectTitleConfigManager)
        *   [RELOAD CONFIGURATION](#subSectTitleReload)
        *   [LOAD CONFIGURATION](#subSectTitleLoad)
        *   [EXPORT CONFIGURATION](#subSectTitleExp)
        *   [SAVE CURRENT AS DEFAULT](#subSectTitleCurrAsDef)
        *   [RESET ORIGINAL CONFIG](#subSectTitleResetOrig)
        *   [EXIT](#subSectTitleExit)
        
    *   [Preferences](#funcPreferencesSubMenu)
        
        *   [GRAPHIC OPTIONS](#subSectTitleGraphOpt)
        *   [GENERAL OPTIONS](#subSectTitleGenOpt)
        
    *   [ScreepT](#funcScreeptSubMenu)
        
        *   [SCREEPT](#subSectTitleCrypter)
        *   [KEY IMAGE GENERATOR](#subSectTitleKeyGenImg)
        *   [KEY DICTIONARY](#subSectTitleKeyDict)
        
    *   [Various](#funcVariousSubMenu)
        
        *   [TEXT EDITOR](#subSectTitleTxtEditor)
        *   [SYSTEM INFORMATIONS](#subSectTitleSysInfo)
        *   [NETWORK INFORMATIONS](#subSectTitleNetInfo)
        *   [IMAGE CONVERTER](#subSectTitleImgConverter)
        *   [FILES AND FOLDERS](#subSectTitleFilesNFolders)
        
    *   [Help](#funcHelpSubMenu)
        
    
3.  [Keyboard shortcuts list](#ShortcutsKeyboard)
    
    *   [General Purpose shortcuts](#shctsMainGui)
        
    
    *   [Text Editor shortcuts](#shctsTextEditor)
        
4.  [Credits and links](#CreditsNLinks)
    

  

* * *

1 - INSTALLATION AND FIRST LAUNCH
=================================

1.1 - LAUNCHER/COMPATIBILITY
----------------------------

*   ScreepT is a ready to launch software written in java, but, it has a built-in custom launcher meant to check the state of dependencies on your computer before to launch, and warn you if it found something missing (provinding different ways to install them).
    
*   You can find the launcher inside main folder once it's extracted from the given .zip archive, just use the correct one for your OS. Remember to download the right version for your OS! (The software is designed to run well under Windows, Linux, and MacOS, but it is possible to use it -with limited functions- under other OS with Java interpreter installed, AKA JVM/JRE/JDK ).  
      
    Notice: it's still possible to just launch the "ScreepT\_Vxx.jar" that you find inside the "/application" folder, but remember: it's not designed to render and work the best this way... so you might have a non-optimal experience running the software this way.  
      
    Notice: full support is granted for java versions series 1.8.XX - 11.X.X : previous version will not work at all, while on newer versions some functions can be disabled or limited for compatibility reasons. You can either download Oracle's main version ([JVM download page](https://www.oracle.com/technetwork/java/javase/downloads/jdk11-downloads-5066655.html)) or openJdk version ([openJdk download page](https://adoptopenjdk.net/releases.html)), or download it from another source.  
    At the end of the process just refresh and/or restart ScreepT. During the launch process JVM will be searched automatically, but if it's not found you can specify manually the path of you JVM /bin folder ;).  
      
    After you run ScreepT\_launcher executable you'll be lead to a basic panel where you can choose to start the software, select specific settings to run the software, or check the dependency.  
    If something is missing you'll be asked to update/download/install it (if you downloaded version without installers you'll need an internet connection for that). Once the required dependencies are installed, you can start the software, or if still having problems, try to restart the launcher.
    
    You don't need to pass through the launcher panel each time... if you want, after the first launch is gone, you can disable launcher panel in the "general options" tab in ScreepT menu bar under the voice "preferences".
    
    1.2 - LAUNCHER SETTINGS
    -----------------------
    
    As sayd before, you can customize settings of your launcher, by clicking "Customize launch parameters". A new window will be opened: here you can set max and min ram memory allocated when using ScreepT. Also the stack size and LOGGING LEVEL can be adjusted in this section.
    
    **IMPORTANT:** remember that when using values that are very similar or the same, the software can use more than needed OS calls to stay in the small gap between them, resulting in a slow execution. You are advised to use default values if you don't have any particular needing or you are an expert user.
    
    1.3 - OS DEPENDENT LAUNCHER
    ---------------------------
    
    ScYpT is written in java language, that can run almost everyehere. Still said this, remember that you can use all its funcionalities (in an optimized way), just launching the right launcher for your OS.
    
    *   **Windows:** double click on .exe launcher
        
    *   **Linux:** double click on .sh launcher _\- Coming soon!_
        
    *   **Mac:** double click on .dpkg launcher _\- Coming soon!_
        
    

  

* * *

2 - FUNCTIONS DESCRIPTION AND TIPS
==================================

2.1 - FILE SUBMENU
------------------

In this section you can find all the functions to save/load configurations, or open the more detailed configuration manager view. You can save, export, load, reset, reload your favourite configuration, or even use the default one. When selecting a specific configuration you specify different variables such as textEditor colors, text size, menu text size, graphic general style, type of encryption, default export/load folder, default image extension or preferences, and others like that.  
The main difference is that in the tree view you can see the .ini configuration files loaded and mantained at each execution of ScreepT, unless you change or delete them. Remember that newer version of ScreepT can have some differences or not load some values, when loading configurations exported/saved in previous versions. Also notice that if you have Autosave option active, your configuration will be automatically saved in the current loaded every N seconds or when the program exits.  
  
*   ![](../Icons/Menu_Icons/manager.png) CONFIGURATION MANAGER

This view contain a tree visualisation of the loaded configuration, a text print with the values contained in the current one, and different functions tha are basically the same you can find in the main menu bar items under the "File" element. **By right clicking** on an element in the list, you view different actions on the selected configuration from the loaded ones, such as select, delete, delete all or refresh.  
The actions that fire when clicking the action buttons are described in the next points.

*   ![](../Icons/Menu_Icons/reload.png) RELOAD CONFIGURATION

By clicking on this element you can reload the current loaded configuration. Remember that any values that are not yet saved will be lost after the original one will be reloaded.

*   ![](../Icons/Menu_Icons/loadConfig.png) LOAD CUSTOM CONFIGURATION

This function allows you to load previously saved/exported configuration by selecting them from your file System. If for some reason any value is not found in the given configuration file, a default one will be loaded. It's also needed to load a valid .ini configuration file saved from ScreepT software. Other files are not accepted, and lead to default values to be loaded.  
After a file-load, a copy of it can be found in the configuration manager inside the tree view of the user loaded files. Here resides the used file you load.

*   ![](../Icons/Menu_Icons/export.png) EXPORT CURRENT CONFIGURATION

This function allows you to export in a directory you choose the current values of the running instance of ScreepT. Please remember that theese can differ from the ones that are actually present in the configuration file loaded, if the loaded ones are not yet saved to current file. Exported configuration files can later be imported using the load function.

*   ![](../Icons/Menu_Icons/save.png) SAVE CURRENT CONFIGURATION

This function will save the configuration in the current loaded file, and will not be loaded in the next start unless you are using the default one, or you load it again. To save as default use the dedicated function.

*   ![](../Icons/Menu_Icons/saveAsDefault.png) SAVE CURRENT AS DEFAULT

When clicking this button, your configuration will be saved as default one, and the current loaded will be also default one. This permits you to find the next values saved as default, even after restarting ScreepT next time, without having to manually reload them each time.

*   ![](../Icons/Menu_Icons/reset.png) RESET ORIGINAL

After you click this you will be asked to confirm that you want to overwrite default configuration with the default one that you use first time you started ScreepT. This is an empty configuration with default values.

*   ![](../Icons/Menu_Icons/turnOff.png) EXIT

This closes the software, deleting temporary files if present, and saving current configuration if autosave option is ON.

2.2 - PREFERENCES SUBMENU
-------------------------

*   ![](../Icons/Menu_Icons/graphics.png) GRAPHICS OPTIONS

In this section you can choose between some graphic settings that will be mapped into current configuration file currently in use, such as:

*   Graphic User Interface type: choose between different available look and feel options. The default is Nimbus if available, for which you can also use dark mode;
*   Menu text size: this will modify the size of menu bar text (you have to restart ScreepT before to see changes);
*   Log text size: this will modify the size used in panels that display process' output;

  
*   ![](../Icons/Menu_Icons/options.png) GENERAL OPTIONS

Here you can change your general preferences that are contained in the loaded configuration. You can choose to save, discard, use them, or return to the settings read when opening this section. In this panel you can configure things like:

*   Autosave option, and autosave interval in seconds (equivalent minuts+seconds will be displayed);
*   Launcher panel visible at start, if deselected ScreepT will be directly launched, unless some blocking problems will be detected at start;
*   Username, that will be displayed in the main title and in some other places to customize you experience
*   Default charset: this will be used when reading text from file or during text save to file.

  

2.3 - SCREEPT SUBMENU
---------------------

This is the core function of this software, and implements a multi crypter/decrypter of files and folders, with a key generator and custom key dictionary manager.  
In this section you can find all the stuff you need to encrypt and decrypt the files you need to keep secret, using a custom algorithm and different kind of codings.  
  
ScreepT uses AES-128 bit, combined with Steg-crypter, that can generate an image containing either cripter or raw bytes from the source stream selected, using a text-key for AES encoding, combined with a PNG image key. You can find information about how it works in the ScreepT section below :)

IMPORTANT: your source files will NOT be deleted or modified, only new ones will be created in the directory you choose to put them in.  
  
*   ![](../Icons/Menu_Icons/crypter.png) SCREEPT - CRYPT AND DECRIPT FILES
  
When opening this subPanel, you'll find:

  
*   On the left side you have informations about key Image-dictionary, a log for current operations, and a button to select a one-use key image importing it from your file system.
  
*   On the right side you have a drag 'n drop panel on which you can drag the files you want to code/decode, or select them using the given button "add source file or folder". After you load them you can add more content simply repeating this operation. The added files will appear in the "Selected files pool", in which you can see the files that will be crypted or decrypted(according to the button you click after).  
    Notice that until you crypt/decrypt them no file will be created.  
    By right clicking on this panel you can see a pop up with useful operations on the loaded files. Also this operations will not modify you source file, but only the link created to them.
  
*   On the Top you will find all informations about keys and files loaded, with a useful feedback message giving you information about missing keys or errors.  
    In this panel you can also provide the AES text key used to crypt/decrypt, and specify which type of coder you want to use. Please be sure to use the right mode for your needs, and remember that you need to use the same mode if you want to decrypt the crypted result!  
    When you load a folder the result will contain all his files, mantaining the directory structure.  
    When using STEG/COMPLETE mode each file coded will be a folder, for this reason when using this modes you can only decrypt folders. Be sure to import all the result folder when decrypting this kind of files!  
    When decrypting files you'll be asked to choose between putting them in a temporary on your folder (that will be automatically deleted as soon as you close the software), or use a custom directory you choose.
  
*   In the central section you'll find a text area that will print all informations about current process, and files/folders being crypted.

  

CRYPTER MODES: Description
--------------------------

*   AES\_ONLY This mode will use only the AES encryption algorithm, given a text key that you have to write in the password box on top panel. For top security, you should use a 16 characters-long password.  
    IMPORTANT: you will need both text password to decrypt the result crypted files, be sure to remember it!  
    
  
*   STEG\_ONLY This mode will use only the STEG encryption algorithm, given a PNG image key, that you have to specify in the left panel, either choosin' it from dictionary, or load it for one use only and then ScreepT will forget about it unless you choose again it. For top security, you should use a random pixels image that you can create with the given "Image Key Generator" that you find in ScreepT subMenu.  
    When using this mode, your files will be encoded one by one using the value of each pixel from key image. the result will be contained in as much images as needed to contain source file, for each file in the source pool, or contained in sosurce folders.  
    IMPORTANT: you will need the key image to decrypt the result crypted files, be sure to keep it in a safe place!  
    The number of images created depends on the size of source file and size of image:  
    
    1.  If you have to encode a large number of small files use a small image key, or the result will waste space.
    2.  If you have to encode big files use a medium-big image, resulting in a faster encoding process.
    
      
    
*   COMPLETE This mode will combine AES and STEG mode this way: first the source bytes from each file will be coded with AES encryption standard, and then they will be encrypted with STEG mode explained before.  
    When used with "Ultra secure" option, this grant a very secure encription by increasing exponentially the entrophy and the number of possible combination... making it near impossible to decrypt them without both text key and Image key.  
    IMPORTANT: you will need both text password and key image to decrypt the result crypted files, be sure to keep them in a safe place!  
    The number of images created depends on the size of source file and size of image:  
    
    1.  If you have to encode a large number of small files use a small image key, or the result will waste space.
    2.  If you have to encode big files use a medium-big image, resulting in a faster encoding process.
    
      
    
*   ULTRA SECURE OPTION
    
    In STEG/COMPLETE modes, when this checkbox is selected, files will have an extra phase in which source bytes of STEG phase will be reordered accorging to values contained in the given Key Image pixels: ScreepT will use theese values as "reorderKey index" to change the order oof source bytes before to crypt or decrypt them.  
    IMPORTANT: if you cript files with this mode, you have to select this option also when you'll have to decript the crypted result later. If not, decription won't succeed.
    

*   ![](../Icons/Menu_Icons/imageWriter.png) KEY IMAGE GENERATOR

In this panel you can generate your own random key image... But remember where you put it: ScreepT is so secure that loosing this key will result in the impossibility to recover your crypted files!  
I suggest to keep the keys in a pen drive, keeping it in a safe place maybe right next you heart :D

You can choose different options when generating a key image, and you can use it to crypt files or folder with STEG or COMPLETE mode. When generating a key you can specify:

1.  Width and Height of image key: remember to choose dimension according to the size of the files you want to crypt. As said before, you can incurr in slow encoding or, worst, in waste of disk space if selecting it in the wrong way. A fast rule to follow can be:  
    \=> Optimize space = small image.  
    \=> Optimize speed = size near to source files size.
2.  Type of key Image: choose between number of bytes for each pixel of generated image. Higher value means generally more security.
3.  "Generate key" button: click this button to generate a new key image with chosen parameters... Just wait for the process to finish :)
4.  "Export" button: click this to save the generated image into a folder on your file System
5.  "Add to Dictionary" button: click this to add the generated key to the dictionary. This way it will be saved in a directory inside ScreepT main directory, and will'be accissible either from dictionary function and ScreepT function seen before.

*   ![](../Icons/Menu_Icons/keyDictionary.png) KEY DICTIONARY

This is a function similar to the one you can find embedded on left panel in cripter, but with some extra option. Using this functionality you can have a specific view on which PNG images you have loaded in your personal library, that is saved inside current SScreepTsource launch folder.  
The tree view of files is updated in real time, and by right click on it you can view different actions, such as delete all/selected files from dictionary, manual refresh and viewing informations about selected file. In this panel you can also open the dictionary in system explorer to manage manually the files contained in it.

IMPORTANT: always remember to do not delete ScreepT folders when you view in system explorer, or the software may not work as expected or run into unexpected crashes. However, if you experience any problem, you can run a new instance of ScreepT and reimport keys manually. Always remember to backup in a safe place your encriptiion key images!! ;)


-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

2.4 - VARIOUS SUBMENU
---------------------

*   ![](../Icons/Menu_Icons/textEditor.png) TEXT EDITOR

This is a minimal text editor with basic functions, with some extra functionality such as:

*   TEXT SPEAKER \- you can use this by clicking on ![](../Icons/Text_Editor/Speaker.png) or using the shortcut \[Ctrl+spacebar\]. This way you will hear the text visible in text-area with english pronunciation. You can also choose the voice speed rate, dragging the selector over the speaker button.
*   TEXT CRYPT/DECRYPT \- with a non-empty password specified in the password-field on top, you can encrypt/decrypt with AES (128bit) the text loaded in text-area, just by clicking on crypt ![](../Icons/Text_Editor/Crypt.png) or decrypt ![](../Icons/Text_Editor/Decrypt.png) (or using the shortcut \[Ctrl+shift+C / Ctrl+shift+D\]).
*   TEXT MANIPULATION \- There some built in function to manipulate selected/all text in ScreepT. By clicking on ![](../Icons/Menu_Icons/uppercase.png) you can make all selected text uppercase, clicking **\[.Tt\]** can make all text after dots or separators be uppercase, while a click on **\[Tt Tt\]** make text separed by a space starting words with uppercase.
*   TEMPLATES INJECTION \- clicking on **\[Templates\]** button, a new dialog will open staying on top till close, and in which you can select an existing template, create one from your currently selected text, or even define your own template using current file. To use a template just select it from the list and click on "Inject Template"
*   CUSTOMIZE COLORS/FONT \- you can customize color of background ![](../Icons/Text_Editor/BackgroundColor.png) and text ![](../Icons/Text_Editor/TextColor_2.png) just by clicking on the relative button: a chooser dialog will appear and you'll be able to choose and preview the selected colors on the loaded text.  
    With the same process is possible to select your preferred font, just by clicking on ![](../Icons/Text_Editor/Font_2.png).  
    If you'll press OK the selected colors/font will be added to current volatile configuration (theese will not be written to configuration file until you explicitly save them, or until autosave will occur, if selected).

*   ![](../Icons/Menu_Icons/hardware.png) SYSTEM INFORMATIONS

IMPORTANT: Download "hardwareInfo" plugin to use this function.

In this section you can find many informations about you system, the java version in use, and current usage of CPU and RAM by ScreepT, but not only. In fact, here you can find informations about your currently linked displays, HD, RAM, CPU.  
In next versions of ScreepT many more options could appear, stay tuned!

*   ![](../Icons/Menu_Icons/network.png) NETWORK INFORMATIONS

In this section you can find many informations about your network cards, and their main connection informations, such as local IP, public IP(only if connected to internet), MAC address, gateway, DNS, MTU, and delay. Just choose your network card from the list on the top center area :)

On the right you can find a panel with information about you connection speed, related to the selected network adapter from the list above. To discover up/down speed and delay of your network card, select one with an active internet connection, press on "Calculate connection speed", and wait for the test to finish to read results. According to the test type selected (Short/medium/large) this can require up to some minutes.  
This button will become clickable only when there is an active connection from the current selected network card.

*   ![](../Icons/Menu_Icons/imageConverter.png) IMAGE CONVERTER

Here you can find a selection of useful conversion tools for image conversion. Notice that this is a beta version yet, it still has to improve in next versions of ScreepT ;)

Let's see a list of some things this converter can do (for now):

*   Change size \- Just change size of the ones you like. If you want to keep proportions between height and width, be sure "original ratio" is selected.
*   Change extension \- Just select the output extension you want to have after conversion. If you want to keep the original one, be sure "original format" is selected.
*   Choose resize algorithm \- An image can be resized in different ways, each one with different load on CPU (and this can have a big impact on time for conversion and final quality): you can select your preferred from the given list on the left.
*   Choose conversion quality \- Especially when using lossy output formats like "JPEG/JPG", changing the quality (from 1 to 100) you can choose the quality of the compression with selected algorithm. Notice that with loseless types this will result in a small optimization or none. For example, PNG format is loseless, but it allows to have a little improvement in terms of disk space, optimizing pixel rapresentation.
*   Export/Add to dictionary \- when you have chosen image source (clicking the button on the right) and output parameters, you can either export to a external directory, or add it to key dictionary, to use it later in crypter section (notice that this way the image will be saved in PNG format in dictionary, cause screept handles only this exention).  
    **Just click** on the relative button you can find on the right side of the panel.
*   Reset original size \- click on the **reset size** button to reset the output size to the original one.

*   ![](../Icons/Menu_Icons/folderActions.png) FILES AND FOLDERS ACTIONS

In this panel you can find some useful functions to manage files and folders located on your File System.

You can choose between:

*   ![](../Icons/Menu_Icons/uppercase.png) UpperCase files and folders \- just click and select a file or folder to make it (and all its contained files -even in subfolders- if it's a folder) start with an Uppercase letter.
*   ![](../Icons/Menu_Icons/createFolder.png) Create directories from string path \- click on this and type the path of the folder you want to create. ScreepT will create all necessaries folders that are missing.  
    (Ex. "C:/folderA/folderB/folderC/newFolder").
*   ![](../Icons/Menu_Icons/cloneDirectory.png) Copy directory structure \- click on this if you want to recreate the structure of a directory without copying the files contained in it and its subfolders. In fact, you will only copy folders contained in the selected one. **You can select only folders in this function.**
*   ![](../Icons/Menu_Icons/targetedDelete.png) Delete files by extension click this if you want to delete all files that end with specified extension, which can include the end charachters of filename. **You can select only files in this function.**
*   ![](../Icons/Menu_Icons/deleteFolder.png) Delete all directory's content (all except selected) click this if you want to delete all content (both files and folders) of a root folder you have to specify. You will be asked to confirm before to continue, after the root folder to empty is selected. **You can select only folders in this function.**
*   ![](../Icons/Menu_Icons/emptyDirectory.png) Delete all directory's content \- this function will delete all files contained in a root directory you have to specify, without deleting folders contained. **You can select only folders in this function.**

2.5 - HELP SUBMENU
------------------

*   Visit this section if you want to know more about ScreepT fuctions, about A.M. Design 'N Development, visit A.M. Official Website, or simply return to start page.
  
IMPORTANT: Always remember that changing section you won't lose any progress of your current opened work, just go back with shortcuts or with the main menu to retrieve the stuff you were doing :)

!!! IN CASE OF ANY PROBLEM you can export the log with the special function "Export event log", that may contain useful information about the problem itself, and can help solving the bug if there is one, or understand more of what happened during execution. **Send it to me** for further bug examination! If you have problems launching the application and cannot extract this way the log, send me "/application/LOG" and "/application/LAUNCHER/LOG" folders in zip format :)

  

* * *

3 - KEYBOARD SHORTCUTS LIST
===========================

3.1 - GENERAL PURPOSE SHORTCUTS
-------------------------------

#### 0 - Emergency shortcuts (active everywhere):

*   FORCE CLOSE APPLICATION - \[Esc x 4 times\] or \[Alt + F4 x 4 times\]

#### 1 - File submenu:

*   OPEN CONFIGURATION MANAGER - \[Alt + Shift + 1\]
*   EXPORT CURRENT CONFIGURATION - \[Ctrl + Alt + Shift + E}
*   LOAD CURRENT CONFIGURATION - \[Ctrl + Alt + Shift + O}
*   RELOAD CURRENT CONFIGURATION - \[Ctrl + Alt + Shift + R}
*   SAVE CURRENT CONFIGURATION - \[Ctrl + Alt + Shift + S\]
*   SAVE CURRENT CONFIGURATION AS DEFAULT ONE - \[Ctrl + Alt + Shift + W\]
*   LOAD SCREEPT BUILT IN DEFAULTS - \[Ctrl + Alt + Shift + D\]
*   EXIT APPLICATION - \[Alt + F4\]

* * *

#### 2 - Preferences submenu:

*   OPEN GENERAL OPTIONS - \[Alt + Shift + 2\]
*   OPEN GRAPHICS OPTIONS - \[Alt + Shift + 3\]
*   TOGGLE FULL SCREEN MODE - \[Alt + Shift + Enter\]
*   IMMERSIVE MODE ON/OFF - \[Ctrl + Alt + Shift + M\]
*   EXIT IMMERSIVE MODE - \[ESC\]

* * *

#### 3 - Screept submenu:

*   OPEN SCREEPT - \[Alt + Shift + S\]
*   OPEN PASSWORD KEYGEN - \[Ctrl + Alt + Shift + K\]
*   OPEN IMAGE KEYGEN - \[Alt + Shift + K\]
*   OPEN KEY DICTIONARY - \[Alt + Shift + D\]

* * *

#### 4 - Text editor submenu:

*   OPEN TEXT EDITOR - \[Alt + Shift + T\]
*   IMPORT/EXPORT CRYPTED WORKSPACE - \[Ctrl + Alt + Shift + I\]

* * *

#### 5 - Various submenu:

*   OPEN SYSTEM INFO - \[Alt + Shift + Y\]
*   OPEN NETWORK INFO - \[Alt + Shift + N\]
*   OPEN HASH CHECKER PANEL - \[Alt + Shift + U\]
*   OPEN COLOR PICKER PANEL - \[Ctrl + Alt + Shift + P\]
*   OPEN CLIPBOARDS-MEMO PANEL - \[Alt + Shift + P\]

* * *

#### 5.1 - Files and Folders actions submenu:

*   UPPERCASE ALL FOLDER CONTENT - \[Alt + Shift + 0\]
*   CREATE FOLDERS FROM STRING PATH - \[Alt + Shift + 9\]
*   COPY DIRECTORY STRUCTURE - \[Alt + Shift + 8\]
*   DELETE FOLDER CONTENT BY EXTENSION - \[Alt + Shift + 7\]
*   DELETE ALL CONTENT FROM DIRECTORY (RECURSIVE) - \[Alt + Shift + 6\]
*   DELETE ONLY FILES CONTAINED IN DIRECTORY (RECURSIVE) - \[Alt + Shift + 5\]

* * *

#### 5.2 - Media converters:

*   OPEN IMAGE CONVERTER - \[Alt + Shift + I\]

* * *

#### 6 - Help submenu:

*   OPEN ABOUT-INFO PANEL - \[Alt + Shift + .\]
*   EXPORT LOGS - \[Alt + E + .\]
*   ERASE LOGS - \[Alt + Canc + .\]
*   OPEN TUTORIAL PANEL - \[Alt + Shift + Q\]
*   RETURN TO HOMEPAGE - \[Alt + Shift + H\]

* * *

#### 6.1 - RESET PANELS:

*   RESET ALL PANELS - \[Ctrl + Alt + Shift + 0\]
*   RESET TEXT EDITOR - \[Ctrl + Alt + Shift + 9\]
*   RESET CRYPTER - \[Ctrl + Alt + Shift + 8\]
*   RESET CONFIGURATION MANAGER - \[Ctrl + Alt + Shift + 7\]

3.2 - TEXT EDITOR SHORTCUTS
---------------------------

* * *

#### On-the-fly view change:

*   MAXIMIZE TEXT AREA - \[Ctrl + M\]
*   SHOW/HIDE SCREEPT TEXT EDITOR'S HEADER - \[Ctrl + Shift + Alt + H\]
*   SHOW/HIDE SCREEPT TEXT EDITOR'S SUB-HEADER - \[Ctrl + Shift + Alt + U\]
*   SHOW/HIDE SCREEPT TEXT EDITOR'S FOOTER - \[Ctrl + Shift + Alt + L\]
*   OPEN/CLOSE FILE TREE PANEL - \[Alt + Q\]
*   OPEN/CLOSE CLIPBOARD PANEL - \[Alt + A\]
*   OPEN/CLOSE DOCUMENT MAP PANEL - \[Alt + W\]
*   SHOW AUTOCOMPLETE OPTIONS - \[Ctrl + Spacebar\]
*   Open Current file into File System - \[Ctrl + Enter\]
*   Open Current file into Shell/Cmd - \[Ctrl + Shift + Enter\]

* * *

#### On-the-fly View settings:

*   CHANGE FONT SIZE - \[Ctrl + Shift + MouseWheel\]
*   SHOW/HIDE TAB GRIDS - \[Ctrl + G\]
*   AUTO SINCHRONIZATION FILE TREE PANEL and CURRENT FILE ON/OFF - \[Ctrl + Shift + U\]
*   WORKSPACE SYNC ON/OFF - \[Ctrl + Shift + W\]
*   FILE TYPE LANGUAGE AUTO SYNC - \[Ctrl + Shift + L\]
*   SELECT NEW WORKSPACE - \[Alt + Shift + W\]
*   NAVIGATE OUT ONE LEVEL IN FILE TREE - \[Ctrl + Alt + Shift + Arrow UP\]
*   NAVIGATE IN ONE LEVEL IN FILE TREE - \[Ctrl + Alt + Shift + Arrow DOWN\]
*   INLINE TABS ON/OFF - \[Ctrl + Shift + I\]
*   ADAPT WIDTH TO SCREEN SIZE - \[Ctrl + Shift + A\]

* * *

#### Various and text editor panels:

*   OPEN SPECIAL/ESCAPES PANEL - \[Ctrl + E\]
*   OPEN TEMPLATES PANEL - \[Ctrl + T\]
*   OPEN CALCULATOR - \[Ctrl + Alt + C\]
*   OPEN COLOR PICKER - \[Ctrl + Alt + Shift + P\]
*   OPEN HASH CHECKER PANEL - \[ Alt + Shift + U\]

* * *

#### Text search panels:

*   OPEN SEARCH PANEL - \[Ctrl + F\]
*   OPEN REPLACE PANEL - \[Ctrl + R\]
*   OPEN GOTO LINE PANEL - \[Ctrl + L\]

* * *

#### File actions:

*   SEE CURRENT FILE PROPERTIES - \[Alt + Enter\]
*   RELOAD FILE FROM DISK - \[Ctrl + Shift + R\]
*   LOAD FILE - \[Ctrl + O\]
*   SAVE FILE - \[Ctrl + S\]
*   SAVE FILE AS - \[Ctrl + Shif + S\]

* * *

#### Text editing functions:

*   UNDO - \[Ctrl + Z\]
*   UNDO X5 - \[Ctrl + Alt + Z\]
*   REDO - \[Ctrl + Y\]
*   REDO X 5 - \[Ctrl + Alt + Y\]
*   COLLAPSE CURRENT FOLD \- \[Ctrl + (Numpad -)\]
*   EXPAND CURRENT FOLD \- \[Ctrl + (Numpad +)\]
*   FONT SIZE ++ \- \[Ctrl + Shift + (+)\]
*   FONT SIZE -- - \[Ctrl + Shift + (-)\]
*   UPPERCASE ALL SELECTED - \[Ctrl + ArrowUp\]
*   UPPERCASE AFTER DOTS/SEPARATORS - \[Ctrl + Shift + .\]
*   DELETE LINE - \[Ctrl + D\]
*   DUPLICATE LINE UP - \[Ctrl + Alt + Arrow up\]
*   DUPLICATE LINE DOWN - \[Ctrl + Alt + Arrow down\]
*   REMOVE SPACES IN SELECTION - \[Ctrl + (-)\]
*   DELETE CURRENT LINE - \[Ctrl + D\]
*   MOVE CURRENT LINE UP/DOWN - \[Alt + Up/Down\]
*   RECTANGULAR SELECTION - Keep pressed "Alt" while selecting text to select in rectangular mode
*   COPY TEXT TO SYSTEM CLIPBOARD (Keeping Style with copy/paste) - \[Alt + C\]
*   FORMAT CURRENT SELECTED TEXT (if void selection all will be selected) \- \[Ctrl+Shift+F\]

* * *

#### Specific Various functions:

*   SPEAK TEXT / STOP SPEAKER - \[Ctrl + Alt + spacebar\]
*   CRYPT TEXT - \[Ctrl + shift + C\]
*   DECRYPT TEXT - \[Ctrl + shift + D\]
*   BASE 64 ENCODE TEXT: - \[Alt + 4\]
*   BASE 64 DECODE TEXT: - \[Ctrl + 4\]

* * *

#### Tabs and History navigation:

*   NEW TAB - \[Ctrl + N\]
*   CLOSE CURRENT TAB - \[Ctrl + W\]
*   CLOSE ALL OPENED TABS - \[Ctrl + Alt + Shift + (-)\]
*   NEXT TAB - \[Ctrl + TAB\]
*   PREVIOUS TAB - \[Ctrl + Shift + TAB\]
*   NEXT TAB IN HYSTORY - \[Alt + right Arrow\]
*   PREVIOUS TAB IN HYSTORY - \[Alt + left Arrow\]
*   REFRESH FILE LIST IF PRESENT - \[Ctrl + F5\]
*   VERTICAL SCROLL SPEED + - \[Ctrl + MouseWheel\]
*   VERTICAL SCROLL SPEED ++ - \[Ctrl + Alt + MouseWheel\]
*   HORIZONTAL SCROLL - \[Shift + MouseWheel\]
*   HORIZONTAL SCROLL SPEED + - \[Shift + Alt + MouseWheel\]

  

* * *

4 - CREDITS AND LINKS
=====================

ScreepT is a free software created with passion during my free time and my busy nights :) Nice to meet you, I'm Alessio Moraschini, and I am the creator of ScreepT!  
  
The CryptoSTEG (the brand new core crypter-algorithm), is the result of many crazy nights passed coding strange ideas born during university period. I've to thank google search, without it it should have been impossible to find all necessary informations about functions implemented in this software :)  
  
ScreepT was created mainly with the aim to implement a new generation of crypters, more secure than existing algorithms (it improves AES-128 combining it with a my algorithm called "Steg-Crypt"). To create this software, I used different opensource public libraries, withouth them this software maybe could have not seen daylight.  
In this project are used different open source libraries, to which I really want give some special thanks. Projects that are used in this software are:

*   OSHI is used to detect hardware and some network related informations. [\[https://github.com/oshi/oshi\]](https://github.com/oshi/oshi)
*   Ini4J.Org is used to manage configuration ".INI" files. [\[http://ini4j.sourceforge.net\]](http://ini4j.sourceforge.net)
*   PngTastic is used to manage PNG conversion. [\[https://github.com/depsypher/pngtastic\]](https://github.com/depsypher/pngtastic)
*   Apache commons are widely used both for some network communication and for file related stuff. [\[https://commons.apache.org\]](https://commons.apache.org)
*   RsyntaxTextArea Is widely used in text editor, ScreepT, and also configuration manager. [\[https://github.com/bobbylight/RSyntaxTextArea\]](https://github.com/bobbylight/RSyntaxTextArea)
*   Tesseract Is used for image-pdf-text conversion and parsing [\[https://github.com/nguyenq/tess4j\]](https://github.com/nguyenq/tess4j)
*   FreeTTS is used to implement text speaker. [\[https://freetts.sourceforge.io\]](https://freetts.sourceforge.io)
*   Icons8 has been used to find ScreepT's most icons. [\[https://icons8.com\]](https://icons8.com)
*   Smashicons has been used to find some others ScreepT's icons. [\[https://www.flaticon.com/\]](https://www.flaticon.com)
*   JsBeautifier has been used to implement text editor code formatter, using external calls to a JS library. [\[https://jsbeautifier.org/\]](https://jsbeautifier.org/)

  

ScreepTⒸ - Alessio Moraschini - A.M. Design & Development - Dalmine(BG) - Italy - 2019
