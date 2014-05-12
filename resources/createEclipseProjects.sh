#!/bin/bash

#get the repository name
GIT_EXTENSION=".git"
if [[ -z "$1" ]]; then
    read -p"input respoitory name to extract ($GIT_EXTENSION will be appended \
automatically if forgotten): " -e repo
else
  repo=$1
fi

if [[ -z "$repo" ]]; then
  echo "no repo inputted, exiting"
  exit 1
fi

#rename varaibles with(out) extension
if [[ $repo != *$GIT_EXTENSION ]]; then
  repoGit="$repo$GIT_EXTENSION"
else
  repoGit="$repo"
  repo=${repo%"$GIT_EXTENSION"}
fi

#clone folders
echo "cloning repository named $repoGit in all children directories"
projectFolders=()
CWD=`pwd`

DIRCOUNT=0

for i in `find . -type d -maxdepth 1`; do
  cd $i
  if [[ -e "$repoGit" ]]; then
    git clone "$repoGit" 2> /dev/null
    projectFolders+=("$i")
    ((DIRCOUNT++))
  fi
  cd $CWD
done

cp GENERIC_POM.xml pom.xml
sed -ie s/REPLACE/"$repo"/ pom.xml

BUILDCOUNT=0

#create eclipse projects
for folder in "${projectFolders[@]}"; do
  cd $folder
  cp $CWD/pom.xml pom.xml
  mvn compile 1> /dev/null
  mvn eclipse:eclipse 1> /dev/null
  projectName="${repo}_${PWD##*/}"
  echo "built project $projectName"

  sed -ie s/$repo/$projectName/ .project
  cd $CWD

  ((BUILDCOUNT++))
  PCT=$(awk -v x=$BUILDCOUNT -v y=$DIRCOUNT 'BEGIN { print (x/y)*100 }')
  echo "About ${PCT}% complete"
done

rm pom.xml
rm pom.xmle
rm **/.projecte 2> /dev/null

echo "DONE!"
