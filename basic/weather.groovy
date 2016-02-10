import groovy.json.*

//get config values
def apiKey = config.global("weather.key")

//get input parameters
def city = input.get("city")

//build the url
def url="http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&units=metric"

//call the connector
def wResponse=call.connector("OpenWeather")
         .set("method","get")
         .set("url",url)
         .set("timeout",5000)
         .sync()

//get the response
def myBody = wResponse.body

//Create a Object out of the JSON response
def jsonSlurper = new JsonSlurper()
def myWeather = jsonSlurper.parseText(myBody)

//Get the actual Values
log.info "Humidity: " + myWeather.main.humidity + "%"
log.info "Temp: " + myWeather.main.temp + "°C"

//Provide some output
output.set("Wetter", "${myWeather.weather.main}")
output.set("Temp", myWeather.main.temp.toString())
