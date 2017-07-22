TAG="mkdir-if-absent ---> "

targetDir=$1

echo -e "$TAG mkdir $targetDir\n"

if [ ! -d "$targetDir" ]; then
    mkdir "$targetDir"
fi