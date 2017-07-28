#!/usr/bin/python

import re
import os
import glob

pattern = re.compile(rb'''
  ^(?P<before>
    \+[\ \t]*
    \}?[\ \t]*
    (else[\ \t]+)?
    (if|while)[\ \t]*
    \(
   )
   (?P<bool>true|false)
   (?P<after>
    \)[\ \t]*
    \{[\ \t]*
    .*
   )
''', flags=re.VERBOSE|re.MULTILINE)

def get_replacement(match):
  return b''.join([
    match.group('before'),
    b'Boolean.parseBoolean("',
    match.group('bool'),
    b'")',
    match.group('after')])

if not os.environ.get('D4J_HOME'):
  raise RuntimeError('D4J_HOME must be set')

for patch_path in glob.glob(os.path.join(os.environ['D4J_HOME'], 'framework', 'projects', '*', 'patches', '*.src.patch')):
  s = open(patch_path, 'rb').read()
  open(patch_path, 'wb').write(pattern.sub(get_replacement, s))
