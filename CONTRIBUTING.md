# Contributing to PyVenvManage

## Development Setup

You'll need [mise](https://mise.jdx.dev) to provision the JDKs (`mise install`) and Python 3.10+ (for creating test
virtual environments). Build the plugin with:

```bash
./gradlew buildPlugin
```

## Testing

The project uses two complementary testing strategies: fast unit tests that mock IntelliJ platform dependencies, and
end-to-end UI tests that interact with a running IDE.

### Unit Tests

Unit tests cover business logic, action update logic, and error paths. They run quickly and don't require a running IDE:

```bash
./gradlew test
```

### UI Tests

UI tests validate full user workflows by interacting with a running PyCharm instance via RemoteRobot. Start the IDE with
robot-server in one terminal:

```bash
./gradlew runIdeForUiTests
```

Wait for the IDE to fully start and the robot-server to be ready at http://localhost:8082, then run the tests in another
terminal:

```bash
./gradlew uiTest
```

### Coverage

Unit tests achieve full line coverage. The CI enforces this with `./gradlew test koverVerify`. UI tests are excluded
from coverage collection since they test end-to-end workflows already covered by unit tests.

To generate an HTML coverage report showing overall percentage, package breakdown, and line-by-line highlighting:

```bash
./gradlew test koverHtmlReport
open build/reports/kover/html/index.html
```

For per-test coverage analysis (which test covered which line), generate a binary report with
`./gradlew test koverBinaryReport`, then in IntelliJ IDEA go to **Run → Show Coverage Data**, click **+**, select
`build/kover/bin-reports/test.ic`, and click **Show selected**. Right-click any covered line and choose **Show Covering
Tests** to see which tests hit it.

## Code Quality

Check code style with `./gradlew ktlintCheck` or auto-fix issues with `./gradlew ktlintFormat`. Run all checks together
(lint, unit tests, coverage verification) with `./gradlew check`.

## Continuous Integration

The CI pipeline in `.github/workflows/check.yaml` builds the plugin, runs linting, executes unit tests with coverage
verification followed by UI tests, verifies the plugin against PyCharm Community and PyCharm Professional, and requires
a changelog fragment or `skip news` on pull requests. The aggregate check blocks a pull request if any required job
fails.

The test job runs unit tests with `koverVerify`, then starts Xvfb and the IDE with robot-server, and finally runs UI
tests for end-to-end validation.

## Making Code Changes

Before committing, run `./gradlew ktlintFormat` to fix style issues, then `./gradlew test koverVerify` to ensure tests
pass with full coverage. If you modified action classes, run UI tests for end-to-end validation by starting
`./gradlew runIdeForUiTests` in one terminal and `./gradlew uiTest` in another.

Follow conventional commit style: use `feat:` for new features, `fix:` for bug fixes, `refactor:` for code refactoring,
`test:` for test changes, `docs:` for documentation, and `chore:` for maintenance tasks.

For a user-visible change, add a Markdown file named `changelog.d/+short-name.<category>.md`. Use `feature` for a new
capability, `change` for changed behavior, `bugfix` for a fix, and `doc` for user documentation. `security`,
`performance`, `deprecation`, `removal`, `breaking`, and `packaging` are available when they describe a user-visible
result. Put separate changes in separate files. The release job adds the squash PR link and human author credit after
merge. For maintenance without a user-visible result, apply `skip news` to the PR; bot PRs without fragments receive it
automatically.

## Troubleshooting

If UI tests timeout or fail to connect, ensure no other IDE instance is using port 8082. Kill any running IDE processes
with `pkill -f runIdeForUiTests`, delete old test projects with `rm -rf ~/projects/ui-test*`, then restart the IDE and
wait for full initialization before running tests.

If `koverVerify` fails due to coverage below 100%, generate the HTML report with `./gradlew koverHtmlReport` and open
`build/reports/kover/html/index.html` to see which lines are uncovered. Add unit tests for those code paths, or if the
code requires IntelliJ platform services that can't be mocked, add UI test coverage instead.

## Releasing

Run **Prepare release** on `main` with `auto` after merging the intended changelog fragments. The job chooses a major
version for a `breaking` fragment, a minor version for a `feature` fragment, and a patch version for other categories.
It adds the release section to `CHANGELOG.md`, runs `prek`, and pushes the release commit and `v`-prefixed tag using the
repository-scoped App. The dispatch does not ask for a version number.

The tag workflow runs `prek`, checks the changelog, and sets `pluginVersion` for a keyless Gradle build. A separate job
checks the unsigned ZIP's SHA256 digest, signs it without running Gradle, then checks the signature, ZIP contents, and
embedded version. Another job checks whether the same version is already on
[JetBrains Marketplace](https://plugins.jetbrains.com/plugin/20536-pyvenv-manage-2). An identical published archive
needs no second upload; a different one stops the release. If the version is absent, a fresh job uploads the verified
ZIP. The GitHub Release uses that ZIP and changelog-based notes. The final job opens a PR for the next development
version in `gradle.properties`.

The GitHub Release is published after Marketplace accepts the upload. Marketplace may hold the update for approval, so
its download can remain unavailable. Once the version is public, run **Verify Marketplace release** with its tag to
compare the Marketplace ZIP with the signed GitHub asset. Do not rerun the upload to check its approval status.

The release workflow requires repository secrets configured by maintainers: `PUBLISH_TOKEN` for JetBrains Marketplace
upload, and `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, and `PRIVATE_KEY_PASSWORD` for plugin signing.

The release App key is stored in the branch-restricted `release-auth` environment. The Marketplace and signing secrets
remain in the `release` environment.
