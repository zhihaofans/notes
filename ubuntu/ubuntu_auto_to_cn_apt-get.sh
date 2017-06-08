#!/bin/bash
echo "Backup '/etc/apt/sources.list' to '/etc/apt/sources.list_backup'"
cp /etc/apt/sources.list /etc/apt/sources.list_backup
rm /etc/apt/sources.list -f
echo "Choose your OS version"
echo "1:17.04"
echo "2:16.10"
echo "3:16.04"
echo "4:14.04"
read ver
if [ $ver -eq "1" ]
then
    downUrl="https://github.com/zhihaofans/notes/raw/master/ubuntu/17.04/sources.list"
elif [ $ver -eq "1" ]
then
    downUrl="https://github.com/zhihaofans/notes/raw/master/ubuntu/16.10/sources.list"
elif [ $ver -eq "1" ]
then
    downUrl="https://github.com/zhihaofans/notes/raw/master/ubuntu/16.04/sources.list"
elif [ $ver -eq "1" ]
then
    downUrl="https://github.com/zhihaofans/notes/raw/master/ubuntu/14.04/sources.list"
else
    echo "Error OS version,end"
    exit 1

wget -P /etc/apt/ $downUrl
echo "Finish"
