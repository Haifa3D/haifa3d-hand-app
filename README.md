<p align="center">
  <a href="#">
    <img
      alt="Haifa 3D app logo"
      src="doc/logo.svg"
      width="100"
    />
  </a>
</p>

# Haifa 3D

[![Build Status](https://dev.azure.com/georg-jung/Haifa3d/_apis/build/status/georg-jung.technion-robotic-arm?branchName=master)](https://dev.azure.com/georg-jung/Haifa3d/_build/latest?definitionId=12&branchName=master)

<a href='https://play.google.com/store/apps/details?id=com.gjung.haifa3d'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' height="60"/></a>

This repository tracks the development of an Android app that enables users of Haifa 3D's artificial hands to control and configure their prosthesis. To achieve that the application connects to the hand's controller via Bluetooth Low Energy.

The scope of this project begins with the defintion of the BTLE based protocol and includes the deployment of a fully functional Android application to the Play Store that is usable by the end users.

See the [programmer's app manual](/doc/programmers-manual.md) for an introduction to the project from a technical & code point of view.

## BLE-API
This repository tracks the development of an Android BLE library that enables programers to use of BLE service, as well as other services which are available in the 
Haifa 3D app.

See the [installation BLE-API](/doc/installation-BLE-API.md) for BLE-API installation requirements.
See the [programmer's BLE-API manual](/doc/programmers-manual-BLE-API.md) for an intoduction to the BLE-API library

## Releasing

In `vA.B.C`, increasing A means a breaking change or (as this is a user facing app and not a lib) any big reqork etc.. Increasing B means new features. Increasing C means fixes. Public releases have versions like `vA.B.C`, while beta releases etc. follow the `vA.B.C-beta`-scheme.  See [SemVer 2](https://semver.org/) and [NBGV](https://github.com/dotnet/Nerdbank.GitVersioning) for details.

Builds from `release/*` branches are signed using the Play Store upload certificate in [CI](https://dev.azure.com/georg-jung/Haifa3d/_build). Additionally, they are also pushed to GitHub releases as draft release. All other branches/refs are build the same way but not signed and not deployed. So, to push an update to the Play Store:

1. Set up development machine
    1. Install [.Net Core runtime](https://dotnet.microsoft.com/download) (it's best to use [chocolatey](https://chocolatey.org/install): [`choco install dotnetcore`](https://chocolatey.org/packages/dotnetcore) or winget)
    2. Install [nbgv](https://github.com/dotnet/Nerdbank.GitVersioning/blob/master/doc/nbgv-cli.md): `dotnet tool install -g nbgv`
2. Develop feature or fix
    * For hotfixing, develop on the corresponding `release/vA.B` branch directly or merge the hotfix branch there. Make sure to merge the `release/v*` branch into master afterwards. No release branch should ever be ahead of master! These releases will increase the version number in the third positon (in `vA.B.X`, X changes).
    * When developing new features, develop them on their feature branch or on develop and then merge into master. When *master is checked out* then, run `nbgv prepare-release` (make sure to run this while beeing on master!). This bumps the version number and creates a new `vA.B+1` branch. Push that new branch to origin (and master too of course).
3. All public releases come from `release/v*` branches. If you just updated such a branch, CI will build the app, sign it using the Play Store upload certificate and create a draft GitHub release (builds from non-release branches are not signed and published to GitHub and have an `-alpha`/`-beta`/etc suffix in their version number).
4. Publish the automatically created GitHub release (promote it from beeing a draft).
5. Download the AAB file from the GitHub release and upload that to the Play Store.

## Screenshots

![Live Control](/doc/screenshot_livecontrol.png)

![Connect](/doc/screenshot_connect.png)

## Legal

Google Play and the Google Play logo are trademarks of Google LLC.

## Donation:
[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/donate?hosted_button_id=GGFJLZDB9GWXE)
