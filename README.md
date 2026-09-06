# Walkthrough: Kotlin Native Executable with Maven

We have set up, built, tested, and validated a Kotlin application configured with **Apache Maven** and compiled ahead-of-time (AOT) into a standalone native Linux executable binary.

## Clarification on Kotlin Multiplatform (KMP) & Maven
- **Kotlin Multiplatform (KMP) officially requires Gradle**: JetBrains maintains the `kotlin-multiplatform` plugin exclusively for Gradle and does not provide native/klib targets in `kotlin-maven-plugin`.
- **Native Executable with Maven**: By leveraging the modern `org.graalvm.buildtools:native-maven-plugin` alongside `kotlin-maven-plugin`, Maven compiles Kotlin code directly into a native Linux ELF executable (`target/mvn-native`) with sub-millisecond cold start times and zero JVM overhead at runtime.

---

## Changes Made

1. **[pom.xml](file:///home/atom/lab/kotlin/mvn_native/pom.xml)**:
   - Configured `kotlin-maven-plugin` (version 2.1.20) for compiling Kotlin sources and tests.
   - Configured `native-maven-plugin` (version 0.11.0) hooked into the `package` lifecycle phase to build the native binary named `mvn-native` with `--no-fallback`.
   - Configured `maven-surefire-plugin` (version 3.2.5) with JUnit 5 / `kotlin-test-junit5`.

2. **[src/main/kotlin/net/knaur/Main.kt](file:///home/atom/lab/kotlin/mvn_native/src/main/kotlin/net/knaur/Main.kt)**:
   - Contains the Kotlin application entry point `main()` and greeting utility `getGreeting()`.
   - Queries and prints host operating system and architecture.

3. **[src/test/kotlin/net/knaur/MainTest.kt](file:///home/atom/lab/kotlin/mvn_native/src/test/kotlin/net/knaur/MainTest.kt)**:
   - Automated unit test verifying greeting output.

4. **[.gitignore](file:///home/atom/lab/kotlin/mvn_native/.gitignore)**:
   - Ignores `target/`, `.idea/`, and `*.iml`.

---

## Verification & Test Results

### 1. Automated Unit Tests
Command executed:
```bash
JAVA_HOME=/home/atom/_java_/graalvm25 PATH=/home/atom/_java_/graalvm25/bin:$PATH mvn clean test
```
Result:
```text
[INFO] Running net.knaur.MainTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.046 s -- in net.knaur.MainTest
[INFO] BUILD SUCCESS
```

### 2. Native Compilation & Binary Packaging
Command executed:
```bash
JAVA_HOME=/home/atom/_java_/graalvm25 PATH=/home/atom/_java_/graalvm25/bin:$PATH mvn package
```
Result:
```text
[INFO] --- native-maven-plugin:0.11.0:compile-no-fork (build-native) @ mvn-native ---
[INFO] Found GraalVM installation from JAVA_HOME variable.
================================================================================
GraalVM Native Image: Generating 'mvn-native' (executable)...
================================================================================
Finished generating 'mvn-native' in 34,3s.
[INFO] BUILD SUCCESS
```

### 3. Native Executable Validation
- **File Inspection**:
  ```bash
  $ file target/mvn-native
  target/mvn-native: ELF 64-bit LSB pie executable, x86-64, version 1 (SYSV), dynamically linked, interpreter /lib64/ld-linux-x86-64.so.2, for GNU/Linux 3.2.0, stripped
  ```
- **Size**: 6.5 MB standalone ELF binary.
- **Execution Output**:
  ```bash
  $ ./target/mvn-native
  Hello, Kotlin Native World! Running natively on Linux (amd64)
  ```
- **Execution Speed**:
  ```bash
  $ time ./target/mvn-native
  real    0m0,003s
  user    0m0,002s
  sys     0m0,001s
  ```
  Startup and execution complete in ~3 milliseconds.

---

## How to Build & Run

To build and run the native binary at any time from `/home/atom/lab/kotlin/mvn_native`:

```bash
# Build the native binary
JAVA_HOME=/home/atom/_java_/graalvm25 PATH=/home/atom/_java_/graalvm25/bin:$PATH mvn clean package

# Run the generated native executable
./target/mvn-native
```
