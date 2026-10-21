# PyVenvManage Changelog

## [Unreleased]

<!-- towncrier release notes start -->

<a name="release-2-4-6"></a>

## [2.4.6] - 2026-09-28

<a name="release-2-4-6-fixed"></a>

### Fixed

- Keep virtual environment icons loading on PyCharm 2026.3 ([#256](https://github.com/tox-dev/PyVenvManage/pull/256)) by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-4-5"></a>

## [2.4.5] - 2026-09-19

<a name="release-2-4-5-fixed"></a>

### Fixed

- Keep Conda, Poetry, Hatch, uv, and Pipenv icons available when their platform resource paths change
  ([#250](https://github.com/tox-dev/PyVenvManage/pull/250)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-4-4"></a>

## [2.4.4] - 2026-09-15

<a name="release-2-4-4-fixed"></a>

### Fixed

- Keep the plugin loading on PyCharm 2026.3 EAP ([#247](https://github.com/tox-dev/PyVenvManage/pull/247)) by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-4-3"></a>

## [2.4.3] - 2026-09-15

<a name="release-2-4-3-fixed"></a>

### Fixed

- Set the project root as the working directory for Hatch and uv interpreters without configuration files
  ([#243](https://github.com/tox-dev/PyVenvManage/pull/243)) by [@gaborbernat](https://github.com/gaborbernat).
- Set the project SDK when needed so Python scratch files resolve builtins
  ([#244](https://github.com/tox-dev/PyVenvManage/pull/244)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-4-2"></a>

## [2.4.2] - 2026-07-29

<a name="release-2-4-2-fixed"></a>

### Fixed

- Show “Set as Project Interpreter” only for virtual environment directories
  ([#221](https://github.com/tox-dev/PyVenvManage/pull/221)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-4-1"></a>

## [2.4.1] - 2026-05-29

<a name="release-2-4-0"></a>

## [2.4.0] - 2026-05-29

<a name="release-2-4-0-added"></a>

### Added

- Support IntelliJ Platform 2026.2 ([#186](https://github.com/tox-dev/PyVenvManage/pull/186)) by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-3-2"></a>

## [2.3.2] - 2026-05-29

<a name="release-2-3-2-added"></a>

### Added

- Limit the 2.3 line to IntelliJ Platform 2026.1 and earlier; use 2.4.0 for 2026.2
  ([#184](https://github.com/tox-dev/PyVenvManage/pull/184)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-3-1"></a>

## [2.3.1] - 2026-05-29

<a name="release-2-3-0"></a>

## [2.3.0] - 2026-04-02

<a name="release-2-3-0-added"></a>

### Added

- Show tool-specific icons for uv, Conda, Hatch, Poetry, and Pipenv environments
  ([#163](https://github.com/tox-dev/PyVenvManage/pull/163)) by [@gaborbernat](https://github.com/gaborbernat).
- Create SDKs with the environment data needed by PyCharm 2026.1 package managers
  ([#163](https://github.com/tox-dev/PyVenvManage/pull/163)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-3-0-fixed"></a>

### Fixed

- Associate an SDK with a project only when the virtual environment lives inside that project
  ([#163](https://github.com/tox-dev/PyVenvManage/pull/163)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-2-7"></a>

## [2.2.7] - 2026-03-31

<a name="release-2-2-6"></a>

## [2.2.6] - 2026-03-31

<a name="release-2-2-6-fixed"></a>

### Fixed

- Keep virtual environment icons loading on IntelliJ Platform 2026.1
  ([#157](https://github.com/tox-dev/PyVenvManage/pull/157)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-2-6-documentation"></a>

### Documentation

- Explain the difficulty of selecting virtual environments in the IDE
  ([#143](https://github.com/tox-dev/PyVenvManage/pull/143)) by [@andrask](https://github.com/andrask).

<a name="release-2-2-5"></a>

## [2.2.5] - 2026-01-30

<a name="release-2-2-4"></a>

## [2.2.4] - 2026-01-30

<a name="release-2-2-3"></a>

## [2.2.3] - 2026-01-30

<a name="release-2-2-3-added"></a>

### Added

- Allow installation in IDEs without bundled Python support and explain the missing dependency at startup
  ([#122](https://github.com/tox-dev/PyVenvManage/pull/122)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-2-2"></a>

## [2.2.2] - 2026-01-29

<a name="release-2-2-1"></a>

## [2.2.1] - 2026-01-05

<a name="release-2-2-1-documentation"></a>

### Documentation

- Refresh the demonstration GIF ([#110](https://github.com/tox-dev/PyVenvManage/pull/110)) by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-2-0"></a>

## [2.2.0] - 2026-01-04

<a name="release-2-2-0-added"></a>

### Added

- Show an error notification when setting a Python interpreter fails
  ([#102](https://github.com/tox-dev/PyVenvManage/pull/102)) by [@gaborbernat](https://github.com/gaborbernat).
- Add a settings page for Python version display and its format
  ([#103](https://github.com/tox-dev/PyVenvManage/pull/103)) by [@gaborbernat](https://github.com/gaborbernat).
- Show the Python implementation, system packages, and creator tool beside virtual environments in the project view
  ([#104](https://github.com/tox-dev/PyVenvManage/pull/104)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-2-0-fixed"></a>

### Fixed

- Refresh cached Python version details when `pyvenv.cfg` changes
  ([#101](https://github.com/tox-dev/PyVenvManage/pull/101)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-2-0-documentation"></a>

### Documentation

- Explain supported IDEs, installation, and plugin use ([#105](https://github.com/tox-dev/PyVenvManage/pull/105)) by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-1-2"></a>

## [2.1.2] - 2025-10-23

<a name="release-2-1-2-fixed"></a>

### Fixed

- Show the correct Python version in the project view ([#77](https://github.com/tox-dev/PyVenvManage/pull/77)) by
  [@andrask](https://github.com/andrask).

<a name="release-2-1-1"></a>

## [2.1.1] - 2025-10-08

<a name="release-2-1-1-fixed"></a>

### Fixed

- Avoid a crash when showing the notification after changing the Python SDK
  ([#59](https://github.com/tox-dev/PyVenvManage/pull/59)) by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-1-0"></a>

## [2.1.0] - 2025-06-25

<a name="release-2-1-0-added"></a>

### Added

- Show the Python version in the project tree ([#51](https://github.com/tox-dev/PyVenvManage/pull/51)) by
  [@andrask](https://github.com/andrask).

<a name="release-2-0-1"></a>

## [2.0.1] - 2024-08-12

<a name="release-2-0-1-added"></a>

### Added

- Support PyCharm Professional by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-0-1-fixed"></a>

### Fixed

- Restore the virtual environment icon provider after the version 2 migration by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-0-0"></a>

## [2.0.0] - 2024-08-12

<a name="release-2-0-0-changes"></a>

### Changes

- Clarify the interpreter action's menu label and description by [@gaborbernat](https://github.com/gaborbernat).
- Include the environment and execution type in interpreter notifications by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-2-0-0-fixed"></a>

### Fixed

- Restore the virtual environment icon in the context menu by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-1-4-0"></a>

## [1.4.0]

<a name="release-1-4-0-added"></a>

### Added

- Support IntelliJ Platform 2024.1 by [@gaborbernat](https://github.com/gaborbernat).

<a name="release-1-3-10"></a>

## [1.3.10] - 2023-10-31

<a name="release-1-3-10-added"></a>

### Added

- Support IntelliJ Platform 2023.1 and 2023.2 without an upper IDE version bound by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-1-3-5"></a>

## [1.3.5] - 2022-01-06

<a name="release-1-3-5-added"></a>

### Added

- Show the interpreter popup option for files inside virtual environment directories by
  [@andrask](https://github.com/andrask).
- Limit compatibility to IntelliJ Platform 2021.3 and earlier by [@andrask](https://github.com/andrask).

<a name="release-1-3-4rc2"></a>

## [1.3.4rc2] - 2021-10-02

<a name="release-1-3-4rc2-added"></a>

### Added

- Limit compatibility to IntelliJ Platform 2022.1 and earlier by [@andrask](https://github.com/andrask).

<a name="release-1-3-4rc"></a>

## [1.3.4rc] - 2021-09-29

<a name="release-1-3-4rc-added"></a>

### Added

- Extend compatibility to IntelliJ Platform 2024.1 by [@andrask](https://github.com/andrask).

<a name="release-1-3-3"></a>

## [1.3.3]

<a name="release-1-3-3-added"></a>

### Added

- Support IntelliJ Platform 2021.3 ([#18](https://github.com/nokia/PyVenvManage/pull/18)) by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-1-3-2"></a>

## [1.3.2] - 2021-06-08

<a name="release-1-3-2-added"></a>

### Added

- Support PyCharm 2021.2 ([#16](https://github.com/nokia/PyVenvManage/issues/16)) by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-1-3-1"></a>

## [1.3.1] - 2021-02-20

<a name="release-1-3-1-added"></a>

### Added

- Support IntelliJ Platform 2021.1 EAP ([#13](https://github.com/nokia/PyVenvManage/issues/13)) by
  [@gaborbernat](https://github.com/gaborbernat).

<a name="release-1-3-0"></a>

## [1.3.0] - 2020-11-11

<a name="release-1-2"></a>

## [1.2] - 2019-05-20

<a name="release-1-2-added"></a>

### Added

- Allow installation in IntelliJ products beyond PyCharm by [@andrask](https://github.com/andrask).

<a name="release-1-1"></a>

## [1.1] - 2019-01-22

<a name="release-1-1-added"></a>

### Added

- Set a Python interpreter for an individual module by [@andrask](https://github.com/andrask).

<a name="release-1-1-fixed"></a>

### Fixed

- Set SDK paths when assigning a module interpreter ([#6](https://github.com/nokia/PyVenvManage/issues/6)) by
  [@andrask](https://github.com/andrask).

<a name="release-1-0"></a>

## [1.0] - 2017-07-19

<a name="release-1-0-added"></a>

### Added

- Set a project's Python interpreter from a popup menu by [@andrask](https://github.com/andrask).
- Mark virtual environments with icons in the project view by [@andrask](https://github.com/andrask).

[1.0]: https://github.com/tox-dev/PyVenvManage/commits/1.0
[1.1]: https://github.com/tox-dev/PyVenvManage/compare/1.0...1.1
[1.2]: https://github.com/tox-dev/PyVenvManage/compare/1.1...1.2
[1.3.0]: https://github.com/tox-dev/PyVenvManage/compare/1.2...1.3.0
[1.3.1]: https://github.com/tox-dev/PyVenvManage/compare/1.3.0...1.3.1
[1.3.10]: https://github.com/tox-dev/PyVenvManage/compare/v1.3.5...1.3.10
[1.3.2]: https://github.com/tox-dev/PyVenvManage/compare/1.3.1...v1.3.2
[1.3.3]: https://github.com/tox-dev/PyVenvManage/compare/v1.3.2...v1.3.3
[1.3.4rc]: https://github.com/tox-dev/PyVenvManage/compare/v1.3.3...v1.3.4rc
[1.3.4rc2]: https://github.com/tox-dev/PyVenvManage/compare/v1.3.4rc...v1.3.4rc2
[1.3.5]: https://github.com/tox-dev/PyVenvManage/compare/v1.3.4rc2...v1.3.5
[1.4.0]: https://github.com/tox-dev/PyVenvManage/compare/1.3.10...1.4.0
[2.0.0]: https://github.com/tox-dev/PyVenvManage/compare/1.4.0...v2.0.0
[2.0.1]: https://github.com/tox-dev/PyVenvManage/compare/v2.0.0...v2.0.1
[2.1.0]: https://github.com/tox-dev/PyVenvManage/compare/v2.0.1...v2.1.0
[2.1.1]: https://github.com/tox-dev/PyVenvManage/compare/v2.1.0...v2.1.1
[2.1.2]: https://github.com/tox-dev/PyVenvManage/compare/v2.1.1...v2.1.2
[2.2.0]: https://github.com/tox-dev/PyVenvManage/compare/v2.1.2...v2.2.0
[2.2.1]: https://github.com/tox-dev/PyVenvManage/compare/v2.2.0...v2.2.1
[2.2.2]: https://github.com/tox-dev/PyVenvManage/compare/v2.2.1...v2.2.2
[2.2.3]: https://github.com/tox-dev/PyVenvManage/compare/v2.2.2...v2.2.3
[2.2.4]: https://github.com/tox-dev/PyVenvManage/compare/v2.2.3...v2.2.4
[2.2.5]: https://github.com/tox-dev/PyVenvManage/compare/v2.2.4...v2.2.5
[2.2.6]: https://github.com/tox-dev/PyVenvManage/compare/v2.2.5...v2.2.6
[2.2.7]: https://github.com/tox-dev/PyVenvManage/compare/v2.2.6...v2.2.7
[2.3.0]: https://github.com/tox-dev/PyVenvManage/compare/v2.2.7...v2.3.0
[2.3.1]: https://github.com/tox-dev/PyVenvManage/compare/v2.3.0...v2.3.1
[2.3.2]: https://github.com/tox-dev/PyVenvManage/compare/v2.3.1...v2.3.2
[2.4.0]: https://github.com/tox-dev/PyVenvManage/compare/v2.3.2...v2.4.0
[2.4.1]: https://github.com/tox-dev/PyVenvManage/compare/v2.4.0...v2.4.1
[2.4.2]: https://github.com/tox-dev/PyVenvManage/compare/v2.4.1...v2.4.2
[2.4.3]: https://github.com/tox-dev/PyVenvManage/compare/v2.4.2...v2.4.3
[2.4.4]: https://github.com/tox-dev/PyVenvManage/compare/v2.4.3...v2.4.4
[2.4.5]: https://github.com/tox-dev/PyVenvManage/compare/v2.4.4...v2.4.5
[2.4.6]: https://github.com/tox-dev/PyVenvManage/compare/v2.4.5...v2.4.6
