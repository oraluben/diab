#!/usr/bin/env bash

function install_pre_push() {
  cp "pre-push.sh" ".git/hooks/pre-push"
  chmod +x ".git/hooks/pre-push"
}

function install_ktlint() {
  echo "Downloading ktlint..."
  curl -sSLO https://github.com/shyiko/ktlint/releases/download/0.27.0/ktlint
  chmod +x ktlint
  ./ktlint --apply-to-idea-project --android -y
}

function disable_git_track_changes() {
  git update-index --skip-worktree $1
  git update-index --assume-unchanged $1
}

function assume_models_unchanged() {
  disable_git_track_changes plugin/src/main/assets/estimator_*.json
  disable_git_track_changes ml/data/test_*.csv
  disable_git_track_changes ml/data/train_*.csv
}

install_pre_push && install_ktlint && assume_models_unchanged
