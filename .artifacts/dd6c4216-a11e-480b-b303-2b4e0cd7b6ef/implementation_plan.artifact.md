# Implementation Plan - WCAG Contrast Audit and Theme Refinement

This plan addresses readability issues across all themes by auditing color contrast against WCAG AA guidelines and refining the "Lively" themes to be both friendly and highly accessible.

## User Review Required

> [!IMPORTANT]
> Some colors will be shifted slightly darker or more saturated to ensure they are readable on their respective backgrounds. The "Lively" feel will be maintained through warmer tones, not just neon brightness.

## Proposed Changes

### [Component] Settings UI
#### [MODIFY] [SettingsScreen.kt](file:///C:/Users/andre/AndroidStudioProjects/QuizSoccorso/app/src/main/java/com/example/quizsoccorso/SettingsScreen.kt)
- Rename "Scuro" option to "Scuro (semplice)".

### [Component] Theme & Colors
#### [MODIFY] [Color.kt](file:///C:/Users/andre/AndroidStudioProjects/QuizSoccorso/app/src/main/java/com/example/quizsoccorso/ui/theme/Color.kt)
- **Lively Coral**: Shift to a deeper, warmer Terracotta (`#C0392B` or `#B33F40`) to achieve 4.5:1 contrast on the cream background.
- **Lively Sage**: Darken to a forest green shade for text usage.
- **Accessible Orange**: Shift to a deeper burnt orange (`#996A00`) to meet AA standards on white.
- **Reading (Sepia)**: Ensure `SepiaText` remains dark enough against the cream background.
- **Lively Dark**: Ensure primary neon colors are bright enough on the charcoal background (7:1 for accessibility).

#### [MODIFY] [Theme.kt](file:///C:/Users/andre/AndroidStudioProjects/QuizSoccorso/app/src/main/java/com/example/quizsoccorso/ui/theme/Theme.kt)
- Explicitly set `onBackground` and `onSurface` for all themes to ensure they meet the 4.5:1 contrast ratio against their respective backgrounds.
- Refine `LivelyLightColorScheme` and `LivelyDarkColorScheme` to balance "friendliness" with high readability.

### [Component] Stats UI
#### [MODIFY] [StatsScreen.kt](file:///C:/Users/andre/AndroidStudioProjects/QuizSoccorso/app/src/main/java/com/example/quizsoccorso/StatsScreen.kt)
- Verify that the new progress bar and icons in the Preparation Index card use the high-contrast `indicatorColor` consistently.

## Verification Plan

### Automated Tests
- N/A

### Manual Verification
1.  **Contrast Check**: Use a contrast checker tool (or simulate in Android Studio) to verify all text-to-background ratios in:
    - **Lively Light**: Text on Cream.
    - **Lively Dark**: Text on Charcoal.
    - **Accessible**: Blue/Orange on White.
2.  **Naming Check**: Verify "Scuro (semplice)" appears in Settings.
3.  **Overall Feel**: Ensure "Lively" themes still feel friendly and non-aseptic while being more readable.
