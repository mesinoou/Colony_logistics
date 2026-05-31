# Phase 16.3 - Gradle settings fix

This patch removes `dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) }` from `settings.gradle`.

NeoForge ModDevGradle adds Minecraft/NeoForge repositories during plugin application. When Gradle is configured to prefer settings repositories over project repositories, ModDevGradle fails while adding `Mojang Minecraft Libraries`, even during `gradle wrapper`.

After applying this patch, run:

```powershell
gradle wrapper --gradle-version 8.10.2
.\gradlew.bat compileJava
```

On Linux/macOS:

```bash
gradle wrapper --gradle-version 8.10.2
./gradlew compileJava
```
