[![Build Status](https://api.travis-ci.org/bvlj/diab.svg)](https://travis-ci.org/bvlj/diab)
[![CircleCI](https://circleci.com/gh/bvlj/diab/tree/staging.svg?style=svg)](https://circleci.com/gh/bvlj/diab/tree/staging)
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

Clone the repository with git

```
git clone https://github.com/bvlj/diab
```

Setup the work environment

* Install git pre-push hooks (all the unit test must pass before pushing to master)
* Install ktlint (enforces code style guidelines)
* Prevent git from tracking changes to your trained models

```
./setup.sh
```

### Creating insulin suggestion models

To create your customized insulin suggestions models, see
the [readme found in the `ml` module](ml/Readme.md)
