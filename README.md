# ATK BSS

ATK BSS stands for Ambisonics Toolkit (ATK) Based Spatialization Server (BSS). 
The ATK provides SuperCollider with a large library for ambisonics reproduction 
and ATK_BSS builds a convenient little GUI window on top of it and enables to 
use ATK with the SpatDIF format for OSC messages. 
This lets a sound spatialization environment that sends SpatDIF compliant OSC messages
make use of the rendering engine of ATK. 

![Screenshot](/misc/images/window.PNG "A screenshot of the ATK_BSS window.")

For further information about ATK, visit:
https://github.com/ambisonictoolkit/atk-sc3

## SpatDIF 

The Spatial Sound Description Interchange Format (SpatDIF) is a format intended for the use of spatial audio applications. 
The before mentioned possiblity to transmit data over OSC is only one branch of
SpatDIFs specifications. In ATK BSS however only OSC data will be received.

SpatDIF sceme:

    /spatdif/source/<id>/position   (msg: [3.141, 1.575, 0])
    /spatdif/media/<id>/location    (msg: "C:/sounds/birds.wav")


...to be continued...

For further information about SpatDIF, visit: 
http://www.spatdif.org/
