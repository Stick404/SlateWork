# hexdoc-slate-work

<a href="https://github.com/hexdoc-dev/hexdoc"><img src="https://img.shields.io/endpoint?url=https://hexxy.media/api/v0/badge/hexdoc?label=1" alt="powered by hexdoc" style="max-width:100%;"></a>

[hexdoc](https://pypi.org/project/hexdoc) plugin for Slate Work's web book.

## Version scheme

We use [hatch-gradle-version](https://pypi.org/project/hatch-gradle-version) to generate the version number based on whichever mod version the docgen was built with.

The version is in this format: `mod-version.python-version.mod-pre.python-dev.python-post`

For example:
* Mod version: `0.11.1-7`
* Python package version: `1.0.dev0`
* Full version: `0.11.1.1.0rc7.dev0`

## Setup

Install Python 3.11 and Node 18 (20+ is **not** currently supported).

```sh
python3.11 -m venv venv

.\venv\Scripts\activate   # Windows
. venv/bin/activate.fish  # fish
source venv/bin/activate  # everything else

# run from the repo root, not doc/
pip install -e .[dev]
```

## Usage

For local testing, create a file called `.env` in the repo root following this template:
```sh
GITHUB_REPOSITORY=Stick404/SlateWork
GITHUB_SHA=main
GITHUB_PAGES_URL=https://stick404.github.io/SlateWork/
```

Useful commands:
```sh
# show help
hexdoc -h

# build, merge, and serve the web book in watch mode
nodemon --config doc/nodemon.json

# build, merge, and serve the web book
hexdoc serve

# build and merge the web book
hexdoc build
hexdoc merge

# start a Python interpreter with some extra local variables
hexdoc repl
```
