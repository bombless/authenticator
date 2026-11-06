# Authenticator

An Android TOTP authenticator built with Kotlin and Jetpack Compose.

## Features

- Scan an `otpauth://totp` QR code with CameraX + Google ML Kit.
- Show the current one-time password directly in each account card, with a live countdown.
- Rename and delete saved entries; copy a code with one tap.
- Support SHA1, SHA256, SHA512 and 6–8 digit TOTP values.
- Encrypt the local account vault with AES-GCM using a key held by Android Keystore.
- No account, analytics, or network sync is implemented.

## Build

- JDK 17
- Android SDK 36
- Gradle 9.1.0 (version pinned in `gradle/wrapper/gradle-wrapper.properties`)

Open the project in Android Studio and sync Gradle, or run the Gradle `assembleDebug` task from a Gradle 9.1.0 installation.

## Notes

Only TOTP QR codes are accepted. The app requests camera permission only when the user starts scanning. Secrets are stored locally in encrypted preferences; Android backup is disabled to avoid exporting the vault without its Keystore key.
