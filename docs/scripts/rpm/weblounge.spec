%define 	_prefix /opt/weblounge
%define 	_data_prefix /var/lib/weblounge
%define 	__jar_repack 0

Name:		weblounge
Version:	CHANGE_ME_VERSION
Release:	CHANGE_ME_RELEASE
Summary:	Weblounge Web Content Management System
License:	LGPL 2.0	
URL:		http://entwinemedia.com	
BuildRoot:	%{_tmppath}/%{name}-%{version}-%{release}
Source:		weblounge.%{release}.tar.gz

# Baseline requirements
Requires: java-1.6.0-openjdk	
Requires: redhat-lsb

%description
Weblounge Web Content Management System

%prep

%setup -n weblounge.%{release}

%build

%pre

# First install
if [ "$1" = "1" ];then
  getent group weblounge >/dev/null || groupadd weblounge
  getent passwd weblounge >/dev/null || useradd -d /opt/weblounge -m -g weblounge weblounge -r -s /sbin/nologin -c "Weblounge System User"
  install -d -m 755 $RPM_BUILD_ROOT/var/log/weblounge

# Things to do before an update
elif [ "$1" = "2" ]; then
  #stop weblounge
  service weblounge stop >/dev/null 2>&1
  #clear caches
  rm -rf /var/cache/weblounge/* >/dev/null 2>&1
  rm -rf /var/tmp/weblounge/fileinstall-* >/dev/null 2>&1
  rm -rf /var/tmp/weblounge/xhtml-* >/dev/null 2>&1
fi

%install

# Work directories
mkdir -p $RPM_BUILD_ROOT/var/cache/weblounge
mkdir -p $RPM_BUILD_ROOT/var/tmp/weblounge
mkdir -p $RPM_BUILD_ROOT/var/log/weblounge

# Data directories
mkdir -p $RPM_BUILD_ROOT%{_data_prefix}/index
mkdir -p $RPM_BUILD_ROOT%{_data_prefix}/sites
mkdir -p $RPM_BUILD_ROOT%{_data_prefix}/sites-data

# Weblounge binaries
install -d -m 755 $RPM_BUILD_ROOT%{_prefix}
install -d -m 755 $RPM_BUILD_ROOT%{_initrddir}
install -d -m 755 $RPM_BUILD_ROOT/etc

install -D -m 644 bin/felix.jar $RPM_BUILD_ROOT%{_prefix}/bin/felix.jar
cp -rf  lib $RPM_BUILD_ROOT%{_prefix}

# Weblounge Start script
install -D -m 755 bin/weblounge $RPM_BUILD_ROOT%{_prefix}/bin/weblounge
install -D -m 755 bin/init $RPM_BUILD_ROOT%{_initrddir}/weblounge

cp -rf etc $RPM_BUILD_ROOT%{_prefix}

# Log rotation
mkdir -p $RPM_BUILD_ROOT/etc/logrotate.d
install -m644 docs/scripts/rpm/weblounge.logrotate $RPM_BUILD_ROOT/etc/logrotate.d/weblounge

%post

# First install only
if [ "$1" =  "1" ]; then
  ln -s /opt/weblounge/etc ${RPM_BUILD_ROOT}/etc/weblounge
  chkconfig --add weblounge
  chkconfig --level 235 weblounge on
elif [ "$1" =  "2" ];then
  #restart weblounge on updates
  service weblounge restart >/dev/null 2>&1
fi

%clean

rm -rf %{buildroot}

%files

%defattr(-,weblounge,weblounge,-)

#/etc/matterhorn
%{_initrddir}

/etc/logrotate.d/weblounge

%attr(0755,weblounge,weblounge) /opt/weblounge/
%attr(0755,weblounge,weblounge) /var/cache/weblounge
%attr(0755,weblounge,weblounge) /var/tmp/weblounge
%attr(0755,weblounge,weblounge) /var/log/weblounge
%attr(0755,weblounge,weblounge) %{_data_prefix}

### Weblounge Config files ###
%config(noreplace) /opt/weblounge/etc/

#Uninstall
%preun
#if [ "$1" = "0" ];then
# service matterhorn stop
#fi

%postun
if [ "$1" = "0" ];then
  userdel -r weblounge
  groupdel weblounge
  chkconfig --del weblounge
  rm -rf /etc/init.d/weblounge
  rm -rf /var/cache/weblounge
  rm -rf /var/tmp/weblounge
  rm /etc/weblounge
fi

%changelog
* Thu Sep 20 2012 Jaime Gago <jaime@entwinemedia.com> - 1.0
-Version 1
