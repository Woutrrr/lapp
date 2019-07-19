#! /usr/bin/python3
import os
import sys
import subprocess
import shutil


if (len(sys.argv) != 4):
    print("Usage: [input dir] [jar dir] [output dir]")
    sys.exit()

input = sys.argv[1]
output = sys.argv[2]
result_output = sys.argv[3]

debug = True

stdout = subprocess.DEVNULL

if debug:
    stdout = None


if (os.path.exists(output)):
    shutil.rmtree(output)
os.mkdir(output)

if (not os.path.exists(result_output)):
    os.mkdir(result_output)
def compile_folder(bin_folder):
    for r, d, f in os.walk(bin_folder):
        print(r, d, f)

        for file in f:
            if '.class' in file:
                print(r, d, f)
                command = ['jar',  '-cf', os.path.join(testcase_output, file[:-6] + '.jar'), '-C', os.path.join(bin_folder), os.path.join(dir.name, file)]
                print(command)
                # subprocess.call(command)
    return

    for r in os.scandir(bin_folder):
        # print(f"$$____{dir.name}____$$")



        if r.is_dir():
            print(f"Dir: {r.name}")
            print(f"Subfolder:")
            compile_folder(os.path.join(bin_folder, r.name))
            print(f"end Subfolder...")
        else:
            print(f"File: {r.name}")
        # for file in f:
            #     if '.class' in file:
            #         print(r, d, f)
            #         command = ['jar',  '-cf', os.path.join(testcase_output, file[:-6] + '.jar'), '-C', os.path.join(bin_folder), os.path.join(dir.name, file)]
            #         print(command)
            #         subprocess.call(command)

for testcase in os.listdir(input):
    if os.path.isdir(os.path.join(input, testcase)):

        print("#")
        print("# Test Case:", testcase)
        print("#")
        testcase_output = os.path.join(output, testcase)
        os.mkdir(testcase_output)

        bin_folder = os.path.join(input, testcase, 'bin')
        for r, d, f in os.walk(bin_folder):


            for file in f:
                if '.class' in file:
                    relative_path = os.path.relpath(r, bin_folder)
                    jar_path = os.path.join(testcase_output, file[:-6] + '.jar')
                    if os.path.exists(jar_path):
                        print(f"!!!!!!!!!!!!!!!!! Overwriting {jar_path}")
                    command = ['jar',  '-cf', jar_path, '-C', bin_folder, os.path.join(relative_path, file)]
                    print(command)
                    subprocess.call(command)



print('#')
print('#')
print('#')
print('#')

for testcase in os.listdir(output):
    testcase_dir = os.path.join(output, testcase)
    if os.path.isdir(testcase_dir):
        print("Test Case: ", testcase)

        jar_files = [x.name for x in os.scandir(testcase_dir)]
        print(jar_files)

        merged_file = os.path.join(output, testcase + "_merged.buf")
        resolved_file = os.path.join(output, testcase + "_resolved.buf")
        single_file = os.path.join(output, testcase + "_single.buf")

        merge_command = ['lapp', 'merge', merged_file]
        single_command = ['lapp', 'callgraph', '-o', single_file]

        # individual
        for jar_file in jar_files:
            jar_file_path = os.path.join(testcase_dir, jar_file)
            buf_file_path = jar_file_path[:-4] + ".buf"
            command = ['lapp', 'callgraph', "-o", buf_file_path,  jar_file_path]
            subprocess.call(command, stdout=subprocess.DEVNULL)
            print(command)
            merge_command.append(buf_file_path)
            single_command.append(jar_file_path)

        print(merge_command)
        subprocess.call(merge_command, stdout=stdout)

        print(single_command)
        subprocess.call(single_command, stdout=stdout)


        merged_result = os.path.join(result_output, testcase + "_merged.dot")
        single_result = os.path.join(result_output, testcase + "_single.dot")

        xdot_command = ['lapp', 'convert', 'resolved', merged_file, merged_result]
        print(xdot_command)
        subprocess.call(xdot_command, stdout=stdout)

        xdot_command2 = ['lapp', 'convert', 'xdot', single_file, single_result]
        print(xdot_command2)
        subprocess.call(xdot_command2, stdout=stdout)



