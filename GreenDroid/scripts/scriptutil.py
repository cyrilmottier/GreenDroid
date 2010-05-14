#!/usr/bin/env python
# encoding: utf-8

"""
Copyright (c) 2007, Muharem Hrnjadovic

All rights reserved.

Redistribution and use in source and binary forms, with or without 
modification, are permitted provided that the following conditions
are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of Muharem Hrnjadovic nor the names of other
      contributors may be used to endorse or promote products derived from
      this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

---------------------------------------------------------------------------

Module providing functions commonly used in shell scripting:

  - ffind()    : finds files in a directory tree
  - ffindgrep(): finds files in a directory tree and matches their
                 content to regular expressions
  - freplace() : in-place search/replace of files in a directory tree
                 with regular expressions
  - printr()   : prints the results of the ffind()/ffindgrep() functions

Please see the documentation strings of the particular functions for
detailed information.
"""

# Copyright: (c) 2007 Muharem Hrnjadovic
# created: 15/04/2007 09:31:25

__version__ = "$Id:$"
# $HeadURL $

import os, sys, types, re, fnmatch, itertools

class ScriptError(Exception): pass

def ffind(path, shellglobs=None, namefs=None, relative=True):
    """
    Finds files in the directory tree starting at 'path' (filtered by
    Unix shell-style wildcards ('shellglobs') and/or the functions in
    the 'namefs' sequence).

    The parameters are as follows:

    - path: starting path of the directory tree to be searched
    - shellglobs: an optional sequence of Unix shell-style wildcards
      that are to be applied to the file *names* found
    - namefs: an optional sequence of functions to be applied to the
      file *paths* found
    - relative: a boolean flag that determines whether absolute or
      relative paths should be returned

    Please not that the shell wildcards work in a cumulative fashion
    i.e. each of them is applied to the full set of file *names* found.

    Conversely, all the functions in 'namefs'
        * only get to see the output of their respective predecessor
          function in the sequence (with the obvious exception of the
          first function)
        * are applied to the full file *path* (whereas the shell-style
          wildcards are only applied to the file *names*)

    Returns a sequence of paths for files found.
    """
    if not os.access(path, os.R_OK):
        raise ScriptError("cannot access path: '%s'" % path)

    fileList = [] # result list
    try:
        for dir, subdirs, files in os.walk(path):
            if shellglobs:
                matched = []
                for pattern in shellglobs:
                    filterf = lambda s: fnmatch.fnmatchcase(s, pattern)
                    matched.extend(filter(filterf, files))
                fileList.extend(['%s%s%s' % (dir, os.sep, f) for f in matched])
            else:
                fileList.extend(['%s%s%s' % (dir, os.sep, f) for f in files])
        if not relative: fileList = map(os.path.abspath, fileList)
        if namefs: 
            for ff in namefs: fileList = filter(ff, fileList)
    except Exception, e: raise ScriptError(str(e))
    return(fileList)

def ffindgrep(path, regexl, shellglobs=None, namefs=None, 
              relative=True, linenums=False):
    """
    Finds files in the directory tree starting at 'path' (filtered by
    Unix shell-style wildcards ('shellglobs') and/or the functions in
    the 'namefs' sequence) and searches inside these.

    The parameters are as follows:

    - path: starting path of the directory tree to be searched
    - shellglobs: an optional sequence of Unix shell-style wildcards
      that are to be applied to the file *names* found
    - namefs: an optional sequence of functions to be applied to the
      file *paths* found
    - relative: a boolean flag that determines whether absolute or
      relative paths should be returned
    - linenums: turns on line numbers for found files (like grep -n)

    Additionaly, the file content will be filtered by the regular
    expressions in the 'regexl' sequence. Each entry in the latter
    is a
    
      - either a string (with the regex definition)
      - or a tuple with arguments accepted by re.compile() (the
        re.M and re.S flags will have no effect though)

    For all the files that pass the file name/content tests the function
    returns a dictionary where the

      - key is the file name and the
      - value is a string with lines filtered by 'regexl'
    """
    fileList = ffind(path, shellglobs=shellglobs, 
                     namefs=namefs, relative=relative)
    if not fileList: return dict()

    result = dict()

    try:
        # first compile the regular expressions
        ffuncs = []
        for redata in regexl:
            if type(redata) == types.StringType:
                ffuncs.append(re.compile(redata).search)
            elif type(redata) == types.TupleType:
                ffuncs.append(re.compile(*redata).search)
        # now grep in the files found
        for file in fileList:
            # read file content
            fhandle = open(file, 'r')
            fcontent = fhandle.read()
            fhandle.close()
            # split file content in lines
            if linenums: lines = zip(itertools.count(1), fcontent.splitlines())
            else: lines = fcontent.splitlines()
            for ff in ffuncs:
                if linenums: lines = filter(lambda t: ff(t[1]), lines)
                else: lines = filter(ff, lines)
                # there's no point in applying the remaining regular
                # expressions if we don't have any matching lines any more
                if not lines: break
            else:
                # the loop terminated normally; add this file to the
                # result set if there are any lines that matched
                if lines:
                    if linenums:
                        result[file] = '\n'.join(["%d:%s" % t for t in lines])
                    else:
                        result[file] = '\n'.join(map(str, lines))
    except Exception, e: raise ScriptError(str(e))
    return(result)

def freplace(path, regexl, shellglobs=None, namefs=None, bext='.bak'):
    """
    Finds files in the directory tree starting at 'path' (filtered by
    Unix shell-style wildcards ('shellglobs') and/or the functions in
    the 'namefs' sequence) and performs an in-place search/replace
    operation on these.

    The parameters are as follows:

    - path: starting path of the directory tree to be searched
    - shellglobs: an optional sequence of Unix shell-style wildcards
      that are to be applied to the file *names* found
    - namefs: an optional sequence of functions to be applied to the
      file *paths* found
    - relative: a boolean flag that determines whether absolute or
      relative paths should be returned

    Additionally, an in-place search/replace operation is performed
    on the content of all the files (whose names passed the tests)
    using the regular expressions in 'regexl'.

    Please note: 'regexl' is a sequence of 3-tuples, each having the
    following elements:

      - search string (Python regex syntax)
      - replace string (Python regex syntax)
      - regex flags or 'None' (re.compile syntax)

    Copies of the modified files are saved in backup files using the
    extension specified in 'bext'.

    The function returns the total number of files modified.
    """
    fileList = ffind(path, shellglobs=shellglobs, namefs=namefs)

    # return if no files found
    if not fileList: return 0

    filesChanged = 0

    try:
        cffl = []
        for searchs, replaces, reflags in regexl:
            # prepare the required regex objects, check whether we need
            # to pass any regex compilation flags
            if reflags is not None: regex = re.compile(searchs, reflags)
            else: regex = re.compile(searchs)
            cffl.append((regex.subn, replaces))
        for file in fileList:
            # read file content
            fhandle = open(file, 'r')
            text = fhandle.read()
            fhandle.close()
            substitutions = 0
            # unpack the subn() function and the replace string
            for subnfunc, replaces in cffl:
                text, numOfChanges = subnfunc(replaces, text)
                substitutions += numOfChanges
            if substitutions:
                # first move away the original file
                """
                bakFileName = '%s%s' % (file, bext)
                if os.path.exists(bakFileName): os.unlink(bakFileName)
                os.rename(file, bakFileName)
                """
                # now write the new file content
                fhandle = open(file, 'w')
                fhandle.write(text)
                fhandle.close()
                filesChanged += 1
    except Exception, e: raise ScriptError(str(e))

    # return the number of files that had some of their content changed
    return(filesChanged)

def printr(results):
    """
    prints the results of ffind()/ffindgrep() in a manner similar to
    the UNIX find utility
    """
    if type(results) == types.DictType:
        for f in sorted(results.keys()):
            sys.stdout.write("%s\n%s\n" % (results[f],f))
    else:
        for f in sorted(results):
            sys.stdout.write("%s\n" % f)

if __name__ == '__main__':
    pass
