#!/usr/bin/env bash

docker run  --cpus=${1} --memory=${2} \
--ulimit nofile=64:64 \
--rm    \
--read-only \
-v /usr/execute/box/${3}:/usr/src/runbox   \
-w /usr/src/runbox atishaya/judge-worker-${4}  \
bash -c "/bin/compile.sh && /bin/run.sh ${5}"

