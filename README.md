# PhoneTest
Android phone app for communication with Allen Bradley programmable logic controllers (PLC).

Intended to be used solely as a testing tool (not fit for any production environment).

Minimum requirement is Android 4.1 (API level 16) while targeting Android 11 (API level 30).
Designed for portrait orientation and also designed to hide the status bar and keep the screen turned on.
In order to work as designed, it uses permission to access Internet and Network State (see app/src/main/AndroidManifest.xml file).

It is using the following open source libraries, in the form of AAR modules added to the app:

- libplctag v2.1.20 ( https://github.com/libplctag/libplctag )
- jna v5.6.0 ( https://github.com/java-native-access/jna )

It is also using a modified version of the Tag.java wrapper, part of the libplctag project, so a tag_id for every tag created could be mapped and used in the software.
Other modifications would be related to added methods for unsigned integers, previously mentioned in the related libplctag4android project (see below) as well as an additional experimental 128-bit support. All the modifications can be seen just by openning this project, navigating to the "libplctag" project, openning the AAR file and navigating through classes.jar.

Related project: https://github.com/libplctag/libplctag4android

This app is as experimental as the above mentioned related project but a bit more elaborate.
The above mentioned related project can be used to compile the latest prerelease version of the libplctag library.

See this for instructions on how to create libplctag AAR: https://github.com/libplctag/libplctag4android/issues/1

# Functionality
- Only a single value will be displayed per tag entered, either of string/char/integer/float...etc.
- It provides automated READ while, during this operation, unused tag spots can be populated and used to write in parallel.
- "Get Tags" will fetch ControlLogix tags and selecting any of the fetched tags will copy it to the clipboard.
- You will need to specify the Custom String Length when the "custom string" data type is selected.

There might be bugs in the app. Not everything could be tested by me, since I don't have access to all the different PLCs supported by the libplctag library.

Screenshots folder has pictures of this app running inside the Android x86 emulator phone (Pixel 3a API 30).
The app was also tested as working on an old Samsung phone with arm processor and Android 4.4.2.

# Licensing
This is all dual licensed under Mozilla Public License 2.0 and GNU Lesser/Library General Public License 2.1 to cover for the use of libplctag and jna libraries.

# Trademarks
Any and all trademarks, either directly on indirectly used or mentioned in this project, belong to their respetive owners.
