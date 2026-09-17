A dialog asking the user to grant (or open settings for) the notifications permission after an
earlier direct request was denied.

## Entities

- [NotificationsRationaleDialog](src/commonMain/kotlin/com/alekseivinogradov/anoti/notificationsrationaledialog/kmp/impl/presentation/compose/NotificationsRationaleDialog.kt) —
  the dialog.

## How to include it

- Gradle: `implementation(project(":feature-kmp:notifications-rationale-dialog"))`
- No DI wiring: the host decides when to show it and calls it directly from its own Compose
  content.

## How to use it

```kotlin
if (state.visible.value) {
    NotificationsRationaleDialog(onDismiss = state.onDismiss, onApprove = state.onApprove)
}
```
