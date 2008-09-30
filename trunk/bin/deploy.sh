#!/bin/bash
#
# Convenience script to expedite installation into a remote maven repository.
#
# Deprecated: rendered obsolete by configuring maven itself to do the deployment.
#
# @Author: Ovidiu Feodorov <ovidiu@feodorov.com>
#

reldir=`dirname $0`

host=a
user=b
path=/var/www/html/maven/repositories/external/releases/third-party

function figure_out_version_to_deploy()
{
    target_dir=$reldir/../target;

    if [ ! -d $target_dir ]; then
        echo "" 1>&2;
        echo "ERROR: no $target_dir directory, release not ready for deployment" 1>&2;
        exit 1;
    fi

    cnt=0;
    for i in `ls $target_dir/hba-*.jar`; do
        cnt=`expr $cnt + 1`
        name=`basename $i`
        name=`echo $name | sed -e 's/hba-//' | sed -e 's/\.jar//'`
        if echo $name | grep "-sources" > /dev/null; then
            name=`echo $name | sed -e 's/-sources//'`
        fi
        if [ "$version" = "" ]; then
            version=$name;
        elif [ "$version" != "$name" ]; then
            echo "" 1>&2;
            echo "ERROR: inconsistent versions $version, $name" 1>&2;
            exit 1;
        fi
    done

    if [ $cnt != 2 ]; then
        echo "ERROR: no jar/sources jar pair found in $target_dir" 1>&2;
        exit 1;
    fi

    echo $version;
}

# $1 - version to deploy
# $2 - remote host name
# $3 - user to perform the deployment on remote host
function read_extra_confirmation()
{
    echo ""
    echo -n "Are you sure you want to deploy $1 to $3@$2? [y|n] "
    read decision
    if [ "$decision" != "y" ]; then
        echo "exiting ..."
        exit 1
    fi
}

# $1 - version
function create_pom()
{
    echo "creating hba-$1.pom ..."
    echo "future pom" > ./hba-$1.pom
}

# $1 - remote host name
# $2 - user to perform the deployment on remote host
# $3 - remote path
# exits with a non-zero value on failure
function create_remote_directory()
{
    echo "creating remote directory $2@$1:$3 ..."
    if ! ssh $2@$1 mkdir $3; then
        echo "failed to create $2@$1:$3" 1>&2;
        exit 1;
    else
        echo "directory created"
    fi
}

# $1 - version to deploy
# $2 - remote host name
# $3 - user to perform the deployment on remote host
# $4 - remote path
function deploy()
{
    echo ""
    echo "deploying ..."
    create_pom $1;
    create_remote_directory $2 $3 $4/hba/hba/$1
}

#
# main
#

if ! version=`figure_out_version_to_deploy`; then
    exit 1;
fi

read_extra_confirmation $version $host $user;

deploy $version $host $user $path;


 











