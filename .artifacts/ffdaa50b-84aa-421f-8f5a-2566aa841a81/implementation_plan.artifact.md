# Implementation Plan - Quiz Questions Data Improvement

The goal is to clean up, repair, and optimize the `android_questions.json` file which contains the core quiz data. Currently, the file is structurally broken (corrupted) and contains several data quality issues such as typos, trailing spaces, and redundant data structures.

## User Review Required

> [!IMPORTANT]
> The `android_questions.json` file is currently **invalid JSON** because it contains two concatenated JSON arrays. This plan will merge them into a single valid array.

> [!WARNING]
> I propose changing the way the correct answer is stored from a string to an index. This will require updates to the `QuizQuestion` data class and several UI/Logic files. This is more robust but involves code changes beyond just the JSON file.
> **Decision needed**: Should we proceed with the data model change (string -> index) or just clean up the JSON content?

## Proposed Changes

### [Quiz Data]

#### [MODIFY] [android_questions.json](file:///C:/Users/andre/AndroidStudioProjects/QuizSoccorso/app/src/main/assets/android_questions.json)
- **Repair Structure**: Merge the two JSON blocks into one valid array.
- **Deduplication**: Ensure each question (by ID) appears only once.
- **Content Cleanup**:
    - Fix typos (e.g., `reanimazione` -> `rianimazione`, `paziende` -> `paziente`, `Quel è` -> `Qual è`).
    - Remove trailing spaces from answers and questions.
    - Standardize Unicode characters (e.g., replace `\u0027` with `'`).
- **Formatting**: Apply consistent indentation (4 spaces) for better maintainability.
- **Enrichment**: Add missing `explanation` text for basic questions (e.g., explaining why 112 is the NUE).
- **Placeholders**: Remove "TEST 1 AUTISTI" and "TEST 2 AUTISTI" or replace them with actual content.

### [Data Model & Logic] (Optional based on user feedback)

#### [MODIFY] [QuizQuestion.kt](file:///C:/Users/andre/AndroidStudioProjects/QuizSoccorso/app/src/main/java/com/example/quizsoccorso/QuizQuestion.kt)
- Change `val correct: String` to `val correctIndex: Int`.

#### [MODIFY] [QuizViewModel.kt](file:///C:/Users/andre/AndroidStudioProjects/QuizSoccorso/app/src/main/java/com/example/quizsoccorso/QuizViewModel.kt) and others
- Update logic to compare against index instead of string.

## Verification Plan

### Automated Tests
- Validate JSON syntax using a shell command or by running the app (it should not crash on load).
- Run existing unit tests if any (to be searched).

### Manual Verification
- Deploy the app and navigate to the Quiz screen.
- Verify that questions are displayed correctly and that the "correct" answer logic still works.
- Check the "Admin" or "Stats" screens to ensure no regression in how data is processed.
