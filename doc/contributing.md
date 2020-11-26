# Contributing

In order to contribute to this repository, certain guidelines are in place, to ensure that contributors follow a unified methodology.
This document describes key aspects:
1. How to develop locally
1. How to push a new feature branch
1. How to release a new version for staging and productio

## Workflow

This repository follow the GitFlow methodology for working. For more information see [here](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

**All** branches must follow this standard:
1. Feature branches are labelled `feature/\[name\]`
1. The staging branch is `develop`. **Never push feature changes to the develop branch directly**
1. The stable branch is `master`. **Never push to master directly**
1. Release branches are labelled `release/v\[version\]`
1. Hotfix branches are labelled `hotfix/\[name\]`

### Installing GitFlow

#### MacOS

1. Install [`Homebrew`](https://brew.sh/) if you don't have it.
1. Get the cask: `brew install git-flow`


#### Windows

TBD (Refer [here](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow))

#### Linux

TBD

## Local development

This section describes how to setup your working environment to develop features locally. If you already have the local repository cloned, skip to step (2)

1. Clone the repository: `git clone https://github.com/Haifa3D/haifa3d-hand-app.git`
1. Initialize GitFlow: `git flow init`
1. Move to the development branch: `git checkout develop`
1. To start a new feature, branch out from develop: `git checkout -b feature/\[name\]` or `git flow feature start \[name\]`

## Integrating a feature

**Note**: You can only manually merge if you have access writes. Other contributors must open a PR for merging `feature/\[name\]` --> `develop`.

When developing new features, develop them on their feature branch or on develop and then merge into master. When *master is checked out* then, run `nbgv prepare-release` (make sure to run this while beeing on master!). This bumps the version number and creates a new `vA.B+1` branch. Push that new branch to origin (and master too of course).

When you're done with your feature, follow these steps to integrate it:

1. Make sure all changes on your local branch are also pushed to the repository
1. Move to the `master` branch and update (`git pull`)
1. Run `nbgv prepare-release`