A dialog asking the user to grant (or open settings for) the notifications permission after an
earlier direct request was denied.

## Entities

- [NotificationsRationaleDialog](src/commonMain/kotlin/com/alekseivinogradov/anoti/notificationsrationaledialog/kmp/api/presentation/compose/NotificationsRationaleDialog.kt) —
  the dialog.

## How to include it

- Gradle: `implementation(project(":feature-kmp:notifications-rationale-dialog"))`
- No DI wiring: the host decides when to show it and calls it directly from its own Compose
  content.

## How to use it

```kotlin
// Android example (no iOS example yet):
if (notificationsRationaleVisible.value) {
    NotificationsRationaleDialog(
        onDismiss = { notificationsRationaleVisible.value = false },
        onApprove = {
            notificationsRationaleVisible.value = false
            onNotificationRequestApproved()
        }
    )
}
```
