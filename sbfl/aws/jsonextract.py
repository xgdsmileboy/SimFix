#!/usr/bin/python3

'''Fetches an item out of a JSON-encoded object.

The AWS command-line tools often print JSON objects, which only contain
one thing (e.g. a public domain name) that we actually care about.
If you pipe that JSON through this script, you can extract just that thing.

Usage example:

    $ echo '{"foo": ["bar", {"baz": "spam"}]}' |
    > python jsonextract.py foo 1 baz
    spam

(This script just prints `input_object['foo'][1]['baz']`. Note that the "1",
 because it looks like a number, is interpreted as a number; you're out of luck
 if you want to extract the "1"th element of a JSON object.)
'''

import json
import sys

def parse_ints_in_indices(indices):
  '''parse_ints_in_indices(["1", "foo", "2"]) -> [1, "foo", 2]'''
  return [int(index) if index.isnumeric() else index for index in indices]

def deep_getitem(obj, indices):
  '''deep_getitem(d, [1, "foo"]) -> d[1]["foo"]'''
  for index in indices:
    obj = obj[index]
  return obj

if __name__ == '__main__':
  import argparse
  parser = argparse.ArgumentParser()
  parser.add_argument('indices', nargs='*', help='the object-keys or array-indices leading to the piece to print')
  args = parser.parse_args()

  indices = parse_ints_in_indices(args.indices)
  obj = json.load(sys.stdin)
  print(deep_getitem(obj, indices))
