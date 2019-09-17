#!/usr/bin/python3
import os
import sys
import subprocess
import shutil


if len(sys.argv) != 4:
    print("Usage: [input dir] [jar dir] [output dir]")
    sys.exit()

input_file = sys.argv[1]
output_folder = sys.argv[2]
result_folder = sys.argv[3]

debug = True

stdout = subprocess.DEVNULL

if debug:
    stdout = None

# Remove old files at output_file
if os.path.exists(output_folder):
    shutil.rmtree(output_folder)
os.mkdir(output_folder)

if not os.path.exists(result_folder):
    os.mkdir(result_folder)


def compile_folder(folder, root, level):
    for r in os.scandir(folder):
        if r.is_dir():
            print(f"{level*'  '}Subfolder {r.name}:")
            compile_folder(os.path.join(folder, r.name), root, level+1)
            print(f"{level*'  '}end Subfolder...")
        else:
            # Only interested in class files
            if not r.name.endswith('.class'):
                continue

            # but not inner classes
            if '$' in r.name:
                continue

            base_name = r.name[:-6]
            with_inner_classes = [x for x in os.scandir(folder) if x.is_file() and x.name.startswith(base_name + "$")]
            with_inner_classes.append(r)
            print(f"{level*'  '}Files: {', '.join([x.name for x in with_inner_classes])}")

            class_files = [os.path.relpath(x.path, root) for x in with_inner_classes]

            command = ['jar',  '-cf', os.path.join(testcase_output, base_name + '.jar')]

            for class_file in class_files:
                command.append("-C")
                command.append(os.path.join(root))
                command.append(class_file)

            print(f"{level*'  '}{command}")
            subprocess.call(command, stdout=stdout)


for testcase in os.listdir(input_file):
    if os.path.isdir(os.path.join(input_file, testcase)):

        print("#")
        print("# Test Case:", testcase)
        print("#")
        testcase_output = os.path.join(output_folder, testcase)
        os.mkdir(testcase_output)

        bin_folder = os.path.join(input_file, testcase, 'bin')
        print(bin_folder)
        compile_folder(bin_folder, bin_folder, 0)
        print("\n\n")

sys.exit()
print('#')
print('#')
print('#')
print('#')

for testcase in os.listdir(output_folder):
    testcase_dir = os.path.join(output_folder, testcase)
    if os.path.isdir(testcase_dir):
        print("Test Case: ", testcase)

        jar_files = [x.name for x in os.scandir(testcase_dir)]
        print(jar_files)

        merged_file = os.path.join(output_folder, testcase + "_merged.buf")
        resolved_file = os.path.join(output_folder, testcase + "_resolved.buf")
        single_file = os.path.join(output_folder, testcase + "_single.buf")

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


        merged_result = os.path.join(result_folder, testcase + "_merged.dot")
        single_result = os.path.join(result_folder, testcase + "_single.dot")

        xdot_command = ['lapp', 'convert', 'resolved', merged_file, merged_result]
        print(xdot_command)
        subprocess.call(xdot_command, stdout=stdout)

        xdot_command2 = ['lapp', 'convert', 'xdot', single_file, single_result]
        print(xdot_command2)
        subprocess.call(xdot_command2, stdout=stdout)



