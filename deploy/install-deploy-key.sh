#!/usr/bin/env bash
# One-time setup on the VPS for the Deploy workflow. Run from the home directory after copying deploy.sh and the
# public deploy key there:  bash install-deploy-key.sh
# Installs deploy.sh as ~/bin/portfolio-deploy and lets the deploy key run that command and nothing else.
set -euo pipefail
mkdir -p ~/bin
install -m 755 ~/deploy.sh ~/bin/portfolio-deploy
pub="$(cut -d' ' -f1,2 ~/portfolio_deploy.pub)"
if grep -qF "$pub" ~/.ssh/authorized_keys; then
	echo "deploy key already installed"
else
	echo "restrict,command=\"$HOME/bin/portfolio-deploy\" $pub github-actions-deploy" >> ~/.ssh/authorized_keys
	echo "deploy key installed (can only run ~/bin/portfolio-deploy)"
fi
rm ~/deploy.sh ~/portfolio_deploy.pub
