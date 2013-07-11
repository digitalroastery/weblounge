#!/usr/bin/python

from xml.etree import ElementTree
from xml.dom import minidom
from xml.etree.ElementTree import Element, SubElement, Comment
import xml.etree.ElementTree as ET
import re
from sh import git

def prettify(elem):
    """Return a pretty-printed XML string for the Element.
    """
    rough_string = ElementTree.tostring(elem, 'utf-8')
    reparsed = minidom.parseString(rough_string)
    return reparsed.toprettyxml(indent="  ")


def MvnSettingProfile(profile,buildversion):
    """Return the Maven profile Maven XML elements required to inject build number
    """
    prof = SubElement(profs, 'profile')
    id = SubElement(prof, 'id')
    id.text = profile
    props = SubElement(prof, 'properties')
    buildNum = SubElement(props, 'build.number')
    buildNum.text = buildversion

def MvnSettingActiveProfile(profile):
    """Return the active profile Maven settings XML so they're built
    """
    activeProf = SubElement(activeProfs, 'activeProfile')
    activeProf.text = profile

#Create list of profiles by parsing pom.xml and filtering out capture,test profiles
# TODO Pass Jenkins workspace as arg to find the pom.xml file

#load the xml file
root = ET.parse("pom.xml")

#First find all profiles by specifying an Xpath
profiles_list = []

for profile in root.findall("./{http://maven.apache.org/POM/4.0.0}profiles/{http://maven.apache.org/POM/4.0.0}profile/{http://maven.apache.org/POM/4.0.0}id"):
    profiles_list.append(profile.text)

#remove capture profile and all profiles containing "test" string
ProfileList = []
for id in profiles_list:
  if not re.search('capture',id) and not re.search('test',id):
    ProfileList.append(id)



# Build number 
#get git commit short hash 
BuildTag = git("rev-parse","--short","HEAD")


#Generate maven-setting.xml file using the profiles parsed from the pom
# and the git commit short hash as build number

NameSpaceSettings = {'xmlns':'http://maven.apache.org/SETTINGS/1.0.0','xmlns:xsi':'http://www.w3.org/2001/XMLSchema-instance', 'xsi:schemaLocation':'http://maven.apache.org/SETTINGS/1.0.0 http:/    /maven.apache.org/xsd/settings-1.0.0.xsd'}

top = Element('settings', NameSpaceSettings)


comment = Comment('Generated Dynamically to be fed to Jenkins as a user maven settings file')
top.append(comment)

profs = SubElement(top, 'profiles')

for profile in ProfileList:
  MvnSettingProfile(profile,BuildTag)  


activeProfs = SubElement(top, 'activeProfiles')


for profile in ProfileList:
  MvnSettingActiveProfile(profile)


f = open('maven-settings.xml','wu')
f.write(prettify(top))
