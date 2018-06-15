# ML estimator model for insulin suggestions

## Setup

Required software:

* python3
* pip

Install dependencies

`pip install -r requirements.txt`

## Prepare the data for training

### Export data from the app

From the main menu, click on the export button to save train data to the sdcard.

#### Import using adb

Enable usb debugging on your device, then eun `bash import.sh` on your pc

#### Import manually

Copy the files from the sdcard to the out directory

#### Setup the tests

Fill the `data/test_*.csv` files with random data with right results (you can pick some from
the train files).

## Train the model and get the json result

The app uses some json files to look at references of the model results instead
of shipping the real model itself in order to allow lower-end devices to use this feature too.

All you need to do is execute the python script:

`python3 estimator.py`

## Save the data into the app

Copy the content of the export/estimator_*.json into the respective files in the res/raw folder

## Disable tracking git changes of sensitive files

```
 git update-index --skip-worktree ml/data/*
 git update-index --skip-worktree app/src/main/res/raw/*
```