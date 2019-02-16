#!/usr/bin/env bash
#!/usr/bin/env bash


cd /usr/execute/box/${3}
cp /usr/execute/compilers/${4}/compile.sh /usr/execute/box/${3}
cp /usr/execute/compilers/${4}/run.sh /usr/execute/box/${3}

chmod 777 compile.sh
chmod 777 run.sh
./compile.sh && ./run.sh ${5}