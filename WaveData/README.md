# WaveData

Acturally this project is graphic module implemeted for Noise Detector project, but you can use this module to other project.

This module animate bars up and down according to array received by using `SurfaceView`. You can configure only up animation by modifying `WaveBar`.

This is composed of four major classes.

## EdgnLine

This is responsible for drawing edge line, marking point and text on the left.

## CriticalLine

This is responsible for drawing critical line. the `heigh` of this line is converted `float value` and notified `WaveBars` and external class implemented `ExternalCLValueListener interface`.

## WaveBars

This is responsible for drawing and animating bars up and down according to array received.

To do this, this implement SurfaceView and repeat drawing bars according to `period` and `oneFramePeriod` input when initialized.

Also this compare the `maximum value` of array with `heigh value` notified from CriticalLine. If `maximum value` is greater than `heigh value`, it is notified to Warning through `WarningListener interface`.

## Warning

This is responsible for warning by twenkling warning color when WaveBars notify.

## Video

You can see the action of this module in [the link](http://joondong.tistory.com/28?category=651762). the running application is noise detector and specific rectangular area in this is WaveData module.

# Initailization

You can configure colors and size of bars, edge line and so on, and also adjust frame size for animation, but you can create only one instance each project.

You can get instance of this module by using `getView` method.

Parameters and these meaning are as follow.

**Common settings**

`backColor` Background color. Acturally it is only used at WaveBars. (other views background color is transparent)

`extraRateHeigh_hor` The ratio EdgeLine occupy when orientation is horizontal mode.

`extraRateWidth_hor`

`extraRateHeigh_ver` The ratio EdgeLine occupy when orientation is vertical mode.

`extraRateWidth_ver`

`debugMode` is not implemented yet.

**EdgeLine settings**

`lineWidth` Edge line width(DP). Acturally It is used at EdgeLine and WaveBars.

`lineColor` Edge line's color

`unit` String indicated on the top left. This is always enclosed in parentheses.

`edgeMaxValue`

`isFraction`

`numOfFraction`

`edgeFontSize_ver`

`edgeFontSize_hor`

`edgeFontColor`

**CriticalLine Settings**

`criColor`

`criWidth`

`criMinValue`

**WaveBars settings**

`barColor`

`gradient`

`gradientColor`

`intervalRate`

`period`

`oneFramePeriod`

`autoAdjustPeriod`

**Waring settings** 

`warnColor`

`warnTime`

`twinkNum`