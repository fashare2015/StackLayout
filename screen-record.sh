TAG="screen-record ---> "

OUTPUT_DIR="./screen-record/"
MP4_POSTFIX=".mp4"
GIF_POSTFIX=".gif"

fileName=$1

# [Required] Get gif $fileName from 1st arg: . Or else do nothing.
if [ "$fileName" != "" ]
then
    echo -e "$TAG specify fileName: $fileName\n"

    mobilePath="/sdcard/$fileName"
    pcPath="$OUTPUT_DIR$fileName"

    # [Optional] Get gif $size from 2nd arg: . default size: "240x400".
    size="240x400"
    if [ "$2" != "" ]
    then
        size=$2
        echo -e "$TAG specify size: $size\n"
    fi

    # [Optional] Get gif $time from 3rd arg. default time: 8s.
    time=8
    if [ "$3" != "" ]
    then
        time=$3
        echo -e "$TAG specify limited time: $time\n"
    fi

    # [Optional] Get $ffmpegPath from 4th arg. default ffmpegPath: "ffmpeg".
    ffmpegPath="ffmpeg"
    if [ "$4" != "" ]
    then
        ffmpegPath=$4
        echo -e "$TAG specify ffmpegPath: $ffmpegPath\n"
    fi

    # Make output dir if absent.
    chmod 777 ./mkdir-if-absent.sh
    ./mkdir-if-absent.sh $OUTPUT_DIR

    # Execute cmd screenrecord and then you get a video (XXX.mp4) in your sdcard.
    echo -e "$TAG begin screenrecord\n"
    adb shell screenrecord "$mobilePath$MP4_POSTFIX" --time-limit $time
    echo -e "$TAG end screenrecord\n"

    # Upload the video into OUTPUT_DIR of your PC .
    adb pull "$mobilePath$MP4_POSTFIX" "$pcPath$MP4_POSTFIX"

    # Remove target gif if exists
    if [ -f "$pcPath$GIF_POSTFIX" ]; then
        rm "$pcPath$GIF_POSTFIX"
    fi

    # Convert the video (XXX.mp4) to a gif (XXX.gif).
    $ffmpegPath -r 20 -i "$pcPath$MP4_POSTFIX" -s $size -b:v 1500k "$pcPath$GIF_POSTFIX"

    if [ $? -eq 0 ]; then
        # Delete the redundant file (XXX.mp4).
        rm "$pcPath$MP4_POSTFIX"

    else
        echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
        echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
        echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
        echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"

        echo -e "$TAG ffmpegPath not found!!!\n"
        echo -e "Make sure you have installed ffmpeg!\n"
        echo -e "Please specify the ffmpegPath by \"FFMPEG_PATH=XXX\" in gradle.properties.\n"

        echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
        echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
        echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"
        echo -e "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n"

        exit 1
    fi

else
    echo -e "$TAG PARAM ERROR! Please specify a fileName (without postfix)!\n"
fi
