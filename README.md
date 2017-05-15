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
