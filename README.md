Please review the [license](https://github.com/aykme/Anoti/blob/develop/LICENSE) before using

Anoti - anime info & notifs

Download the app
via [Google Play](https://play.google.com/store/apps/details?id=com.alekseivinogradov.anoti)

Anoti allows you to always be aware of the release of new episodes!

■ Choose anime to your liking from the list of ongoings and announcements or using the search

■ Subscribe to the expected anime and receive notifications about new episodes

■ See information about the date of the next episode

■ Use the convenient "Favorites" section to control all subscriptions

■ Keep a record of the episodes you've watched so you don't forget where you left off

<img src="https://github.com/user-attachments/assets/dee9275c-b37f-40d3-b33a-25f3e9d4fc22" width="100"  alt=""/>
<img src="https://github.com/user-attachments/assets/4955a06c-0280-4702-8047-296f0200e184" width="100"  alt=""/>
<img src="https://github.com/user-attachments/assets/853345b7-2704-48e8-9ebb-8a2b42ee0ee9" width="100"  alt=""/>
<img src="https://github.com/user-attachments/assets/4b0b5168-42ee-45b3-b1cf-4be5ad3f9d71" width="100"  alt=""/>
<img src="https://github.com/user-attachments/assets/c5ac4d40-e5be-4172-92b4-c1f721001e04" width="100"  alt=""/>
<img src="https://github.com/user-attachments/assets/889e4b8e-c06a-43e9-b446-02880dfe3ca7" width="100"  alt=""/>
<img src="https://github.com/user-attachments/assets/b2d04efd-141d-4ac7-bc4a-5285abaf2612" width="100"  alt=""/>

Technology stack:

1. A fully [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) app with an
   Android and an iOS project. The business logic, the UI, the data layer and DI are shared by
   both platforms. Platform code is kept to what only the OS can do, such as notifications and
   background updates.
2. The UI is built with [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)
   and shared by Android and iOS.
3. Multi-module architecture. MVI is based on [MVIKotlin](https://github.com/arkivanov/MVIKotlin),
   navigation on [Decompose](https://github.com/arkivanov/Decompose).
4. Kotlin Coroutines and Flow.
5. Networking is implemented via [Ktor](https://github.com/ktorio/ktor).
6. The local database is implemented via
   [Room](https://developer.android.com/kotlin/multiplatform/room) for Kotlin Multiplatform.
7. Custom pagination, written without third-party libraries and designed to fit MVI and UDF.
8. DI is implemented through [kotlin-inject](https://github.com/evant/kotlin-inject).
9. Unit testing is done with
   [kotlin-test](https://github.com/JetBrains/kotlin/tree/master/libraries/kotlin.test). Test
   doubles are handwritten fakes, plus Ktor's `MockEngine` for the network. Line coverage over
   the whole project is held at no less than 95%, measured with
   [Kover](https://github.com/Kotlin/kotlinx-kover) and enforced by a build that fails below it.

Minimum versions:

1. The app runs on Android 10 (API 29) and newer, and on iOS 16 and newer.
2. The Android app is built with Android Studio Panda 2 (2025.3.2) or newer. It is the oldest
   Android Studio that supports the project's Android Gradle Plugin.
3. Gradle runs on JDK 21. When Gradle finds no JDK 21 on the machine, it downloads one during the
   first Gradle sync.
4. The iOS app is built with Xcode 26.4 or newer, the release the project's Kotlin version is
   documented against. Xcode 26.4 runs on macOS Tahoe 26.2 and newer.
5. The iOS build runs the project's Gradle build as well, so the Mac also needs a JDK. Any JDK
   starts the build, the one inside Android Studio included, and JDK 21 is downloaded like on any
   other machine. The Android SDK is not needed for it.

Setting up the Android project:

1. Install Android Studio Panda 2 (2025.3.2) or newer. Its first-launch wizard downloads the
   Android SDK.
2. Open the repository root in Android Studio and wait for the Gradle sync to finish. No separate
   JDK install is needed. Gradle first looks for an installed JDK 21: the one `JAVA_HOME` points
   to, the usual install folders of the system, and JDKs installed through Android Studio. Only
   when it finds none does it download JDK 21 during that sync.
3. Run the `androidApp` configuration.

Setting up the iOS project:

1. Install Xcode 26.4 or newer.
2. Give the build a Java, in one of two ways:
   - Install Android Studio into the standard `/Applications` folder. The build finds the Java
     inside it there, so no separate JDK is needed.
   - Or install any JDK with its ordinary installer.
3. Open `iosApp/iosApp.xcodeproj` in Xcode, pick an iPhone simulator and run the `iosApp` scheme.
   The first build takes long: it compiles the whole shared code for iOS.
