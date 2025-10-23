# PyVenvManage Changelog

## [Unreleased]

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

[Unreleased]: https://github.com/pyvenvmanage/PyVenvManage/compare/v2.1.2...HEAD
[2.1.2]: https://github.com/pyvenvmanage/PyVenvManage/compare/v2.1.0...v2.1.2
[2.1.0]: https://github.com/pyvenvmanage/PyVenvManage/compare/v2.0.1...v2.1.0
[2.0.1]: https://github.com/pyvenvmanage/PyVenvManage/compare/v2.0.0...v2.0.1
[2.0.0]: https://github.com/pyvenvmanage/PyVenvManage/compare/v1.4.0...v2.0.0
[1.4.0]: https://github.com/pyvenvmanage/PyVenvManage/compare/v1.3.4...v1.4.0
[1.3.4]: https://github.com/pyvenvmanage/PyVenvManage/compare/v1.3.3...v1.3.4
[1.3.3]: https://github.com/pyvenvmanage/PyVenvManage/compare/v1.3.2...v1.3.3
[1.3.2]: https://github.com/pyvenvmanage/PyVenvManage/compare/v1.3.1...v1.3.2
[1.3.1]: https://github.com/pyvenvmanage/PyVenvManage/compare/v1.3.0...v1.3.1
[1.3.0]: https://github.com/pyvenvmanage/PyVenvManage/commits/v1.3.0
