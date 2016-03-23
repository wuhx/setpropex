#!/system/bin/sh
SELF=$(pwd)/$0
APP_TMP_DATA=$SELF.data
if [[ ! -d $APP_TMP_DATA ]]; then
  mkdir -p $APP_TMP_DATA
  tail -n +10 $SELF | tar -x -C $APP_TMP_DATA
fi
cd $APP_TMP_DATA && ./run.sh $1 $2
exit 0
