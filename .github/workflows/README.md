# GitHub Workflows

## Build and Upload Artifact

This workflow automatically builds the EliteMobs plugin and uploads the resulting JAR files as artifacts.

### Triggers
- **Push to master/main branch**: Builds and uploads artifacts for releases and main development
- **Pull requests to master/main**: Builds and uploads artifacts for testing proposed changes

### What it does
1. Sets up Java 17 environment
2. Caches Gradle dependencies for faster builds
3. Runs `./gradlew clean build shadowJar` to build the plugin
4. Uploads two artifact sets:
   - `EliteMobs-{event}-{run_number}`: All build artifacts (30-day retention)
   - `EliteMobs-Plugin`: Main plugin JAR file (90-day retention)

### Artifacts
- **Main Plugin**: `testbed/plugins/EliteMobs.jar` - The primary plugin file ready for server deployment
- **Build Artifacts**: `build/libs/*.jar` - All generated JAR files including development versions

### Downloading Artifacts
1. Go to the [Actions tab](../../actions) in the repository
2. Click on a completed workflow run
3. Scroll down to the "Artifacts" section
4. Download the desired artifact ZIP file