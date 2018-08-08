[![Build Status](https://api.travis-ci.org/bvlj/diab.svg)](https://travis-ci.org/bvlj/diab)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/5ba8b95a14d04075b86cce7ce71c46f0)](https://www.codacy.com/app/bvlj/diab?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=bvlj/diab&amp;utm_campaign=Badge_Grade)
[![ktlint](https://img.shields.io/badge/code%20style-%E2%9D%A4-FF4081.svg)](https://ktlint.github.io/)

Diab
=====

## Features

* Save records of glucose and insulins
* Google Fit integration (optional)
* Insulin suggestions

## License

Released under the [GNU GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt).

## Working with the repository

### Install git push hooks

Install git pre-push hooks to make sure code commited to
the master branch passess all the unit tests.

```
./install-git-hook.sh
```

### Install ktlint

Ktlint enforces code style guidelines.
Enable it to keep your contributions consistent to
the rest of the code 

```
curl -sSLO https://github.com/shyiko/ktlint/releases/download/0.27.0/ktlint && chmod +x ktlint
./ktlint --apply-to-idea-project --android
```

### Creating insulin suggestion models

To create your customized insulin suggestions models, see
the [readme found in the `ml` module](ml/Readme.md)
