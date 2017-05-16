## Prologue

This project contain an experiment of GoogleFit and Awareness public API, specifically to detect Daily Steps count, Geofencing, Location Updates and User Activities. 

## Project Purpose

- Playing around with Google public APIs
- I want to know how can I keep my [`Services`](https://developer.android.com/reference/android/app/Service.html) running on background without rely on [`startForeground()`](https://developer.android.com/guide/components/services.html#Foreground) functionality 
- I want to track down all detected event inside of preference and show it on the application

## Epilogue
Here is the conclusions :

- Google Fit
	- Only Steps Count API continue measuring daily steps event application being force stopped by system or User
- I can wake up my services through several events such as :
	- Whenever phone rebooted
	- Whenever geofencing detected
	- Whenever Location updates detected
	- Through Alarm manager	  

## Screen shoot
### Features
#### &emsp;&emsp;1. Steps Count History
&emsp;&emsp;<img src="https://cloud.githubusercontent.com/assets/21379421/26088196/f07588c4-3a1f-11e7-936a-69a8fdc7814c.png" width="300">

#### &emsp;&emsp;2. Geofencing History
&emsp;&emsp;<img src="https://cloud.githubusercontent.com/assets/21379421/26088171/cfa6cca2-3a1f-11e7-95fe-a21e7c399a71.png" width="300">

#### &emsp;&emsp;3. Detected Activities
&emsp;&emsp;<img src="https://cloud.githubusercontent.com/assets/21379421/26088136/9070a40e-3a1f-11e7-83d6-7d4642f145e5.png" width="300">

#### &emsp;&emsp;4. Location History
&emsp;&emsp;<img src="https://cloud.githubusercontent.com/assets/21379421/26088038/c3972d2c-3a1e-11e7-89da-b05ef8dfe2c0.png" width="300">

## Battery Consumptions
&emsp;&emsp;<img src="https://cloud.githubusercontent.com/assets/21379421/26088398/3e8e15d4-3a21-11e7-9b23-87f53ef3fcb9.png" width="300">

&emsp;&emsp;<img src="https://cloud.githubusercontent.com/assets/21379421/26088420/5b967c7a-3a21-11e7-923a-b663b2adb00e.png" width="300">
