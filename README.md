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

1. MVI based on [MVI Kotlin](https://github.com/arkivanov/MVIKotlin).
2. Multi-modularity. Both the business logic and the UI are located in the KMP modules, the UI
   built with [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform).
3. Kotlin Coroutines and Flow.
4. The local database is implemented via
   [Room](https://developer.android.com/kotlin/multiplatform/room), which now natively supports
   Kotlin Multiplatform.
5. API services are implemented via [Ktor](https://github.com/ktorio/ktor), fully in the KMP
   modules.
6. Custom pagination implemented in pure KMP, without third-party libraries, designed to fit
   MVI and UDF architectures.
7. DI is implemented through [kotlin-inject](https://github.com/evant/kotlin-inject), fully in the
   KMP modules, working on both Android and iOS.
8. Unit testing of KMP modules is done with
   [kotlin-test](https://github.com/JetBrains/kotlin/tree/master/libraries/kotlin.test). For test
   doubles I mostly use a "mock" approach, with a "fake" approach used less often. Line coverage
   over the whole project is held at no less than 95%, measured with
   [Kover](https://github.com/Kotlin/kotlinx-kover) and enforced by a build that fails below it.

Minimum versions:

1. The app runs on Android 10 (API 29) and newer, and on iOS 16 and newer.
2. The Android app is built with Android Studio Panda 2 (2025.3.2) or newer. It is the oldest
   Android Studio that supports the project's Android Gradle Plugin.
3. Gradle runs on JDK 21. When the machine has no JDK 21, it is downloaded automatically during
   the first Gradle sync.
4. The iOS app is built with Xcode 26.4 or newer, the release the project's Kotlin version is
   documented against. Xcode 26.4 runs on macOS Tahoe 26.2 and newer.
5. The iOS build runs the project's Gradle build as well, so the Mac also needs a JDK and the
   Android SDK. Any JDK starts the build, and JDK 21 is downloaded like on any other machine.

Setting up the Android project:

1. Install Android Studio Panda 2 (2025.3.2) or newer. Its first-launch wizard downloads the
   Android SDK.
2. Open the repository root in Android Studio and wait for the Gradle sync to finish. JDK 21 is
   downloaded during that sync if the machine has none, so no separate JDK install is needed.
3. Run the `androidApp` configuration.
