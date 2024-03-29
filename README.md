# PhoneTest
Android phone app for communication with Allen Bradley programmable logic controllers (PLC) via WiFi on a local network.

Intended to be used solely as a testing tool (not fit for any production environment).

Minimum requirement is Android 4.1 (API level 16) while targeting Android 11 (API level 30). This should cover lots of old Android phones as well as new.

Designed for portrait orientation and also designed to hide the status bar and keep the screen turned on.
In order to work as designed, it uses permission to access Internet and Network State (see app/src/main/AndroidManifest.xml file).

It is using the following open source libraries, added to the app in the form of AAR modules:

- [libplctag](https://github.com/libplctag/libplctag) v2.1.20
- [jna](https://github.com/java-native-access/jna) v5.6.0

Newer versions of the libplctag library, v2.2.0+, will probably break some functionality of this app (I am currently aware of MicroLogix PID not working so it will not show as an option if you update the library).
The AAR folder was added and has a couple of library versions just for the convenience.

This is the procedure for changing the library (upgrade or downgrade):
- Right-click the `libplctag` folder and select `Load/Unload Modules`, with libplctag highlighted click `Unload` and `OK`.
- Right-click the "libplctag" folder again and select "Remove Module", confirm removal.
- Right-click the `libplctag` folder again and select `Delete`, confirm deletion.
- On the menu select `File/Project Structure`, in the `Modules` window click `+` and select `Import .JAR/.AAR Package`, browse to the folder where the new library is, select it and click `Finish`.
- On the menu select `File/Save All` and then do `File/Sync Project with Gradle Files` (this will make the newly created `libplctag` folder name go bold)
- In the project tree, open `settings.gradle` file and delete the duplicate line that says `include ':libplctag'`.

The app is also using a modified version of the Tag.java wrapper, part of the libplctag project, so a tag_id for every tag created could be mapped and used in the software.
Other modifications would be related to added methods for unsigned integers, previously mentioned in the related libplctag4android project (see below) as well as an additional experimental 128-bit support. All the modifications can be seen just by openning this project, navigating to the "libplctag" project, openning the AAR file and navigating through classes.jar.

Related project: [libplctag4android](https://github.com/libplctag/libplctag4android)

This app is as experimental as the above mentioned related project but a bit more elaborate.
The above mentioned related project can be used to compile the latest prerelease version of the libplctag library.

See [this link](https://github.com/libplctag/libplctag4android/issues/1) for instructions on how to create libplctag AAR.

# Screenshot

![Start Page](screenshots/App%20Running%20Screen%20(Pixel%203a%20Emulator).png?raw=true)

# Functionality
- Only a single value will be displayed per tag entered, either of string / char / integer / float ... etc.
- The default PLC values are set in the `MainActivity.java` file (variables: abCPU, abIPAddress, abPath, abProgram, abTimeout, boolDisplay) and can be changed to match any specific setup.
- The app provides automated READ (`Auto ON`) while, during this operation, unused tag spots can be populated and used to write in parallel.
- The `Write` buttons are enabled when the tag is present AND the value to write is present.
- The `Get Tags` button will fetch ControlLogix tags and selecting any of the fetched tags will copy it to the clipboard.
- The Custom String Length has to be specified when the `custom string` data type is selected.
- The `Gauge` screen is to demo [AndroidUserControls](https://github.com/GitHubDragonFly/AndroidUserControls) but does allow setting the PLC Tag for either or both: the green LED and the gauges (multiple data types supported).
The red and blue LEDs are only visible and functional in the Demo mode.
- Modbus functionality of the libplctag library is not included in this app.

There might be bugs in the app. Not everything could be tested by me, since I don't have access to all the different PLCs supported by the libplctag library.
See the libplctag website for all PLCs supported by the library.

Screenshots folder has pictures of this app running inside the Android x86 emulator phone (Pixel 3a API 30) as well as BlueStacks emulator.
The app was also tested as working on an old Samsung phone with arm processor and Android 4.4.2.

# Build

All it takes is to:

- Download and install Android Studio.
- Download and extract the zip file of this project.
- Open this as an existing project in Android Studio and, on the menu, do a Build/Rebuild Project.
- Locate created APK file in this folder: app/build/outputs/apk/free/debug.
- Copy the APK over to your phone and install it (you might need to enable the "Install from Unknown Sources" option).

## Note: This app can be installed on a tablet as well
You could achieve any orientation on the tablet and prevent the MainActivity and GaugeActivity from restarting when orientation changes.
Just make the following changes within the AndroidManifest:
 - remove all `android:screenOrientation="sensorPortrait"` entries
 - add `android:configChanges="orientation|screenSize|screenLayout|keyboardHidden"` just below each of the following lines `android:name=".MainActivity"` and `android:name=".GaugeActivity"`

Otherwise it will be portrait orientation only.

# Licensing
This is all dual licensed under Mozilla Public License 2.0 and GNU Lesser/Library General Public License 2.1 to cover for the use of libplctag and jna libraries.

# Trademarks
Any and all trademarks, either directly or indirectly mentioned in this project, belong to their respective owners.

# Useful Resources
Other open source projects:
- The AdvancedHMI website [forum](https://www.advancedhmi.com/forum/), providing communication with a large number of different PLCs.
- The [pylogix](https://github.com/dmroeder/pylogix) and [pycomm3](https://github.com/ottowayi/pycomm3) repositories.
