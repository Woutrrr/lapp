#!/usr/bin/env python3
import sys
import subprocess
import os

if (len(sys.argv) != 3):
    print("Usage: [input dir] [output dir]")
    sys.exit()

dir = sys.argv[1]
output = sys.argv[2]


for d in os.scandir(dir):

    if not d.is_dir():
        continue
    # print("\n")
    print(d.name)
    scope = "L" + d.name[:2].lower()
    callgraph = os.path.join(d.path, "WALA", "RTA", "cg.json")

    command = ['./convert_xdot.sh', callgraph, scope]
    # print(command)
    extract_command = subprocess.Popen(command, stdout=subprocess.PIPE)

    command = ['./json2dot.py']
    with open(os.path.join(output, d.name + ".dot"), 'w') as fp:
        json_exec = subprocess.Popen(command, stdin=extract_command.stdout, stdout=fp)
        json_exec.wait()




