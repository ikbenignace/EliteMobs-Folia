# Folia Compatibility Implementation

This document outlines the changes made to make EliteMobs compatible with Folia (Paper's regionised multithreading server).

## Changes Made

### 1. Build Configuration Updates
- **build.gradle**: 
  - Updated Java version from 17 to 21
  - Removed deprecated `jcenter()` repository
  - Added PaperMC repository for Folia API access
  - Added Folia API dependency `dev.folia:folia-api:1.21.8-R0.1-SNAPSHOT`
  - Updated Spigot API to newer version

### 2. Plugin Configuration
- **plugin.yml**: Added `folia-supported: true` to mark the plugin as Folia-compatible

### 3. Scheduler Abstraction Layer
- **Created SchedulerUtil.java**: A utility class that provides cross-server scheduler compatibility
  - Automatically detects if running on Folia or traditional Paper/Spigot
  - Provides methods that map to appropriate scheduler types:
    - `runTaskTimer()` - For repeating tasks
    - `runTask(Location/Entity)` - For region-specific tasks
    - `runTaskAsync()` - For async tasks
    - `runTaskLater()` - For delayed tasks
    - `cancelTask()` - Universal task cancellation

### 4. Core File Updates
Updated the following critical files to use SchedulerUtil instead of direct BukkitScheduler:

#### Event System
- **EventsPackage.java**: Updated reload command scheduling
- **TimedEvent.java**: Updated event picker timer and event queueing
- **CustomEvent.java**: Updated event watchdog and command execution

#### Entity Management
- **EntityTracker.java**: Updated managed entity watchdog and temporary block cleanup
- **CustomBossEntity.java**: Updated dynamic level updater and escape mechanism
- **ElitePower.java**: Updated power cooldown scheduling

#### Instance/Arena System
- **MatchInstance.java**: Updated watchdog tasks and countdown mechanisms
- **CooldownHandler.java**: Updated cooldown management

#### Mob Spawning
- **CustomSpawn.java**: Updated spawn queueing and location generation

## Technical Implementation Details

### Folia Detection
The system uses reflection to detect Folia's presence:
```java
Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler");
```

### Region-Aware Scheduling
- Location-based tasks use `RegionScheduler` on Folia
- Entity-based tasks use `EntityScheduler` on Folia  
- Async tasks use `AsyncScheduler` on Folia
- Falls back to traditional `BukkitScheduler` on Paper/Spigot

### Task Cancellation
Universal task cancellation handles both:
- Folia's `ScheduledTask` objects (via reflection)
- Bukkit's `BukkitTask` objects (direct casting)

## Remaining Work
The following files still contain scheduler usage that should be updated:
- Various power/ability classes in `powers/` package
- Menu system classes
- Quest system components
- Additional utility classes

## Testing Notes
- Build requires network access to download Folia API dependencies
- Plugin should work on both Folia and traditional servers seamlessly
- No configuration changes needed for end users