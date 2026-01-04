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
- **Visual identification**: Virtual environment folders display with a distinctive icon and customizable decoration
  (e.g., `.venv [3.11.5 - CPython]`) in the project view
- **Customizable decorations**: Configure which fields to show (Python version, implementation, system site-packages,
  creator tool), their order, and the format via Settings
- **Multi-IDE support**: Works with PyCharm (Community and Professional), IntelliJ IDEA, GoLand, CLion, and RustRover
- **Smart detection**: Automatically detects Python virtual environments by recognizing `pyvenv.cfg` files
- **Cached version display**: Python version information is cached for performance and automatically refreshed when
  `pyvenv.cfg` files change

<!-- Plugin description end -->

## Supported IDEs

Version 2025.1 or later of:

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

## Settings

Open **Settings** -> **PyVenv Manage** to customize the virtual environment decoration display:

- **Prefix/Suffix**: Characters surrounding the decoration (default: ` [` and `]`)
- **Separator**: Text between fields (default: `-`)
- **Fields**: Enable, disable, and reorder which information to display:
  - Python version (e.g., `3.14.2`)
  - Python implementation (e.g., `CPython`)
  - System site-packages indicator (`SYSTEM`)
  - Virtual environment creator (e.g., `uv@0.9.21`)

A live preview updates as you modify settings.

## License

This project is licensed under the BSD-3-Clause license - see the
[LICENSE](https://github.com/pyvenvmanage/PyVenvManage/blob/main/LICENSE).
