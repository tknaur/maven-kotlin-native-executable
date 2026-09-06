# GraalVM Native Image Optimization Guide

During the GraalVM native image build, the compiler provides the following recommendations:

```text
Recommendations:
 G1GC: Use the G1 GC ('--gc=G1') for improved latency and throughput.
 PGO:  Use Profile-Guided Optimizations ('--pgo') for improved throughput.
 FUTR: Use '--future-defaults=all' to prepare for future releases.
 HEAP: Set max heap for improved and more predictable memory usage.
 CPU:  Enable more CPU features with '-march=native' for improved performance.
```

This guide explains what each recommendation does, when to use it, associated trade-offs, and how to configure them in Maven (`pom.xml`).

---

## 1. Summary of Recommendations

| Recommendation | Build Flag | Target Use Case | Trade-Off / Caveat |
| :--- | :--- | :--- | :--- |
| **G1GC** | `--gc=G1` | Long-running services, microservices with large heaps (>500MB) | Adds ~2–4MB binary size; increases baseline memory footprint. Not recommended for short-lived CLI tools. |
| **PGO** | `--pgo` | Peak throughput for high-traffic or compute-heavy applications | Requires a 2-stage build workflow (instrument $\to$ profile $\to$ compile). |
| **FUTR** | `--future-defaults=all` | Future-proofing builds against upcoming GraalVM releases | None. Encouraged best practice. |
| **HEAP** | `-R:MaxHeapSize=<size>` | Predictable memory limits (Docker/K8s/CLI) | Prevents runaway memory growth; can still be overridden at runtime with `-Xmx`. |
| **CPU** | `-march=native` | Highest execution speed on the host machine | Binary becomes host-specific; may crash (`SIGILL`) if run on older CPU architectures. |

---

## 2. Detailed Breakdown & Configuration

### 2.1. G1 Garbage Collector (`--gc=G1`)
- **Default behavior**: GraalVM Native Image defaults to the single-threaded **Serial GC**, which is optimal for small utilities, CLI commands, and microVMs where low startup memory is critical.
- **When to use**: Web servers (e.g. Ktor, Quarkus), background batch processors, or services holding larger in-memory states with high object allocation rates across multiple CPU threads.
- **Maven configuration**:
  ```xml
  <buildArg>--gc=G1</buildArg>
  ```
- *Note: G1 GC is included in Oracle GraalVM.*

---

### 2.2. Profile-Guided Optimization (`--pgo`)
PGO allows GraalVM's AOT compiler to optimize machine code using runtime profiling information (similar to JIT profiling).

#### Step-by-Step Workflow:
1. **Build the instrumented binary**:
   ```bash
   JAVA_HOME=/home/atom/_java_/graalvm25 PATH=/home/atom/_java_/graalvm25/bin:$PATH mvn clean package -Poptimized,pgo-instrument
   ```

2. **Run the instrumented binary under realistic workload**:
   ```bash
   ./target/mvn-native
   # Send traffic:
   curl http://localhost:7070/
   curl http://localhost:7070/greeting
   # Stop server (Ctrl+C or kill)
   ```
   This generates a profile dump: `default.iprof`.

3. **Rebuild the final binary with the gathered profile**:
   ```bash
   JAVA_HOME=/home/atom/_java_/graalvm25 PATH=/home/atom/_java_/graalvm25/bin:$PATH mvn clean package -Poptimized,pgo
   ```

- **Benefits**: Up to 20–40% higher peak throughput, better inlining of hot methods, and reduced runtime memory.
- **Dedicated Guide**: See **[PGO.md](PGO.md)** for a full lifecycle diagram, verification steps, and automated build script.

---

### 2.3. Future Defaults (`--future-defaults=all`)
- **What it does**: GraalVM introduces stricter checks, security defaults, and newer compiler flags gradually. Passing `--future-defaults=all` opts into all upcoming defaults early.
- **Maven configuration**:
  ```xml
  <buildArg>--future-defaults=all</buildArg>
  ```

---

### 2.4. Max Heap Size Limit (`-R:MaxHeapSize=<size>`)
- **Default behavior**: Without this flag, GraalVM native executables dynamically compute max heap based on host RAM (often defaulting up to 80% of system memory).
- **When to use**: Essential when deploying in containers (Docker/Kubernetes) or writing CLI tools to avoid unbounded memory usage.
- **Maven configuration**:
  ```xml
  <!-- Set max heap to 64MB at build time -->
  <buildArg>-R:MaxHeapSize=64m</buildArg>
  ```
- **Runtime override**: The compiled binary can always be overridden when executed:
  ```bash
  ./target/mvn-native -Xmx128m
  ```

---

### 2.5. Host CPU Instruction Vectorization (`-march=native`)
- **Default behavior**: GraalVM targets a conservative baseline (`x86-64-v3` or generic x86_64) to ensure the binary runs across different machines.
- **What `-march=native` does**: Generates machine code utilizing all features of the CPU on which the binary is compiled (AVX2, AVX-512, FMA, BMI1/2, AES, etc.).
- **Maven configuration**:
  ```xml
  <buildArg>-march=native</buildArg>
  ```
- **Warning**: Do not use `-march=native` if you plan to distribute this binary to other machines with older or different CPU models.

---

## 3. Recommended `pom.xml` Setup

### Option A: Recommended for CLI Applications
Edit the `native-maven-plugin` configuration in `pom.xml`:

```xml
<plugin>
    <groupId>org.graalvm.buildtools</groupId>
    <artifactId>native-maven-plugin</artifactId>
    <version>${graalvm.buildtools.version}</version>
    <configuration>
        <imageName>mvn-native</imageName>
        <mainClass>${main.class}</mainClass>
        <buildArgs>
            <buildArg>--no-fallback</buildArg>
            <buildArg>--future-defaults=all</buildArg>
            <buildArg>-march=native</buildArg>
            <buildArg>-R:MaxHeapSize=64m</buildArg>
        </buildArgs>
    </configuration>
    <executions>
        <execution>
            <id>build-native</id>
            <goals>
                <goal>compile-no-fork</goal>
            </goals>
            <phase>package</phase>
        </execution>
    </executions>
</plugin>
```

---

### Option B: Modular Maven Profile (Build Fast vs. Build Optimized)
Add a dedicated `<profile>` to your `pom.xml` so you can compile quickly during development, and apply all optimizations for release:

```xml
<profiles>
    <!-- Standard build: mvn package -->
    <!-- Optimized build: mvn package -Poptimized -->
    <profile>
        <id>optimized</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.graalvm.buildtools</groupId>
                    <artifactId>native-maven-plugin</artifactId>
                    <configuration>
                        <buildArgs combine.children="append">
                            <buildArg>--future-defaults=all</buildArg>
                            <buildArg>-march=native</buildArg>
                            <buildArg>-R:MaxHeapSize=128m</buildArg>
                            <!-- Uncomment for high-throughput server workloads: -->
                            <!-- <buildArg>--gc=G1</buildArg> -->
                        </buildArgs>
                    </configuration>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

Build command with the profile:
```bash
JAVA_HOME=/home/atom/_java_/graalvm25 PATH=/home/atom/_java_/graalvm25/bin:$PATH mvn clean package -Poptimized
```
