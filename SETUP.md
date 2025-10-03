# Setup Instructions

## Prerequisites

1. Android Studio Arctic Fox or later

## Initial Setup

1. Clone the repository
2. For release builds, copy `gradle.properties.template` to `gradle.properties`
3. Edit `gradle.properties` and add your keystore credentials for release builds

## Important Security Notes

- **Never commit `gradle.properties` with real keystore credentials**
- **Never commit keystore files to public repositories**
- **Use environment variables for CI/CD pipelines**
- The `.gitignore` file is configured to protect sensitive files

## Building the App

1. Open the project in Android Studio
2. Sync gradle files
3. Build and run

## Environment Variables (Recommended for CI/CD)

For production builds, use environment variables:

- `KEYSTORE_PASSWORD`: Your keystore password
- `KEY_ALIAS`: Your key alias
- `KEY_PASSWORD`: Your key password
