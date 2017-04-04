#Run this script to get statistics from Model file
#!/bin/bash

current_dir=$(pwd)


class_file="$current_dir/target/classes/"

echo $class_file

if [$1 = ""];
then
    echo "Please provide input model file"
    exit 1
fi
echo $1

java -cp $class_file edu.isi.karma.util.KarmaStats $1