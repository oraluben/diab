#!/usr/bin/env bash

function install_pre_push() {
  cp "pre-push.sh" ".git/hooks/pre-push"
  chmod +x ".git/hooks/pre-push"
}

function install_ktlint() {
  echo "Downloading ktlint..."
  curl -sSLO https://github.com/shyiko/ktlint/releases/download/0.31.0/ktlint
  chmod a+x ktlint
  ./ktlint --apply-to-idea-project --android -y
  ./ktlint --install-git-pre-commit-hook
  sed -i "s:xargs ktlint:xargs ./ktlint:" .git/hooks/pre-commit
}

function disable_git_track_changes() {
  git update-index --skip-worktree $1
  git update-index --assume-unchanged $1
}

function assume_models_unchanged() {
  disable_git_track_changes ml/export
  disable_git_track_changes ml/data/test_*.csv
  disable_git_track_changes ml/data/train_*.csv
}

install_pre_push && install_ktlint && assume_models_unchanged
