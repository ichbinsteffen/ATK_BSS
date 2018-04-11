# ATK BSS

ATK BSS stands for Ambisonics Toolkit (ATK) Based Spatialization Server (BSS). 
The ATK provides SuperCollider with a large library for ambisonics reproduction 
and ATK_BSS builds a convenient little GUI window on top of it and enables to 
use ATK with the SpatDIF format for OSC messages. 
This lets a sound spatialization environment that sends SpatDIF compliant OSC messages
make use of the rendering engine of ATK. 

![alt text](https://github.com/ichbinsteffen/tree/master/misc/images/window.PNG "A screenshot of the ATK_BSS window.")

For further information about ATK, visit:
https://github.com/ambisonictoolkit/atk-sc3

## SpatDIF 

The Spatial Sound Description Interchange Format (SpatDIF) is a format intended for the use of spatial audio applications. 
The before mentioned possiblity to transmit data over OSC is only one branch of
SpatDIFs specifications. In ATK BSS however only OSC data will be received.


At the current state of ATK BSS, IDs are considered to be numeric and will be interpreted as such in the OSC path.
Following statements:

    /spatdif/source/<id>/position
    /spatdif/media/<id>/location

would only work in this manner:

    /spatdif/source/0/position 
    /spatdif/source/13/location
  
The housekeeping of the appropriate association between source objects and media is thereby easier and more efficient to achieve.

...to be continued...

For further information about SpatDIF, visit: 
http://www.spatdif.org/
