#!/bin/bash
bin=`dirname "$0"`
bin=`cd "$bin"; pwd`
DIR=`cd $bin/../; pwd`
. "${DIR}/../bin/config.sh"
. "${DIR}/bin/config.sh"

# pre-running
DU ${INPUT_HDFS} SIZE 

#default Sort
APP=SimpleSort
JAR="${DIR}/target/KMeansApp-1.0.jar"
CLASS="src.main.scala.SimpleSort"
OPTION=" ${INOUT_SCHEME}${INPUT_HDFS} ${INOUT_SCHEME}${OUTPUT_HDFS} ${NUM_OF_CLUSTERS} ${MAX_ITERATION} ${STORAGE_LEVEL}"

if  [ $# -ge 1 ] && [ $1 = "WordCount" ]; then
	APP=SimpleWordCount
	CLASS="src.main.scala.SimpleWordCount"
    OPTION=" ${INOUT_SCHEME}${INPUT_HDFS} ${INOUT_SCHEME}${OUTPUT_HDFS} ${NUM_OF_CLUSTERS} ${NUM_OF_PARTITIONS} ${STORAGE_LEVEL}"
fi

echo "========== running ${APP} bench =========="

setup
for((i=0;i<${NUM_TRIALS};i++)); do
    RM ${OUTPUT_HDFS}
    purge_data "${MC_LIST}"	
    START_TS=`get_start_ts`;
    START_TIME=`timestamp`

    echo_and_run sh -c " ${SPARK_HOME}/bin/spark-submit --class $CLASS --master ${APP_MASTER} ${YARN_OPT} ${SPARK_OPT} ${SPARK_RUN_OPT} $JAR ${OPTION} 2>&1|tee ${BENCH_NUM}/${APP}_run_${START_TS}.dat"
    res=$?;

    END_TIME=`timestamp`
    get_config_fields >> ${BENCH_REPORT}
    print_config  ${APP} ${START_TIME} ${END_TIME} ${SIZE} ${START_TS} ${res}>> ${BENCH_REPORT};
done
teardown

exit 0

