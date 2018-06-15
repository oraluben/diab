#!/usr/bin/env bash


readonly OUT_DIRECTORY="out"


function has_bin() {
  which $1 &> /dev/null
  return $?
}


function cleanup_out() {
  rm -rf OUT_DIRECTORY/1 && rm -rf OUT_DIRECTORY/3 && rm -rf OUT_DIRECTORY/5
  return $?
}


##
# Main
#

bash import.sh

if [[ $(has_bin python3) -ne 0 ]]; then
  echo "python3 not found!"
  exit 1
fi


cleanup_out &> /dev/null
python3 estimator.py