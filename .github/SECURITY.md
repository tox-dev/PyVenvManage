# Security policy

PyVenvManage is a JetBrains IDE plugin that marks virtual environment folders in the project view and sets one as the
project or module interpreter on request. This policy says how to report a security problem and how we decide whether a
report is one.

## Reporting a vulnerability

**Do not open a public issue for a security problem.** Use
[GitHub's private vulnerability reporting](https://github.com/tox-dev/PyVenvManage/security/advisories/new). If you do
not have a GitHub account, or you are a Tidelift subscriber, report through the
[Tidelift security contact](https://tidelift.com/security) instead; Tidelift coordinates the fix and the disclosure with
us. If neither works for you, email [gaborjbernat@gmail.com](mailto:gaborjbernat@gmail.com).

Include what the attacker controls and what they gain, the steps or a proof of concept, and the versions and platform
you tested. Write the report yourself; unreviewed tool or model output takes longer to triage.

## What to expect

Volunteers maintain PyVenvManage, with no on-call rotation. We aim to acknowledge a report within **5 working days** and
assess it within **15 working days**. Fixes are developed in a private fork attached to the draft advisory, ship in a
new release, and the advisory, with a CVE requested through GitHub, goes out with that release. The default embargo is
**90 days** from the report; we agree any change with the reporter. We credit reporters in the advisory under the name
they choose, or not at all. There is no bug bounty.

## Supported versions

Only the latest release receives fixes. Confirm the problem reproduces on it before reporting.

## Vulnerability or bug

A report is a vulnerability when an attacker who controls only an input that the [threat model](THREAT_MODEL.md) lists
as untrusted breaks a guarantee PyVenvManage makes. A report that first needs control of something the threat model
trusts, such as the IDE, the open project, or the user's choice of interpreter, is a bug: we fix it in the open and
publish no advisory. Classify by who supplies the dangerous input, not by how bad the outcome sounds.

**In scope**

- The plugin setting or changing an interpreter without the user choosing the action.
- The plugin running a program while it decorates the project view; decoration only reads files.
- Crafted `pyvenv.cfg` or marker content making the plugin read or write outside the environment folder.

**Out of scope**

- What the IDE does with an interpreter after the user selects it: indexing and running it is the IDE's behavior.
- A malicious interpreter inside a project's virtual environment the user chose to use.
- Vulnerabilities in the IntelliJ Platform or the Python plugin.

If you cannot tell which side a finding falls on, report it privately and we will decide.
