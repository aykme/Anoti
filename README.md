Please review the ![license](https://github.com/aykme/Anoti/blob/develop/LICENSE) before using

Anoti - anime info & notifs

Anoti allows you to always be aware of the release of new episodes!

■ Choose anime to your liking from the list of ongoings and announcements or using the search

■ Subscribe to the expected anime and receive notifications about new episodes

■ See information about the date of the next episode

■ Use the convenient "Favorites" section to control all subscriptions

■ Keep a record of the episodes you've watched so you don't forget where you left off

<img src="https://github.com/user-attachments/assets/dee9275c-b37f-40d3-b33a-25f3e9d4fc22" width="100" />
<img src="https://github.com/user-attachments/assets/4955a06c-0280-4702-8047-296f0200e184" width="100" />
<img src="https://github.com/user-attachments/assets/853345b7-2704-48e8-9ebb-8a2b42ee0ee9" width="100" />
<img src="https://github.com/user-attachments/assets/4b0b5168-42ee-45b3-b1cf-4be5ad3f9d71" width="100" />
<img src="https://github.com/user-attachments/assets/c5ac4d40-e5be-4172-92b4-c1f721001e04" width="100" />
<img src="https://github.com/user-attachments/assets/889e4b8e-c06a-43e9-b446-02880dfe3ca7" width="100" />
<img src="https://github.com/user-attachments/assets/b2d04efd-141d-4ac7-bc4a-5285abaf2612" width="100" />

Technology stack:
1. MVI based on ![MVI Kotlin](https://github.com/arkivanov/MVIKotlin).
2. Multi-modularity. The business logic is located in the KMP modules. The UI and some frameworks are located in the android modules.
3. Kotlin Coroutines and Flow.
4. The local database is implemented via ![Room](https://developer.android.com/training/data-storage/room). I plan to try replace it with ![Realm Kotlin](https://github.com/realm/realm-kotlin) in the future for KMP supporting.
5. Api services are implemented via ![Retrofit](https://github.com/square/retrofit). In the future, I will replace it with ![Ktor](https://github.com/ktorio/ktor) for KMP supporting.
6. ![Multiplatform paging](https://github.com/cashapp/multiplatform-paging). It has some problems on iOS, as it only supports UIKit, but not SwiftUI. It is also not very suitable for MVI and UDF architectures. I will think about how to solve these issues.
7. Based on Views and Fragments. I plan to rewrite the UI to ![Compose](https://developer.android.com/compose) in the future, and then to ![Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform) in the very distant future ^_^.
8. DI is implemented through ![Dagger 2](https://github.com/google/dagger), and injections are performed through the UI. I plan to use [Koin](https://github.com/InsertKoinIO/koin) in the future for KMP supporting.
9. ![Kotlin Tests](https://kotlinlang.org/api/core/kotlin-test) and ![Mockk](https://github.com/mockk/mockk) libraries are used for Unit testing of KMP modules.
