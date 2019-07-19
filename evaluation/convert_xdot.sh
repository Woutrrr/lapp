#!/bin/bash

jq '[.reachableMethods[] | select(.method.declaringClass | startswith("'$2'"))]' $1 |
jq  '[.[] | {source: .method, target: .callSites[].targets[]} ]' |
jq '[.[] | {source: .source | {declaringClass, name, params: .parameterTypes | join(","), returnType}, target: .target | {declaringClass, name, params: .parameterTypes | join(","), returnType}}]'
