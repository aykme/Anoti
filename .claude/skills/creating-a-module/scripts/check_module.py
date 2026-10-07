"""Checks a Gradle module against the shape .claude/design/new-module.md prescribes.

Usage: python -I .claude/skills/creating-a-module/scripts/check_module.py :<group>:<name> <kind>
<kind> is one of KINDS below, as new-module.md "Choose the kind" names it.
Prints one line per finding, FAIL or WARN, and exits non-zero on any FAIL.
"""
import os
import re
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), *[".."] * 4))
PLATFORMS = {"commonMain": "kmp", "commonTest": "kmp", "androidMain": "android",
             "androidHostTest": "android", "androidDeviceTest": "android", "iosMain": "ios",
             "iosTest": "ios"}
REQUIRED_PLUGINS = ["kotlinMultiplatform", "androidKotlinMultiplatformLibrary", "detekt"]
KSP_TARGETS = ["Android", "IosArm64", "IosSimulatorArm64"]
COMMON = "src/commonMain/kotlin/**/kmp/"
STORE = [("the main store contract", COMMON + "api/domain/store/**/*Store.kt"),
         ("the executor", COMMON + "impl/domain/store/**/*ExecutorImpl.kt"),
         ("the reducer", COMMON + "impl/domain/store/**/*ReducerImpl.kt"),
         ("the store factory", COMMON + "impl/domain/store/**/*StoreFactory.kt"),
         ("the controller", COMMON + "impl/presentation/*Controller.kt")]
# Kind -> what its module must hold (label, glob relative to the module directory). The entities
# and their places are in .claude/design/entities.md and module-anatomy.md.
KINDS = {
    "screen-feature": STORE + [
        ("the dependencies contract", COMMON + "api/di/Di*Dependencies.kt"),
        ("the feature graph", COMMON + "impl/di/Di*Component.kt"),
        ("the screen component", COMMON + "impl/presentation/navigation/Nav*ScreenComponent.kt"),
        ("the route", COMMON + "impl/presentation/navigation/*Route.kt"),
        ("the screen", COMMON + "impl/presentation/compose/*Screen.kt"),
        ("the state-to-UI mapper", COMMON + "api/presentation/mapper/StateToUiModelMapper.kt"),
        ("the UI model", COMMON + "api/presentation/model/*UiModel.kt")],
    "root-level-element": STORE + [
        ("the module component", COMMON + "impl/di/Di*Component.kt"),
        ("the composable", COMMON + "impl/presentation/compose/*.kt")],
    "ui-only-component": [("the composable", COMMON + "impl/presentation/compose/*.kt")],
    "shared-base": [],
    "platform-service": [
        ("the common contract", COMMON + "api/**/*.kt"),
        ("the Android platform component", "src/androidMain/kotlin/**/Di*PlatformComponent.kt"),
        ("the iOS platform component", "src/iosMain/kotlin/**/Di*PlatformComponent.kt")],
    "external": [("a contract", "src/commonMain/kotlin/**/kmp/api/**/*.kt")],
    "core": [],
}
findings = []


def fail(message):
    findings.append(("FAIL", message))


def warn(message):
    findings.append(("WARN", message))


def text(path):
    with open(path, encoding="utf-8") as handle:
        return handle.read()


def root_package():
    """The package every module's namespace starts with, read from the navigation module."""
    match = re.search(r'namespace = "([\w.]+)\.navigation\.kmp"',
                      text(os.path.join(ROOT, "core-kmp", "navigation", "build.gradle.kts")))
    return match.group(1)


def kotlin_files(module_dir):
    for directory, _, names in os.walk(os.path.join(module_dir, "src")):
        for name in names:
            if name.endswith(".kt"):
                yield os.path.join(directory, name)


def check_settings(gradle_path, group):
    lines = text(os.path.join(ROOT, "settings.gradle.kts")).splitlines()
    includes = [i for i, line in enumerate(lines) if line.startswith("include(")]
    mine = [i for i in includes if lines[i] == f'include("{gradle_path}")']
    if len(mine) != 1:
        fail(f'settings.gradle.kts needs exactly one line include("{gradle_path}")')
        return
    group_lines = [i for i in includes if lines[i].startswith(f'include(":{group}:')]
    if group_lines and not (min(group_lines) <= mine[0] <= max(group_lines)):
        fail(f"the include line sits outside the :{group} group")


def check_build(module_dir, module_id, package, kotlin):
    path = os.path.join(module_dir, "build.gradle.kts")
    if not os.path.exists(path):
        fail("no build.gradle.kts")
        return
    build = text(path)
    for plugin in REQUIRED_PLUGINS:
        if f"libs.plugins.{plugin})" not in build:
            fail(f"plugin {plugin} is missing; start from skeleton (a)")
    expected = f'namespace = "{package}.{module_id}.kmp"'
    if expected not in build:
        fail(f"the namespace is not {expected.split(' = ')[1]}")
    for line in ("compileSdk = libs.versions.compileSdk.get().toInt()",
                 "minSdk = libs.versions.minSdk.get().toInt()", "iosArm64()",
                 "iosSimulatorArm64()"):
        if line not in build:
            fail(f"`{line}` is missing; start from skeleton (a)")
    if re.search(r"^\s*repositories\s*\{", build, re.M):
        fail("the build file declares repositories; settings.gradle.kts does that once")
    lines = build.splitlines()
    for number, line in enumerate(lines):
        if re.match(r"\s*api\(", line):
            above = number - 1
            while above >= 0 and re.match(r"\s*api\(", lines[above]):
                above -= 1
            if above < 0 or not lines[above].strip().startswith("//"):
                fail(f"build.gradle.kts:{number + 1} `{line.strip()}` has no comment naming "
                     "the signature that needs it")
    components = [f for f in kotlin if re.search(r"^@Component\b", text(f), re.M)]
    has_ksp = "libs.plugins.ksp)" in build
    if components and not has_ksp:
        fail("a @Component lives here but KSP is not applied")
    if has_ksp:
        for target in KSP_TARGETS:
            if f'"{target}"' not in build and f'"ksp{target}"' not in build:
                fail(f"KSP is missing for the {target} target")
        if not components and not re.search(r"\broom\b", build, re.I):
            fail("KSP is applied but no @Component or Room lives here")
    has_resources = os.path.isdir(os.path.join(module_dir, "src", "commonMain", "composeResources"))
    enables_resources = re.search(r"androidResources\s*\{\s*enable\s*=\s*true", build)
    if has_resources != bool(enables_resources):
        fail("androidResources must be enabled exactly when src/commonMain/composeResources exists")


def check_sources(module_dir, module_id, package, kotlin):
    src = os.path.join(module_dir, "src")
    if not os.path.isdir(src):
        fail("no src directory")
        return
    for source_set in sorted(os.listdir(src)):
        if source_set not in PLATFORMS:
            fail(f"src/{source_set} is not a source set of a KMP module")
        elif os.path.isdir(os.path.join(src, source_set, "java")):
            fail(f"src/{source_set}/java: source directories are kotlin, never java")
    if not os.path.isdir(os.path.join(src, "commonMain")):
        fail("no src/commonMain: commonMain is the default home of code")
    if not any(s.endswith("Test") for s in os.listdir(src)):
        warn("no test source set yet")
    module_package = package + "." + module_id
    for path in kotlin:
        rel = os.path.relpath(path, module_dir).replace("\\", "/")
        source_set = rel.split("/")[1]
        declared = re.search(r"^package ([\w.]+)", text(path), re.M)
        if not declared:
            fail(f"{rel} has no package declaration")
            continue
        name = declared.group(1)
        on_disk = "/".join(rel.split("/")[3:-1]).replace("/", ".")
        if name != on_disk:
            fail(f"{rel}: package {name} does not match its directory")
        platform = PLATFORMS.get(source_set)
        # A test takes the package of the code it tests, which may be common code.
        allowed = {platform, "kmp"} if source_set.endswith("Test") else {platform}
        prefixes = [f"{module_package}.{p}." for p in sorted(allowed) if p]
        matched = next((p for p in prefixes if name.startswith(p)), None)
        if platform and not matched:
            fail(f"{rel}: package must start with {' or '.join(prefixes)}")
        elif platform:
            visibility = name[len(matched):].split(".")[0]
            if visibility not in ("api", "impl"):
                fail(f"{rel}: the segment after .{matched.split('.')[-2]} must be api or impl")
        stem = os.path.basename(path)[:-3]
        in_fake = name.split(".")[-1] == "fake"
        if in_fake and not stem.endswith(("Fake", "FakeTest")):
            fail(f"{rel}: a file in a fake package is a double named *Fake")
        if re.search(r"^(?:internal |private )?(?:class|object) Fake\w*", text(path), re.M):
            fail(f"{rel}: Fake goes at the end of a name, never at the front")
        if stem.endswith("Fake") and not in_fake and "/src/commonTest/" not in "/" + rel:
            warn(f"{rel}: a shared double lives in a package ending .fake")


def check_docs(module_dir, gradle_path):
    stem = gradle_path.lstrip(":").replace(":", "-").upper()
    for suffix in ("README", "REGRESS"):
        if not os.path.exists(os.path.join(module_dir, f"{stem}-{suffix}.md")):
            fail(f"{stem}-{suffix}.md is missing at the module root (code-documentation skill)")
    full = text(os.path.join(ROOT, "ANOTI-FULL-REGRESS.md"))
    if f"{stem}-REGRESS.md" not in full:
        fail(f"ANOTI-FULL-REGRESS.md does not link {stem}-REGRESS.md")


def glob_regex(pattern):
    out, i = "", 0
    while i < len(pattern):
        if pattern.startswith("**/", i):
            out, i = out + "(?:.*/)?", i + 3
        elif pattern[i] == "*":
            out, i = out + "[^/]*", i + 1
        else:
            out, i = out + re.escape(pattern[i]), i + 1
    return re.compile(out + "$")


def module_files(module_dir):
    for directory, _, names in os.walk(os.path.join(module_dir, "src")):
        for name in names:
            yield os.path.relpath(os.path.join(directory, name), module_dir).replace("\\", "/")


def check_kind(module_dir, kind, gradle_path):
    files = list(module_files(module_dir))
    for label, pattern in KINDS[kind]:
        regex = glob_regex(pattern)
        if not any(regex.match(f) for f in files):
            fail(f"{kind}: {label} is missing ({pattern})")
    build = text(os.path.join(module_dir, "build.gradle.kts"))
    stores = [f for f in files if "/domain/store/" in f]
    screens = [f for f in files if re.search(r"/Nav\w+ScreenComponent\.kt$", f)]
    if kind == "screen-feature":
        configs = text(os.path.join(ROOT, "core-kmp", "navigation", "src", "commonMain",
                                    "kotlin", *root_package().split("."), "navigation", "kmp",
                                    "NavRootConfig.kt"))
        for screen in screens:
            feature = re.search(r"/Nav(\w+)ScreenComponent\.kt$", screen).group(1)
            if f'@SerialName("{feature}")' not in configs \
                    or f"data object {feature} " not in configs:
                fail(f"no root config `{feature}` with @SerialName(\"{feature}\") "
                     "(navigation.md \"Adding a destination\")")
        main_build = text(os.path.join(ROOT, "main", "build.gradle.kts"))
        if f'project("{gradle_path}")' not in main_build:
            fail("the entry module does not depend on the screen feature")
    if kind == "root-level-element" and screens:
        fail("a root-level element has no screen component")
    if kind == "ui-only-component":
        if stores:
            fail("a UI-only component holds no store; its host keeps its state")
        if "libs.plugins.ksp)" in build or "kotlinSerialization" in build:
            fail("a UI-only component applies neither KSP nor serialization (skeleton (d))")
    if kind == "platform-service":
        contracts = {os.path.basename(f)[:-3] for f in files
                     if re.match(r"src/commonMain/kotlin/.*/kmp/api/.*\.kt$", f)}
        twins = {}
        for f in files:
            match = re.match(r"src/(androidMain|iosMain)/kotlin/.*/impl/.*/(\w+)Impl\.kt$", f)
            if match and match.group(2) in contracts:
                twins.setdefault(match.group(2) + "Impl", set()).add(match.group(1))
        if not twins:
            fail("platform-service: no platform implementation (*Impl) in androidMain or iosMain")
        for name, sets in sorted(twins.items()):
            if sets != {"androidMain", "iosMain"}:
                missing = ({"androidMain", "iosMain"} - sets).pop()
                fail(f"{name} has no twin of the same name in {missing} "
                     "(platform-mirroring.md \"Twins\")")
    if kind == "external" and any(re.match(r"src/commonMain/.*/impl/", f) for f in files):
        fail("an external module holds contracts only; the entry module implements them")


def check_wiring(gradle_path):
    for directory, _, names in os.walk(ROOT):
        if any(part in directory for part in (os.sep + ".", os.sep + "build")):
            continue
        if "build.gradle.kts" in names:
            path = os.path.join(directory, "build.gradle.kts")
            if f'project("{gradle_path}")' in text(path) and \
                    os.path.dirname(path) != os.path.join(ROOT, *gradle_path.strip(":").split(":")):
                return
    warn("no other module depends on it yet (new-module.md \"Wiring\")")


def main():
    if len(sys.argv) != 3 or sys.argv[2] not in KINDS \
            or not re.fullmatch(r":(core-kmp|feature-kmp):[a-z0-9-]+", sys.argv[1]):
        sys.exit("usage: check_module.py :core-kmp:<name>|:feature-kmp:<name> "
                 + "|".join(KINDS))
    gradle_path, kind = sys.argv[1], sys.argv[2]
    _, group, name = gradle_path.split(":")
    module_dir = os.path.join(ROOT, group, name)
    if not os.path.isdir(module_dir):
        sys.exit(f"no module directory {group}/{name}")
    module_id = name.replace("-", "")
    if module_id.endswith("external"):
        module_id = module_id[: -len("external")] + ".external"
    package = root_package()
    kotlin = list(kotlin_files(module_dir))
    check_settings(gradle_path, group)
    check_build(module_dir, module_id, package, kotlin)
    check_sources(module_dir, module_id, package, kotlin)
    if os.path.exists(os.path.join(module_dir, "build.gradle.kts")):
        check_kind(module_dir, kind, gradle_path)
    check_docs(module_dir, gradle_path)
    check_wiring(gradle_path)
    for level, message in findings:
        print(f"{level} {message}")
    fails = sum(level == "FAIL" for level, _ in findings)
    print(f"{gradle_path}: {fails} FAIL, {len(findings) - fails} WARN")
    sys.exit(1 if fails else 0)


main()
