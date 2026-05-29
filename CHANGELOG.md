# PyVenvManage Changelog

## [Unreleased]

## [2.3.1] - 2026-05-29

${GITHUB_EVENT_RELEASE_BODY}

## [2.3.0] - 2026-04-02

${GITHUB_EVENT_RELEASE_BODY}

## [2.2.7] - 2026-03-31

${GITHUB_EVENT_RELEASE_BODY}

## [2.2.6] - 2026-03-31

- Bump version to `2.2.6-dev` by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/127
- Add permissions to workflows by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/137
- Move SECURITY.md to .github/SECURITY.md by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/139
- Add missing .github config files by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/140
- Switch FUNDING.yml to github: gaborbernat by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/141
- Standardize .github files to .yaml suffix by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/142
- Clarify the venv selection painfulness by @andrask in https://github.com/tox-dev/PyVenvManage/pull/143
- 🔒 ci(workflows): add zizmor security auditing by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/154
- 🐛 fix(icons): resolve NoSuchFieldError on IntelliJ 2026.1 by @gaborbernat in
  https://github.com/tox-dev/PyVenvManage/pull/157
- 🔒 fix(ci): split release workflow for proper credential scoping by @gaborbernat in
  https://github.com/tox-dev/PyVenvManage/pull/158

## [2.2.5] - 2026-01-30

- Replace deprecated PluginDescriptor.isEnabled API by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/126

## [2.2.4] - 2026-01-30

- Bump version to `2.2.4-dev` by @github-actions[bot] in https://github.com/tox-dev/PyVenvManage/pull/123
- Use RELEASE_TOKEN for post-release PR and auto-merge by @gaborbernat in
  https://github.com/tox-dev/PyVenvManage/pull/124

## [2.2.3] - 2026-01-30

- Bump version to `2.2.3-dev` by @github-actions[bot] in https://github.com/tox-dev/PyVenvManage/pull/120
- Add auto-merge workflow for trusted contributors by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/121
- Make Python dependency optional to fix marketplace verification by @gaborbernat in
  https://github.com/tox-dev/PyVenvManage/pull/122

## [2.2.2] - 2026-01-29

- Changelog update - `v2.2.1` by @github-actions[bot] in https://github.com/tox-dev/PyVenvManage/pull/111
- Improve CI workflows and release process by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/119

## [2.2.1] - 2026-01-05

- Changelog update - `v2.2.0` by @github-actions[bot] in https://github.com/tox-dev/PyVenvManage/pull/107
- Fix Gradle transforms cache corruption in CI by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/109
- Refresh demo gif by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/110

## [2.2.0] - 2026-01-04

- Changelog update - `v2.1.2` by @github-actions[bot] in https://github.com/tox-dev/PyVenvManage/pull/78
- Optimize GitHub Actions: parallelize verification and fix disk space by @gaborbernat in
  https://github.com/tox-dev/PyVenvManage/pull/99
- Refactor to modern Kotlin idioms and fix deprecated API by @gaborbernat in
  https://github.com/tox-dev/PyVenvManage/pull/100
- Add cache invalidation with file watcher by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/101
- Improve error UX with notifications by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/102
- Add plugin settings page by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/103
- Improve plugin description and documentation by @gaborbernat in https://github.com/tox-dev/PyVenvManage/pull/105
- Enhance project view decorations and add 100% test coverage by @gaborbernat in
  https://github.com/tox-dev/PyVenvManage/pull/104

## [2.1.2] - 2025-10-23

- Fix python version display (#76) by @andrask in https://github.com/tox-dev/PyVenvManage/pull/77

## [2.1.0] - 2025-06-25

- Add support for showing python version in tree view by @andrask in https://github.com/tox-dev/PyVenvManage/pull/51

## [2.0.1] - 2024-08-12

- Extend compatibility for PyCharm Professional
- Fix Icon provider did not survive migration to v2

## [2.0.0] - 2024-08-12

### Changed

- Improve action menu label and description
- On interpreter set include the Python environment type (virtualenv) and execution type (local) in the notification
  message

### Fixed

- Icon not showing up on context menu, use the virtual environment icon

### Internal

- Use Kotlin instead of Java
- Move to `org.jetbrains.intellij.platform:2.0.0` as the build/test plugin
- Add UI tests

## [1.4.0]

### Fixed

- Fix support for 2024.1

## [1.3.4]

### Fixed

- Fix support for 2021.3

## [1.3.3]

### Fixed

- https://github.com/nokia/PyVenvManage/pull/18 Upgrade gradle and support 2021.3

### Thanks

- I would like to express my gratitude towards @gaborbernat for his continued support of the plugin.

## [1.3.2] - 2021-06-08

### Fixed

- [16](https://github.com/nokia/PyVenvManage/issues/16) PyCharm 2021.1.2

## [1.3.1] - 2021-02-20

### Fixed

- [13](https://github.com/nokia/PyVenvManage/issues/13) Enable compatibility with 2021.1 EAP

## [1.3.0] - 2020-11-11

### Fixed

- Removed the usage of the deprecated PythonSdkType.getPythonExecutable API

[Unreleased]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.3.1...HEAD
[2.3.1]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.3.0...2.3.1
[2.3.0]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.2.7...2.3.0
[2.2.7]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.2.6...2.2.7
[2.2.6]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.2.5...2.2.6
[2.2.5]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.2.4...2.2.5
[2.2.4]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.2.3...2.2.4
[2.2.3]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.2.2...2.2.3
[2.2.2]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.2.1...2.2.2
[2.2.1]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.2.0...2.2.1
[2.2.0]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.1.2...2.2.0
[2.1.2]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.1.0...2.1.2
[2.1.0]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.0.1...2.1.0
[2.0.1]: https://github.com/pyvenvmanage/PyVenvManage/compare/2.0.0...2.0.1
[2.0.0]: https://github.com/pyvenvmanage/PyVenvManage/compare/1.4.0...2.0.0
[1.4.0]: https://github.com/pyvenvmanage/PyVenvManage/compare/1.3.4...1.4.0
[1.3.4]: https://github.com/pyvenvmanage/PyVenvManage/compare/1.3.3...1.3.4
[1.3.3]: https://github.com/pyvenvmanage/PyVenvManage/compare/1.3.2...1.3.3
[1.3.2]: https://github.com/pyvenvmanage/PyVenvManage/compare/1.3.1...1.3.2
[1.3.1]: https://github.com/pyvenvmanage/PyVenvManage/compare/1.3.0...1.3.1
[1.3.0]: https://github.com/pyvenvmanage/PyVenvManage/commits/1.3.0
[unreleased]: https://github.com/pyvenvmanage/PyVenvManage/compare/v2.2.7...HEAD
