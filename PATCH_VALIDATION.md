# Maktabati cumulative patch status

Implemented in this patch:
- Replaced invalid Python-like code in SampleBooksProvider.kt with Kotlin collection, UTF-8, and maxOf expressions.
- Expanded the sample book "همسُ المرايا" to six interactive pages with the requested CSS variables and template classes.
- Added the missing Compose `offset` import and repaired the floating pill controls (back, tools toggle, collapse, expand).
- Persisted `isDarkTheme` and `viewMode` using `maktabati_preferences`.
- Updated the core palette and removed the old `#451A03` color from BookCard.
- Added a compact library reading-statistics card.
- Added sample-book assertions to ExampleRobolectricTest.kt.

Validation:
- `./gradlew testDebugUnitTest` could not run because this archive does not contain `gradlew` or `gradle-wrapper.jar`.
- The environment has no installed Gradle and cannot resolve services.gradle.org, so Gradle could not be installed here.
- Kotlin compiler parser pass over project source/test files produced no syntax/expecting diagnostics. This is not a substitute for an Android Gradle build; unresolved Android/Compose dependencies remain without Gradle dependency resolution.
- The four reader sheets/dialogs have NOT yet been extracted from ReaderScreen.kt into ReaderSheetsAndDialogs.kt. This item remains open and the archive is not represented as fully validated or release-ready.
