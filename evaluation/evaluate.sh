#! /bin/bash

for filename in ./fingerprints/*.dot; do
    [ -e "$filename" ] || continue
    filebase=$(basename $filename .dot)
    echo $filebase


    ## Open the pdf
    xdot "compile_results/"$filebase"_single.dot" &
    ## Save the PID of evince
    pid="$!"
    ## Wait for a 1.5 seconds. This is to give the window time to
    ## appear. Change it to a higher value if your system is slower.
    sleep 0.5
    ## Get the X name of the evince window
    name1=$(wmctrl -lp | awk -vpid="$pid" '$3==pid{print $1}')
    ## Position the window
    wmctrl -ir "$name1" -t 3
    wmctrl -ir "$name1" -e 1,1,1440,3840,700

    xdot "compile_results/"$filebase"_merged.dot" &
    ## Save the PID of evince
    pid="$!"
    ## Wait for a 1.5 seconds. This is to give the window time to
    ## appear. Change it to a higher value if your system is slower.
    sleep 0.5
    ## Get the X name of the evince window
    name2=$(wmctrl -lp | awk -vpid="$pid" '$3==pid{print $1}')
    ## Position the window
    wmctrl -ir "$name2" -t 3
    wmctrl -ir "$name2" -e 1,1,720,3840,700

    xdot $filename &
    ## Save the PID of evince
    pid="$!"
    ## Wait for a 1.5 seconds. This is to give the window time to
    ## appear. Change it to a higher value if your system is slower.
    sleep 0.5
    ## Get the X name of the evince window
    name3=$(wmctrl -lp | awk -vpid="$pid" '$3==pid{print $1}')
    ## Position the window
    wmctrl -ir "$name3" -t 3
    wmctrl -ir "$name3" -e 1,1,1,3840,700

    meld $filename".sorted" "compile_results/"$filebase"_single.dot.sorted" &
    sleep 1.0
    pid="$!"
    ## Wait for a 1.5 seconds. This is to give the window time to
    ## appear. Change it to a higher value if your system is slower.
    echo $pid
    ## Get the X name of the evince window
    name4=$(wmctrl -lp | awk -vpid="$pid" '$3==pid{print $1}')
    ## Position the window
    wmctrl -ir "$name4" -t 4

    meld $filename".sorted" "compile_results/"$filebase"_merged.dot.sorted" &
    sleep 1.0
    pid="$!"
    ## Wait for a 1.5 seconds. This is to give the window time to
    ## appear. Change it to a higher value if your system is slower.
    ## Get the X name of the evince window
    #name=$(wmctrl -lp | awk -vpid="$pid" '$3==pid{print $1}')
    name5=$(wmctrl -lp | grep single.dot.sorted | cut -d " " -f1)
    ## Position the window
    wmctrl -ir "$name5" -t 5

    code -n tmp/$filebase/src &
    sleep 1.0
    pid="$!"
    ## Wait for a 1.5 seconds. This is to give the window time to
    ## appear. Change it to a higher value if your system is slower.
    ## Get the X name of the evince window
    name6=$(wmctrl -lp | awk -vpid="$pid" '$3==pid{print $1}')
    ## Position the window
    wmctrl -ir "$name6" -t 2
    wmctrl -ir "$name6" -b add,maximized_vert,maximized_horz


    read  -n 1 -p "Next.." mainmenuinput

    wmctrl -ic $name1
    wmctrl -ic $name2
    wmctrl -ic $name3
    wmctrl -ic $name4
    wmctrl -ic $name5
    wmctrl -ic $name6
done
