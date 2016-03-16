# Vungle Provider

## Parameters
* `appId` string, app id key as provided by the Vungle integration guide

## Notes
Unity does not repackage assets correctly from JARs inside of AARs, which
results in the Vungle assets not appearing correctly when built from Unity.
For this reason the assets need to be unpacked from the Vungle JAR manually
into `assets/` and have exclusions added for them into `build.gradle`.
