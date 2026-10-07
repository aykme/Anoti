"""Checks a Gradle module against the shape the technical design prescribes for its kind.

Usage: python -I .claude/skills/creating-a-module/scripts/check_module.py :<group>:<name> <kind>
(`python3` where only that exists). <kind> is one of KINDS, as new-module.md "Choose the kind"
names it. Prints one line per finding, FAIL or WARN, and exits non-zero on any FAIL.

The checks follow these design sections; a change to one of them changes this script:
- new-module.md: "Choose the kind", "Names", "Settings", "Skeletons", "Boilerplate notes",
  "Wiring";
- module-anatomy.md: "Source sets", "Packages", "Naming", "Manifests and resources";
- project-structure.md "Module kinds"; entities.md; principles.md (`api` with a reason comment);
- navigation.md "Root stack", "Root-level elements", "Adding a destination";
- mvi.md "Mappers and UI models"; platform-mirroring.md "Twins"; testing.md "Test doubles";
- .claude/rules/module-docs.md.
"""
import collections
import os
import re
import sys

ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), *[".."] * 4))
FULL_REGRESS = "ANOTI-FULL-REGRESS.md"
GROUPS = ("core-kmp", "feature-kmp")
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
# Kind -> what its module must hold (label, glob relative to the module directory), in the order
# of new-module.md "Choose the kind". entities.md names each entity's place.
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
    "platform-service": [
        ("the common contract", COMMON + "api/**/*.kt"),
        ("the Android platform component", "src/androidMain/kotlin/**/Di*PlatformComponent.kt"),
        ("the iOS platform component", "src/iosMain/kotlin/**/Di*PlatformComponent.kt")],
    "shared-base": [],
    "external": [("a contract", "src/*Main/kotlin/**/api/**/*.kt")],
    "core": [],
}
findings = []


def fail(message):
    findings.append(("FAIL", message))


def warn(message):
    findings.append(("WARN", message))


def text(path):
    with open(path, encoding="utf-8-sig", errors="replace") as handle:
        return handle.read()


def build_files():
    for directory, dirs, names in os.walk(ROOT):
        dirs[:] = [d for d in dirs if not d.startswith(".") and d != "build"]
        if "build.gradle.kts" in names:
            yield os.path.join(directory, "build.gradle.kts")


def root_package():
    """The prefix of the other modules' namespaces `<root-package>.<module-id>.kmp`."""
    prefixes = collections.Counter()
    for path in build_files():
        name = os.path.basename(os.path.dirname(path))
        module_id = re.escape(name.replace("-", ""))
        match = re.search(rf'namespace = "([\w.]+)\.{module_id}\.kmp"', text(path))
        if match:
            prefixes[match.group(1)] += 1
    if not prefixes:
        sys.exit("no other module's namespace names the root package yet")
    return prefixes.most_common(1)[0][0]


def module_files(module_dir):
    """Paths under `src`, relative to the module, with forward slashes; hidden entries skipped."""
    for directory, dirs, names in os.walk(os.path.join(module_dir, "src")):
        dirs[:] = [d for d in dirs if not d.startswith(".")]
        for name in names:
            if not name.startswith("."):
                path = os.path.join(directory, name)
                yield os.path.relpath(path, module_dir).replace("\\", "/")


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


def check_settings(gradle_path, group):
    lines = text(os.path.join(ROOT, "settings.gradle.kts")).splitlines()
    includes = [i for i, line in enumerate(lines) if line.startswith("include(")]
    mine = [i for i in includes if lines[i] == f'include("{gradle_path}")']
    if len(mine) != 1:
        fail(f'settings.gradle.kts needs exactly one line include("{gradle_path}")')
        return
    position = includes.index(mine[0])
    others = [includes.index(i) for i in includes
              if i != mine[0] and lines[i].startswith(f'include(":{group}:')]
    if others and not (min(others) - 1 <= position <= max(others) + 1):
        fail(f"the include line sits outside the :{group} group")


def check_build(module_dir, module_id, package, kotlin, files):
    build = text(os.path.join(module_dir, "build.gradle.kts"))
    for plugin in REQUIRED_PLUGINS:
        if f"libs.plugins.{plugin})" not in build:
            fail(f"plugin {plugin} is missing; start from skeleton (a)")
    expected = f'"{package}.{module_id}.kmp"'
    if f"namespace = {expected}" not in build:
        fail(f"the namespace is not {expected}")
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
    components = [f for f in kotlin
                  if re.search(r"^\s*(?:@\w+(?:\([^)]*\))?\s+)*@Component\b", f[1], re.M)]
    has_ksp = "libs.plugins.ksp)" in build
    if components and not has_ksp:
        fail("a @Component lives here but KSP is not applied")
    if has_ksp:
        for target in KSP_TARGETS:
            if f'"{target}"' not in build and f'"ksp{target}"' not in build:
                fail(f"KSP is missing for the {target} target")
        if not components and not re.search(r"\broom\b", build, re.I):
            fail("KSP is applied but no @Component or Room lives here")
    expects = [f for f in kotlin if re.search(r"^\s*(?:\w+\s+)*expect\s+(?:class|object)\b",
                                              f[1], re.M)]
    if expects and "-Xexpect-actual-classes" not in build:
        fail("an expect class or object needs -Xexpect-actual-classes (skeleton (c))")
    has_resources = any(f.startswith("src/commonMain/composeResources/") for f in files)
    enables_resources = re.search(r"androidResources\s*\{\s*enable\s*=\s*true", build)
    res_package = f'packageOfResClass = "{package}.{module_id}.kmp.generated.resources"'
    if has_resources:
        if not enables_resources:
            fail("compose resources need androidResources { enable = true }")
        if "compose.resources" not in build or res_package not in build:
            fail(f"compose resources need a compose.resources block with {res_package}")
    elif enables_resources or "compose.resources" in build:
        fail("androidResources and compose.resources come only with "
             "src/commonMain/composeResources")


def check_sources(module_dir, module_id, package, kotlin, kind):
    src = os.path.join(module_dir, "src")
    if not os.path.isdir(src):
        fail("no src directory")
        return
    for source_set in sorted(d for d in os.listdir(src)
                             if os.path.isdir(os.path.join(src, d)) and not d.startswith(".")):
        if source_set not in PLATFORMS:
            fail(f"src/{source_set} is not a source set of a KMP module")
        elif os.path.isdir(os.path.join(src, source_set, "java")):
            fail(f"src/{source_set}/java: source directories are kotlin, never java")
    if not os.path.isdir(os.path.join(src, "commonMain")) and kind != "external":
        fail("no src/commonMain: commonMain is the default home of code")
    if not any(d.endswith("Test") for d in os.listdir(src)):
        warn("no test source set yet")
    module_package = package + "." + module_id
    for rel, body in kotlin:
        source_set = rel.split("/")[1]
        declared = re.search(r"^package ([\w.]+)", body, re.M)
        if not declared:
            fail(f"{rel} has no package declaration")
            continue
        name = declared.group(1)
        if name != "/".join(rel.split("/")[3:-1]).replace("/", "."):
            fail(f"{rel}: package {name} does not match its directory")
        platform = PLATFORMS.get(source_set)
        if platform:
            # A test takes the package of the code it tests; an actual, that of its expect.
            allowed = {platform}
            if source_set.endswith("Test") or re.search(r"^\s*(?:\w+\s+)*actual\s", body, re.M):
                allowed.add("kmp")
            prefixes = [f"{module_package}.{p}." for p in sorted(allowed)]
            matched = next((p for p in prefixes if name.startswith(p)), None)
            if not matched:
                fail(f"{rel}: package must start with {' or '.join(prefixes)}")
            elif name[len(matched):].split(".")[0] not in ("api", "impl"):
                fail(f"{rel}: the segment after {matched.rstrip('.')} must be api or impl")
        stem = os.path.basename(rel)[:-3]
        in_fake = name.split(".")[-1] == "fake"
        if in_fake and not stem.endswith(("Fake", "FakeTest")):
            fail(f"{rel}: a file in a fake package is a double named *Fake")
        if stem.endswith("Fake") and not in_fake:
            fail(f"{rel}: a double's file sits in a package ending .fake (testing.md)")
        if re.search(r"^\s*(?:(?:private|internal|public|open|abstract|data|sealed|inner)\s+)*"
                     r"(?:class|object|interface)\s+Fake\w*", body, re.M):
            fail(f"{rel}: Fake goes at the end of a double's name, never at the front")


def check_kind(module_dir, kind, gradle_path, package, files, build):
    for label, pattern in KINDS[kind]:
        regex = glob_regex(pattern)
        if not any(regex.match(f) for f in files):
            fail(f"{kind}: {label} is missing ({pattern})")
    stores = [f for f in files if "/domain/store/" in f]
    screens = [f for f in files if re.search(r"/Nav\w+ScreenComponent\.kt$", f)]
    routes = [f for f in files if f.endswith("Route.kt")]
    if kind == "screen-feature":
        configs_path = os.path.join(ROOT, "core-kmp", "navigation", "src", "commonMain", "kotlin",
                                    *package.split("."), "navigation", "kmp", "NavRootConfig.kt")
        configs = text(configs_path) if os.path.exists(configs_path) else ""
        if not configs:
            fail("no root configs to register the screen in (navigation.md \"Root stack\")")
        for screen in screens:
            feature = re.search(r"/Nav(\w+)ScreenComponent\.kt$", screen).group(1)
            if configs and (f'@SerialName("{feature}")' not in configs
                            or f"data object {feature} " not in configs):
                fail(f"no root config `{feature}` with @SerialName(\"{feature}\") "
                     "(navigation.md \"Adding a destination\")")
        main_build = os.path.join(ROOT, "main", "build.gradle.kts")
        if not os.path.exists(main_build) or f'project("{gradle_path}")' not in text(main_build):
            fail("the entry module does not depend on the screen feature")
    if kind == "root-level-element":
        if screens:
            fail("a root-level element has no screen component")
        if routes:
            fail("a root-level element's route lives in the entry module "
                 "(navigation.md \"Root-level elements\")")
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
            fail("platform-service: no platform implementation (*Impl) of a common contract")
        for name, sets in sorted(twins.items()):
            if sets != {"androidMain", "iosMain"}:
                missing = ({"androidMain", "iosMain"} - sets).pop()
                fail(f"{name} has no twin of the same name in {missing} "
                     "(platform-mirroring.md \"Twins\")")
    if kind == "external":
        for f in files:
            if re.match(r"src/\w+Main/.*/impl/", f) and "/fake/" not in f:
                fail(f"{f}: an external module holds contracts only; the entry module "
                     "implements them")


def check_docs(module_dir, gradle_path):
    stem = gradle_path.lstrip(":").replace(":", "-").upper()
    present = os.listdir(module_dir)
    for suffix in ("README", "REGRESS"):
        if f"{stem}-{suffix}.md" not in present:
            fail(f"{stem}-{suffix}.md is missing at the module root (code-documentation skill)")
    full = os.path.join(ROOT, FULL_REGRESS)
    _, group, name = gradle_path.split(":")
    if not os.path.exists(full) or f"]({group}/{name}/{stem}-REGRESS.md)" not in text(full):
        fail(f"{FULL_REGRESS} does not link {group}/{name}/{stem}-REGRESS.md (module-docs.md)")


def check_wiring(gradle_path, module_dir):
    for path in build_files():
        if os.path.dirname(path) != module_dir and f'project("{gradle_path}")' in text(path):
            return
    warn("no other module depends on it yet (new-module.md \"Wiring\")")


def main():
    pattern = rf":({'|'.join(GROUPS)}):[a-z0-9-]+"
    if len(sys.argv) != 3 or sys.argv[2] not in KINDS or not re.fullmatch(pattern, sys.argv[1]):
        sys.exit(f"usage: check_module.py :<{'|'.join(GROUPS)}>:<name> <{'|'.join(KINDS)}>")
    gradle_path, kind = sys.argv[1], sys.argv[2]
    _, group, name = gradle_path.split(":")
    module_dir = os.path.join(ROOT, group, name)
    if not os.path.isfile(os.path.join(module_dir, "build.gradle.kts")):
        sys.exit(f"no build.gradle.kts in {group}/{name}")
    module_id = name.replace("-", "")
    if module_id.endswith("external"):
        module_id = module_id[: -len("external")] + ".external"
    package = root_package()
    files = list(module_files(module_dir))
    kotlin = [(f, text(os.path.join(module_dir, f))) for f in files if f.endswith(".kt")]
    build = text(os.path.join(module_dir, "build.gradle.kts"))
    check_settings(gradle_path, group)
    check_build(module_dir, module_id, package, kotlin, files)
    check_sources(module_dir, module_id, package, kotlin, kind)
    check_kind(module_dir, kind, gradle_path, package, files, build)
    check_docs(module_dir, gradle_path)
    check_wiring(gradle_path, module_dir)
    for level, message in findings:
        print(f"{level} {message}")
    fails = sum(level == "FAIL" for level, _ in findings)
    print(f"{gradle_path}: {fails} FAIL, {len(findings) - fails} WARN")
    sys.exit(1 if fails else 0)


if __name__ == "__main__":
    main()
