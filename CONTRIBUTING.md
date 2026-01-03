# Contributing to PyVenvManage

## Development Setup

You'll need JDK 21 and Python 3.10+ (for creating test virtual environments). Build the plugin with:

```bash
./gradlew buildPlugin
```

## Testing

The project uses two complementary testing strategies: fast unit tests that mock IntelliJ platform dependencies, and
end-to-end UI tests that interact with a running IDE.

### Unit Tests

Unit tests cover business logic, action update logic, and error paths. They run quickly and don't require a running
IDE:

```bash
./gradlew test
```

### UI Tests

UI tests validate full user workflows by interacting with a running PyCharm instance via RemoteRobot. Start the IDE
with robot-server in one terminal:

```bash
./gradlew runIdeForUiTests
```

Wait for the IDE to fully start and the robot-server to be ready at http://localhost:8082, then run the tests in
another terminal:

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
verification followed by UI tests, verifies the plugin against PyCharm Community and PyCharm Professional, and creates
a release draft on the main branch.

The test job runs unit tests with `koverVerify`, then starts Xvfb and the IDE with robot-server, and finally runs UI
tests for end-to-end validation.

## Making Code Changes

Before committing, run `./gradlew ktlintFormat` to fix style issues, then `./gradlew test koverVerify` to ensure tests
pass with full coverage. If you modified action classes, run UI tests for end-to-end validation by starting
`./gradlew runIdeForUiTests` in one terminal and `./gradlew uiTest` in another.

Follow conventional commit style: use `feat:` for new features, `fix:` for bug fixes, `refactor:` for code
refactoring, `test:` for test changes, `docs:` for documentation, and `chore:` for maintenance tasks.

## Troubleshooting

If UI tests timeout or fail to connect, ensure no other IDE instance is using port 8082. Kill any running IDE processes
with `pkill -f runIdeForUiTests`, delete old test projects with `rm -rf ~/projects/ui-test*`, then restart the IDE and
wait for full initialization before running tests.

If `koverVerify` fails due to coverage below 100%, generate the HTML report with `./gradlew koverHtmlReport` and open
`build/reports/kover/html/index.html` to see which lines are uncovered. Add unit tests for those code paths, or if the
code requires IntelliJ platform services that can't be mocked, add UI test coverage instead.

## Releasing

The plugin version is defined in `gradle.properties` as `pluginVersion`. To release, update the version in that file
and merge your PR to main. The CI automatically creates a draft release on GitHub with the version from
`gradle.properties`.

Review the draft release on the [Releases page](https://github.com/pyvenvmanage/PyVenvManage/releases) and edit the
release notes if needed. Click "Publish release" (not pre-release) to trigger the release workflow, which builds and
signs the plugin, publishes to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/20536-pyvenv-manage-2),
uploads the plugin ZIP to the GitHub release, and creates a PR to update CHANGELOG.md. Merge that changelog PR after
the release workflow completes.

The release workflow requires repository secrets configured by maintainers: `PUBLISH_TOKEN` for JetBrains Marketplace
upload, and `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, and `PRIVATE_KEY_PASSWORD` for plugin signing.

Follow [semantic versioning](https://semver.org/): increment MAJOR for breaking changes, MINOR for new backward
compatible features, and PATCH for backward compatible bug fixes.
