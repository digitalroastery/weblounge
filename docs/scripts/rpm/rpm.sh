#!/bin/bash
#
# This script is to be executed by Entwine Jenkins
# currently running at build.entwinemedia.com
# It takes 3 arguments that must be passed in the jenkins projects
# The arguments are, the jenkins workspace (see Jenkins doc)
# the customer name (used to create the customer repository)
# the customer rpm repository host FQDN
# the username for which ssh key payring has been configured for jenkins
# (this means /home/$username/.ssh/authorized_keys contains the jenkins public key
#
# With these 4 args this script will create the RPM package 
# that will be made available to the customer systems
# the archival of all builds but the latest 3 ones, 
# and the initialization of the mirroring of the repository at the customer site.

#usage jenkins-script.sh -w jenkins-workspace -c customer-name -m mirror-host-fqdn -u ssh-username

set -x 

# Script execution checks
echo "Time check: script starting"
date +%H\:%M

# Get Jenkins passed arguments
#
WORKSPACE=
CUSTOMER=
MIRROR_HOST=
SSH_USERNAME=
while getopts "c:w:m:u:" OPTION
do
     case $OPTION in
         w)
             WORKSPACE=$OPTARG
             ;;
         c)
             CUSTOMER=$OPTARG
             ;;
         m)
             MIRROR_HOST=$OPTARG
             ;;
         u)
             SSH_USERNAME=$OPTARG
             ;;
         ?)
             echo "Error in the script arguments exiting"
             exit 1
             ;;
esac
done

# Get the version and release
VERSION=3.1
RELEASE="$(git log -1 --pretty=format:"%ad %h" --date=short|sed s/'[[:space:]]'/."$(git log --oneline|wc -l)"git/|sed s/-//g)"
cd "$WORKSPACE"

# Sanity check for production default config files replacement
if [ ! -d "docs/scripts/rpm" ];then
  echo "docs/scripts/rpm does not exist, check the branch used for the checkout"
  exit 1
fi

# Set version and release tag for the package
sed -i s#CHANGE_ME_VERSION#"$VERSION"# "docs/scripts/rpm/weblounge.spec"
sed -i s#CHANGE_ME_RELEASE#"$RELEASE"# "docs/scripts/rpm/weblounge.spec"

# Time check
echo "Time check: starting the rpm building preparations"
date +%H\:%M

# Create a local user for this build
sudo useradd "$RELEASE"

# Remove what we don't want in the rpm
sudo rm -f "bin/start.sh"
sudo rm -f "bin/start.bat"

# Create the directory structure for putting together the RPMs
sudo su - "$RELEASE" -c "rpmdev-setuptree"
sudo su - "$RELEASE" -c "mkdir -p /home/$RELEASE/weblounge.$RELEASE"
sudo su - "$RELEASE" -c "mkdir -p /home/$RELEASE/rpmbuild/{SOURCES,SPECS}"

# Move the files in place for the creation of the RPMs
sudo cp -r "bin" /home/"$RELEASE"/weblounge."$RELEASE"
sudo cp -r "docs" /home/"$RELEASE"/weblounge."$RELEASE"
sudo cp -r "docs/scripts/rpm/contents/etc" /home/"$RELEASE"/weblounge."$RELEASE"
sudo cp -r "etc" /home/"$RELEASE"/weblounge."$RELEASE"
sudo cp -r "lib" /home/"$RELEASE"/weblounge."$RELEASE"

# Switch to the rpm build directory
sudo su - "$RELEASE" -c "cd ~"

# Create the sources RPM
sudo su - "$RELEASE" -c "tar -cvzf rpmbuild/SOURCES/weblounge.$RELEASE.tar.gz weblounge.$RELEASE"
sudo chown "$RELEASE" /home/"$RELEASE"/rpmbuild/SOURCES/weblounge."$RELEASE".tar.gz

# Move the spec file to the release directory
sudo cp "docs/scripts/rpm/weblounge.spec" /home/"$RELEASE"/rpmbuild/SPECS
sudo chown $RELEASE /home/"$RELEASE"/rpmbuild/SPECS/weblounge.spec

# Time check
echo "Time check: end of rpm preparations, starting the rpm build process"
date +%H\:%M

sudo su - "$RELEASE" -c "rpmbuild -ba rpmbuild/SPECS/weblounge.spec"
rpm_exit_code=$?
if [ ! $rpm_exit_code -eq 0 ];then
  echo "RPM Creation failed, check rpm log, exiting"
  sudo userdel -r "$RELEASE"
  exit 1
fi


#time check
echo "tTime check: package created repository operations starting"
date +%H\:%M

# Move the package to the repo (on Entwine infrastructure)
sudo chmod 777 -R /home/"$RELEASE"/rpm
sudo rm -rf /tmp/"$RELEASE"
mkdir /tmp/"$RELEASE"
chmod -R 777 /tmp/"$RELEASE"
sudo su "$RELEASE" -c "cp -r rpm/SRPMS/ /tmp/$RELEASE/"
sudo su "$RELEASE" -c "cp -r rpm/RPMS/* /tmp/$RELEASE/"
sudo mkdir -p /var/www/rpm-repos/$CUSTOMER/{SRPMS,RPMS}
sudo cp -r /tmp/$RELEASE/SRPMS/* /var/www/rpm-repos/$CUSTOMER/SRPMS/
sudo cp -r /tmp/$RELEASE/x86_64/entwine* /var/www/rpm-repos/$CUSTOMER/RPMS
sudo userdel -r "$RELEASE"

#Delete debuginfo packages from customer repo
sudo rm -f /var/www/rpm-repos/$CUSTOMER/RPMS/*debuginfo*.rpm

# Delete all packages but the 3 newest ones
cd /var/www/rpm-repos/$CUSTOMER/RPMS && sudo ls -t1|tail -n +4|sudo xargs rm -r
cd /var/www/rpm-repos/$CUSTOMER/SRPMS && sudo ls -t1|tail -n +4|sudo xargs rm -r

# Update the customer repo
sudo createrepo /var/www/rpm-repos/$CUSTOMER

#end of script
echo "Time check: script end"
date +%H:%M
exit 0
