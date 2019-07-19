#!/bin/sh

lapp callgraph src/test/resources/example_jars/com.company\$app\$3.2.jar
mv output/lapp.buf output/app.buf

lapp callgraph src/test/resources/example_jars/com.company\$core\$1.1.jar
mv output/lapp.buf output/core.buf

lapp callgraph src/test/resources/example_jars/com.company\$extension-a\$1.0.jar
mv output/lapp.buf output/ext.buf

lapp merge output/merged.buf output/app.buf output/ext.buf output/core.buf

lapp convert json output/app.buf output/app.json
lapp convert json output/ext.buf output/ext.json
lapp convert json output/core.buf output/core.buf

lapp convert udot output/merged.buf output/merged.dot
lapp convert json output/merged.buf output/merged.json
