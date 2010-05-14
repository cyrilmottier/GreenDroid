#! /usr/bin/env python
# encoding: utf-8

"""
Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
"""

import os
import shutil
import sys
import fileinput
import re
import scriptutil

VERSION = 1.0
GD_SRC = '../src'
GD_RES = '../res'

def gd_apply_tree(src, dst):
  names = os.listdir(src)
  
  try:
    os.makedirs(dst)
  except OSError:
    #print('The directory ' + dst + ' already exists')
    pass
    
  for name in names:
    srcname = os.path.join(src, name)
    dstname = os.path.join(dst, name)
    
    if os.path.isdir(srcname):
      #print('gd_apply_tree(' + srcname + ', ' + dstname + ')')
      gd_apply_tree(srcname, dstname)
    else:
      #print('shutil.copy2(' + srcname + ', ' + dstname + ')')
      shutil.copy2(srcname, dstname)
  

  

def gd_apply(path):
  project = os.listdir(path)
  #print(repr(project))
  
  if project.__contains__('AndroidManifest.xml'):
    print('Applying Greendroid to ' + path)
    
    """
    I think parsing XML would be better because the following regular expression sucks.
    Unfortunatly I'm a beginner at Python ...
    """
    
    package_name = '';
    for line in fileinput.input(path + '/AndroidManifest.xml'):
      res = re.search("package=\"(?P<package_name>[\w\.]+)\"", line)
      if res != None:
        package_name = res.groupdict()['package_name']
    
    if package_name == '':
      print('An error occured while parsing: no package tag found in the AndroidManifest.xml')
      exit()
    else:
      
      """
      We're now ready to apply files from the GreenDroid project to the project pointed 
      by 'path'. The process consist on copying some files from the GreenDroid project and
      replace all occurences to R.java:
      
      'import com.cyrilmottier.android.greendroid.R' is replaced by 'import <package_name>.R'
      'xmlns:greendroid="http://schemas.android.com/apk/res/com.cyrilmottier.android.greendroid"' is replaced by 'xmlns:greendroid="http://schemas.android.com/apk/res/<package_name>"'
      """
      
      # First of all, let's copy java files
      srcdest = os.path.join(path, "src")
      gd_apply_tree(GD_SRC, srcdest)
      # All java files have been copied so replace previously explained occurences
      scriptutil.freplace(srcdest, regexl=(('import com.cyrilmottier.android.greendroid.R;', 'import ' + package_name + '.R;', None),))
      
      # We can now import all copy all files fro
      resdest = os.path.join(path, "res")
      gd_apply_tree(GD_RES, resdest)
      # All resource files have been copied so replace previously explained occurences
      scriptutil.freplace(srcdest, regexl=(('xmlns:greendroid="http://schemas.android.com/apk/res/com.cyrilmottier.android.greendroid"', 'xmlns:greendroid="http://schemas.android.com/apk/res/' + package_name + '"', None),))
      
      print('Greendroid successfully applied to ' + path)


    #shutil.copytree(GD_SRC, path + 'src')
  else:
    print(path + 'does not point to a valid Android project')





#print (repr(sys.argv))

argvLen = len(sys.argv)

if argvLen <= 1:
  print('Usage: greendroid <cmd> <options>')
  exit()
  
cmd = sys.argv[1]

if cmd == "apply":
  
  if (argvLen <= 2):
    print('Usage: greendroid apply <project_path>')
  else:
    gd_apply(sys.argv[2]);
    
elif cmd == "version":
  print('GreenDroid v' + repr(VERSION))
else:
  print('Unknown command ' + sys.argv[1])
  print('Available commands are: \'apply\', \'version\'')
