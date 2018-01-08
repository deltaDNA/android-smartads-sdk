#!/usr/bin/env python

import unittest
import version_checker as uut

class Test(unittest.TestCase):
  
  def test_newest(self):
    self.assertFalse(uut.newer('1', '1'))

    self.assertTrue(uut.newer('2', '1'))
    self.assertTrue(uut.newer('2', '1.0'))
    self.assertTrue(uut.newer('2.0', '1'))
    self.assertTrue(uut.newer('2.0', '1.0'))

    self.assertFalse(uut.newer('2.0', '2.0'))
    self.assertTrue(uut.newer('2.0.1', '2.0'))
    self.assertTrue(uut.newer('2.1', '2'))
    self.assertTrue(uut.newer('2.1', '2.0'))

if __name__ == '__main__':
  unittest.main()
