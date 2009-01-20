mkdir build && cd build
echo ----
echo Downloading all sources...
echo ----
wget http://idmc.googlecode.com/files/iDMC_building_toolset.tar.bz2
tar xjf iDMC_building_toolset.tar.bz2
cd iDMC_building_toolset
wget http://idmclib.googlecode.com/files/idmclib-0.11.0-Source.tar.bz2
tar xjf idmclib-0.11.0-Source.tar.bz2
wget http://idmc.googlecode.com/files/iDmc-2.1.0-src.tar.bz2
tar xjf iDmc-2.1.0-src.tar.bz2
echo ----
echo Finished downloading sources
echo ----
echo ----
echo Compiling native library
echo ----
cd idmclib-0.9.0-Source
make java
echo ----
echo Done
echo ----
