# PyVenvManage

[![Build](https://github.com/tox-dev/PyVenvManage/actions/workflows/check.yaml/badge.svg)](https://github.com/tox-dev/PyVenvManage/actions/workflows/check.yaml)
[![Version](https://img.shields.io/jetbrains/plugin/v/20536)](https://plugins.jetbrains.com/plugin/20536/versions)
[![Rating](https://img.shields.io/jetbrains/plugin/r/rating/20536)](https://plugins.jetbrains.com/plugin/20536)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/20536)](https://plugins.jetbrains.com/plugin/20536)

## Introduction

<!-- Plugin description -->

**PyVenvManage** simplifies Python virtual environment management in JetBrains IDEs.

Managing multiple Python interpreters across different virtual environments (for testing against various Python versions
with tools like `tox` or `nox`) traditionally requires navigating through multiple dialogs in PyCharm. PyVenvManage
streamlines this by enabling quick interpreter selection directly from the project view with just a right-click.

## Features

- **Quick interpreter switching**: Right-click any virtual environment folder to set it as your project or module
  interpreter instantly
- **Visual identification**: Virtual environment folders display with a distinctive icon and Python version badge
  (e.g., `venv [3.11.5]`) in the project view
- **Multi-IDE support**: Works with PyCharm (Community and Professional), IntelliJ IDEA, GoLand, CLion, and RustRover
- **Smart detection**: Automatically detects Python virtual environments by recognizing `pyvenv.cfg` files
- **Cached version display**: Python version information is cached for performance and automatically refreshed when
  `pyvenv.cfg` files change

<!-- Plugin description end -->

## Supported IDEs

- PyCharm (Community and Professional)
- IntelliJ IDEA (Community and Ultimate)
- GoLand
- CLion
- RustRover

## Install

In your JetBrains IDE, open **Settings** -> **Plugins**, search for "PyVenv Manage", and click **Install**.

The official plugin page is at https://plugins.jetbrains.com/plugin/20536-pyvenv-manage-2

## Usage

![usage video](anim.gif?raw=true)

1. Create or navigate to a Python virtual environment folder in your project
2. Right-click the virtual environment folder (e.g., `venv`, `.venv`, or any folder with a `pyvenv.cfg`)
3. Select **Set as Project Interpreter** or **Set as Module Interpreter**
4. The interpreter is configured instantly with a confirmation notification

## License

This project is licensed under the BSD-3-Clause license - see the
[LICENSE](https://github.com/pyvenvmanage/PyVenvManage/blob/main/LICENSE).
