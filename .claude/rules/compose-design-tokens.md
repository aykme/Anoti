---
paths:
  - "**/src/*Main/**/presentation/**/*.kt"
  - "**/*{Dimens,Fonts,Colors,Const,Consts}.kt"
---

# Compose design tokens (Dimens/Fonts/Colors/Const)

- Shared Compose UI constants live in typed files by kind: `Dimens.kt` (sizes, spacing,
  corner/alpha percentages — `Dp`/`Int`), `Fonts.kt` (text sizes — `TextUnit`), `Colors.kt` (the
  color palette). `Const.kt` is separate and holds only business-logic constants (paging, timing,
  domain limits), never UI values. The one exception is the log tag `ANOTI_TAG`, kept in
  `CelebrityConsts.kt` of `core-kmp:celebrity`.
- Placement: a constant used by more than one module lives in the closest common dependency every
  consumer already has (e.g. `core-kmp:celebrity` for project-wide values, `feature-kmp:anime-base`
  for values shared only among the anime feature screens that already depend on it). A constant
  used by exactly one module lives in a local file of the same kind in that module's own package.
  Don't leave it as a bare `private const val` at the bottom of a UI/Composable file.
- When more than one module has its own local `Dimens.kt`/`Fonts.kt`, prefix the file name with
  the module name (e.g. `AnimeListDimens.kt`, `AnimeFavoritesDimens.kt`). An import or search then
  says unambiguously which one it means.
- Comments on these constants describe what the value represents, not which features or screens
  consume it. That list changes independently of the value and shouldn't be hardcoded.
