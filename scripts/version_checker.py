#!/usr/bin/env python

from abc import ABCMeta, abstractmethod
from os import listdir, path
from re import findall
from urllib2 import urlopen

class Parser:
  __metaclass__ = ABCMeta

  def __init__(self, url, regex):
    self.url = url
    self.regex = regex

  def newest(self):
    if self.url is None:
      return False
    else:
      request = urlopen(self.url)
      content = self.content(request.read(), request.geturl())
      matches = findall(self.regex, content)
      result = matches[0][0] if (len(matches) > 0 and len(matches[0]) > 0) else None
      if result is None:
          raise Exception('Failed to parse content for version')
      else:
          return result

  @abstractmethod
  def content(self, content, url):
    pass

class AdColony(Parser):
  
  def __init__(self):
    Parser.__init__(
      self,
      'https://raw.githubusercontent.com/AdColony/AdColony-Android-SDK-3/master/CHANGELOG.md',
      r'\#\#\s+((\d+\.)+\d+)')

  def content(self, content, url):
    return content

class AdMob(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://firebase.google.com/docs/admob/android/quick-start',
      r'compile \'com\.google\.firebase:firebase-ads:((\d+\.)+\d+)\'')

  def content(self, content, url):
    return content

class Amazon(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://developer.amazon.com/sdk-download',
      r'Amazon Mobile Ads \(((\d+\.)+\d+)\)')

  def content(self, content, url):
    return content

class AppLovin(Parser):

  def __init__(self):
    Parser.__init__(self, None, None)

  def content(self, content, url):
    return None

class Chartboost(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'http://www.chartboo.st/sdk/android',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

class Facebook(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'http://central.maven.org/maven2/com/facebook/android/audience-network-sdk/maven-metadata.xml',
      r'<latest>((\d+\.)+\d+)')

  def content(self, content, url):
    return content

class Flurry(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://bintray.com/yahoo/maven/com.flurry/_latestVersion',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

class HyprMx(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      None,
      None)

  def content(self, content, url):
    return None

class InMobi(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://bintray.com/inmobi/maven/inmobi-ads/_latestVersion',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

class Ironsource(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://bintray.com/ironsource-mobile/android-sdk/mediation/_latestVersion',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

class LoopMe(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://bintray.com/loopme/maven/loopme-sdk/_latestVersion',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

class MachineZone(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      None,
      None)

  def content(self, content, url):
    return None

class MobFox(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://api.github.com/repos/mobfox/MobFox-Android-SDK/releases/latest',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return content

class MoPub(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://bintray.com/mopub/mopub-android-sdk/mopub-android-sdk/_latestVersion',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

class Tapjoy(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://bintray.com/tapjoy/tapjoy-sdk/android-sdk/_latestVersion',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

class Thirdpresence(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://bintray.com/thirdpresence/thirdpresence-ad-sdk-android/com.thirdpresence.adsdk.sdk/_latestVersion',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

class Unity(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://api.github.com/repos/Unity-Technologies/unity-ads-android/releases/latest',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return content

class Vungle(Parser):

  def __init__(self):
    Parser.__init__(
      self,
      'https://v.vungle.com/dashboard/api/1/sdk/android',
      r'((\d+\.)+\d+)')

  def content(self, content, url):
    return url

providers = {
  'adcolony': AdColony(),
  'admob': AdMob(),
  'amazon': Amazon(),
  'applovin': AppLovin(),
  'chartboost': Chartboost(),
  'facebook': Facebook(),
  'flurry': Flurry(),
  'hyprmx': HyprMx(),
  'inmobi': InMobi(),
  'ironsource': Ironsource(),
  'loopme': LoopMe(),
  'machinezone': MachineZone(),
  'mobfox': MobFox(),
  'mopub': MoPub(),
  'tapjoy': Tapjoy(),
  'thirdpresence': Thirdpresence(),
  'unity': Unity(),
  'vungle': Vungle()
}

def newer(new, old):
  newl = new.split('.')
  oldl = old.split('.')
  if len(newl) > len(oldl):
    oldl.extend(['0' for _ in xrange(len(newl) - len(oldl))])
  elif len(oldl) > len(newl):
    newl.extend(['0' for _ in xrange(len(oldl) - len(newl))])

  for o, n in zip(oldl, newl):
    if (int(o) < int(n)):
        return True

  return False

def main():
  directories = [ x for x in listdir('.') if x.startswith('provider-') ]
  for directory in directories:
    provider = directory[len('provider-'):]
    properties = dict(line.strip().split('=') for line in open(path.join('.', directory, 'gradle.properties'), 'r'))
    current = properties['PROVIDER_VERSION']
    newest = providers[provider].newest()

    if newest is False:
      print("# %s is not supported" % provider)
    elif newer(newest, current):
      print("# %s has a new version %s" % (provider, newest))

if __name__ == "__main__":
  main()
