#!/bin/bash
set -e

ROOT_DIR=/opt/hadoop/data/namenode

# ensure correct path (lowercase) exists
if [ ! -d "$ROOT_DIR/current" ]; then
    echo "Storage directory missing or empty at $ROOT_DIR. Creating and formatting NameNode..."
    mkdir -p "$ROOT_DIR"
    chown -R hadoop:hadoop "$ROOT_DIR" 2>/dev/null || true

    # non-interactive format (use -force -nonInteractive to avoid Y/N prompt)
    hdfs namenode -format -force -nonInteractive
fi

exec hdfs namenode