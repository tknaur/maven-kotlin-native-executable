# Kotlin Native Executable with Apache Maven

This project demonstrates how to build and compile a **Kotlin** application into a standalone **native Linux executable** (ELF 64-bit) using **Apache Maven** and **GraalVM Native Image**.

---

## 📌 Project Overview & Context

### Kotlin Multiplatform (KMP) vs. Maven
* **Official JetBrains KMP Tooling**: JetBrains officially maintains Kotlin Multiplatform (KMP) and Kotlin/Native exclusively for **Gradle** (`org.jetbrains.kotlin.multiplatform`). The official `kotlin-maven-plugin` only targets JVM bytecode and JavaScript, without native/KLIB compiler goals.
* **Native Binaries via Maven**: By combining `kotlin-maven-plugin` with GraalVM's `native-maven-plugin`, you achieve a pure, standard Maven workflow (`pom.xml`) that compiles Kotlin code ahead-of-time (AOT) into a standalone native binary without requiring a JVM at runtime.

---

## 🚀 Key Highlights

* **Pure Maven Build**: Standard Maven lifecycle commands (`mvn clean test`, `mvn package`).
* **Instant Cold Start**: Sub-millisecond to ~3 ms execution time.
* **Low Memory Footprint**: Minimal runtime RSS (~15 MB) using Serial GC and bounded heap.
* **No Runtime Dependencies**: Generates a self-contained 6.5 MB ELF 64-bit binary in `target/mvn-native`.
* **Automated Testing**: Kotlin unit tests running via Maven Surefire and JUnit 5.
* **Production-Tuned Profiles**: Built-in `-Poptimized` profile enabling host CPU vectorization (`-march=native`), heap bounds (`-R:MaxHeapSize=64m`), and future-proof GraalVM defaults (`--future-defaults=all`).

---

## 📂 Project Structure

```text
mvn_native/
├── pom.xml                   # Maven build descriptor (Kotlin + GraalVM plugins)
├── OPTIMIZATIONS.md          # Guide on GraalVM compiler recommendations & tuning
├── README.md                 # Project documentation and summary
├── .gitignore                # Git exclusions (target/, IDE files)
└── src/
    ├── main/
    │   └── kotlin/
    │       └── net/knaur/
    │           └── Main.kt   # Application entry point & greeting logic
    └── test/
        └── kotlin/
            └── net/knaur/
                └── MainTest.kt # JUnit 5 unit test suite
```

---

## 🛠️ Prerequisites

* **Java / GraalVM**: Oracle GraalVM 25 or GraalVM JDK 21+ with `native-image` installed.
* **Maven**: Apache Maven 3.8.0 or newer.
* **C Toolchain**: Standard Linux build tools (`gcc`, `glibc-devel`, `zlib`).

Set your `JAVA_HOME` to your GraalVM installation:
```bash
export JAVA_HOME=/home/atom/_java_/graalvm25
export PATH=$JAVA_HOME/bin:$PATH
```

---

## ⚡ Quick Start & Commands

### 1. Run Automated Unit Tests
Compiles Kotlin source and test files and executes JUnit 5 tests via Maven Surefire:
```bash
mvn clean test
```

### 2. Build Standard Native Executable
Compiles Kotlin to bytecode and runs GraalVM AOT compilation to produce `target/mvn-native`:
```bash
mvn clean package
```

### 3. Build Optimized Native Executable
Applies production optimizations (`-march=native`, `-R:MaxHeapSize=64m`, `--future-defaults=all`):
```bash
mvn clean package -Poptimized
```

### 4. Run the Native Executable
```bash
./target/mvn-native
```
**Output:**
```text
Hello, Kotlin Native World! Running natively on Linux (amd64)
```

---

## 📊 Verification & Benchmarks

| Metric | Measurement | Notes |
| :--- | :--- | :--- |
| **Binary Format** | ELF 64-bit LSB pie executable | Stripped, dynamically linked |
| **Binary Size** | ~6.5 MB | Standalone executable |
| **Execution Time** | **~3 ms** (`0.003s`) | Instant startup compared to JVM warmup |
| **Memory Limit** | 64 MB | Configured via `-R:MaxHeapSize=64m` |

---

## ⚙️ Advanced Compiler Tuning & Recommendations

During native image compilation, GraalVM provides compiler recommendations:
* **G1GC (`--gc=G1`)**: Multi-threaded GC for high-throughput, high-allocation services.
* **PGO (`--pgo`)**: Profile-Guided Optimization for up to 30% higher throughput.
* **FUTR (`--future-defaults=all`)**: Early adoption of upcoming compiler standards.
* **HEAP (`-R:MaxHeapSize=<size>`)**: Hard memory limits for container/CLI environments.
* **CPU (`-march=native`)**: Vectorized machine code tailored to host CPU features.

For detailed explanations, trade-offs, and step-by-step PGO instructions, see **[OPTIMIZATIONS.md](OPTIMIZATIONS.md)**.
