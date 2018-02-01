# WaveData

Acturally this project is graphic module implemeted for Noise Detector project, but you can use this module to other project.

This module animate bars up and down according to array received by using `SurfaceView`. You can configure only up animation by modifying `WaveBar`.

This is composed of four major classes.

## EdgnLine

This is responsible for drawing edge line, marking point and text on the left.

## CriticalLine

This is responsible for drawing critical line. the `height` of this line is converted `float value` and notified `WaveBars` and external class implemented `ExternalCLValueListener interface`.

## WaveBars

This is responsible for drawing and animating bars up and down according to array received.

To do this, this implement SurfaceView and repeat drawing bars according to `period` and `oneFramePeriod` input when initialized.

Also this compare the `maximum value` of array with `height value` notified from `CriticalLine`. If `maximum value` is greater than `height value`, it is notified to Warning through `WarningListener interface`.

Received array is packaged to `ArrayBundle` instance

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

`extraRateHeight_hor` The ratio EdgeLine occupy when orientation is horizontal mode.

`extraRateWidth_hor`

`extraRateHeight_ver` The ratio EdgeLine occupy when orientation is vertical mode.

`extraRateWidth_ver`

`maxValue` Max data size received from a peer. `View's height` x `extraRateHeight_hor/ver` is corresponding to this.

`debugMode` is not implemented yet.

**EdgeLine settings**

`lineWidth` Edge line width(DP). Acturally It is used at EdgeLine and WaveBars.

`lineColor` Edge line's color.

`unit` String indicated on the top left. This is always enclosed in parentheses.

`edgeMaxValue` This is converted to string then, 1/4, 1/2, 3/4 times value.

`isFraction` If true, display decimal places of `value` converted from `edgeMaxValue` displayed next to vertical line.

`numOfFraction` It indicates how many decimal places are displayed.

`edgeFontSize_ver` The size of the `value` above in vertical mode.

`edgeFontSize_hor` The size of the `value` above in horizontal mode.

`edgeFontColor` The color of the `value` above.

**CriticalLine Settings**

`criColor` The color of the critical line.

`criWidth` The width of the critical line.

`criMinValue` Minimum value about maxValue that critical line can down. This value is converted to height, then compared with the height converted from `maxValue`.

**WaveBars settings**

`barColor` Color of bars animated up and down.

`gradient` If true, configured gradient about `barColor`.

`gradientColor` It indicates gradient color mixed with `barColor` if `gradient` is true.

`intervalRate` Interval between each bar.

`period` between up and down

`oneFramePeriod` Number of frame during the time up and down(`period`)

`autoAdjustPeriod` If true, period is reduced when the number of `ArrayBundle` in the buffer exceeds 3

**Waring settings** 

`warnColor` Warning color. It is displayed as radial gradient. 

`warnTime` Twinkling time.

`twinkNum` The number of twinkle during `warnTime`.

# Major methods

### animateBars(float[] values)

### animateBars(byte[] values)

### animateBars(float[] values, float maxValue)

> This method calculates max value in `values` array and package the array to `ArrayBundle`.
>
> If byte array is input, it is converted float inside.
>
> You can calculate and input max value directly in the external thread to reduce overhead of main thread.

### setVibrator(boolean onOFF)

> If onOFF is true, vibration occurs when max value of `ArrayBunle` exceeds the value converted from ciritical line's height.