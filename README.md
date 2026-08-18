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
2. Multi-modularity. The business logic is located in the KMP modules. The UI and some
   frameworks are located in the android modules.
3. Kotlin Coroutines and Flow.
4. The local database is implemented via
   [Room](https://developer.android.com/kotlin/multiplatform/room), which now natively supports
   Kotlin Multiplatform.
5. Api services are implemented via [Ktor](https://github.com/ktorio/ktor), fully in the KMP
   modules.
6. Custom pagination implemented in pure KMP, without third-party libraries, designed to fit
   MVI and UDF architectures.
7. Based on Views and Fragments. I plan to rewrite the UI to
   [Compose](https://developer.android.com/compose) in the future, and then to
   [Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform) in the very distant
   future ^_^.
8. DI is implemented through [kotlin-inject](https://github.com/evant/kotlin-inject), fully in the
   KMP modules, working on both Android and iOS.
9. Unit testing of KMP modules is done with
   [kotlin-test](https://github.com/JetBrains/kotlin/tree/master/libraries/kotlin.test). For test
   doubles I mostly use a "mock" approach, with a "fake" approach used less often.
