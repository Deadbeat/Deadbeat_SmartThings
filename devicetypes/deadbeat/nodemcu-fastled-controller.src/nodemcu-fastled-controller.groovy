/**
 *  Child RGBW Switch
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Change History:
 *
 *    Date        Who            What
 *    ----        ---            ----
 *    2017-10-01  Allan (vseven) Original Creation (based on Dan Ogorchock's child dimmer switch)
 *    2017-10-06  Allan (vseven) Added preset color buttons and logic behind them.
 *    2017-10-12  Allan (vseven) Added ability to change White and renamed to RGBW from RGB
 *	  2017-12-17  Allan (vseven) Modified setColor to use the newer color attributes of only hue and saturation which
 *                               it compatible with values passed in from things like Alexa or Goggle Home.
 *	  2018-03-11  Nick (Deadbeat)Modified and updated to control an ESP8266 NodeMCU FastLED controller
 */

import groovy.json.JsonSlurper
import groovy.json.JsonOutput

// for the UI
metadata {
	definition (name: "NodeMCU FastLED Controller", namespace: "deadbeat", author: "Nick (deadbeat) - based on code by Allan (vseven) - based on code by Dan Ogorchock") {
	capability "Switch"		
	capability "Switch Level"
	capability "Actuator"
	capability "Color Control"
	capability "Sensor"
	capability "Light"
	
    attribute "effect", "string"

	command "generateEvent", ["string", "string"]

	command "softwhite"
	command "daylight"
	command "warmwhite"
	command "red"
	command "green"
	command "blue"
	command "cyan"
	command "magenta"
	command "orange"
	command "purple"
	command "yellow"
	command "white"

	command "brightness"
    
	command "solid"
    command "candycane"
    command "confetti"
    command "cyclonrainbow"
    command "dots"
    command "fire"
    command "glitter"
    command "juggle"
    command "lightning"
    command "policeall"
    command "policeone"
    command "rainbow"
    command "rainbowwithglitter"
    command "sinelon"
    command "twinkle"
    command "randomstars"
    command "sinehue"
    command "ripple"
    

  command "setWhiteLevel"

  attribute "whiteLevel", "number"
  }
/* Pulled from mqtt-bridge - no longer needed, but preserved
	preferences {
        input("ip", "string",
            title: "MQTT Bridge IP Address",
            description: "MQTT Bridge IP Address",
            required: true,
            displayDuringSetup: true
        )
        input("port", "string",
            title: "MQTT Bridge Port",
            description: "MQTT Bridge Port",
            required: true,
            displayDuringSetup: true
        )
        input("mac", "string",
            title: "MQTT Bridge MAC Address",
            description: "MQTT Bridge MAC Address",
            required: true,
            displayDuringSetup: true
        )
    }
    */
	simulator {
		// TODO: define status and reply messages here
	}

	tiles (scale: 2){
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.illuminance.illuminance.dark", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.illuminance.illuminance.light", backgroundColor:"#00A0DC", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.illuminance.illuminance.light", backgroundColor:"#ffffff", nextState:"turningOn"
			}    
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
        			attributeState "level", action:"switch level.setLevel"
    			}
			tileAttribute ("device.color", key: "COLOR_CONTROL") {
				attributeState "color", action:"color control.setColor"
			}
		}
		controlTile("whiteSliderControl", "device.whiteLevel", "slider", height: 1, width: 6, inactiveLabel: false) {
			state "whiteLevel", action:"setWhiteLevel", label:'White Level'
        }
 		valueTile("lastUpdated", "device.lastUpdated", inactiveLabel: false, decoration: "flat", width: 6, height: 2) {
    			state "default", label:'Last Updated ${currentValue}', backgroundColor:"#ffffff"
		}
		standardTile("softwhite", "device.softwhite", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offsoftwhite", label:"soft white", action:"softwhite", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onsoftwhite", label:"soft white", action:"softwhite", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFF1E0"
		}
		standardTile("daylight", "device.daylight", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offdaylight", label:"daylight", action:"daylight", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "ondaylight", label:"daylight", action:"daylight", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFB"
		}
		standardTile("warmwhite", "device.warmwhite", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offwarmwhite", label:"warm white", action:"warmwhite", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onwarmwhite", label:"warm white", action:"warmwhite", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFF4E5"
		}
		standardTile("red", "device.red", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offred", label:"red", action:"red", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onred", label:"red", action:"red", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF0000"
		}
		standardTile("green", "device.green", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offgreen", label:"green", action:"green", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "ongreen", label:"green", action:"green", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00FF00"
		}
		standardTile("blue", "device.blue", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offblue", label:"blue", action:"blue", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onblue", label:"blue", action:"blue", icon:"st.illuminance.illuminance.bright", backgroundColor:"#0000FF"
		}
		standardTile("cyan", "device.cyan", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offcyan", label:"cyan", action:"cyan", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "oncyan", label:"cyan", action:"cyan", icon:"st.illuminance.illuminance.bright", backgroundColor:"#00FFFF"
		}
		standardTile("magenta", "device.magenta", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offmagenta", label:"magenta", action:"magenta", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onmagenta", label:"magenta", action:"magenta", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF00FF"
		}
		standardTile("orange", "device.orange", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offorange", label:"orange", action:"orange", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onorange", label:"orange", action:"orange", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FF6600"
		}
		standardTile("purple", "device.purple", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offpurple", label:"purple", action:"purple", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onpurple", label:"purple", action:"purple", icon:"st.illuminance.illuminance.bright", backgroundColor:"#BF00FF"
		}
		standardTile("yellow", "device.yellow", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offyellow", label:"yellow", action:"yellow", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onyellow", label:"yellow", action:"yellow", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFF00"
		}
		standardTile("white", "device.white", width: 2, height: 2, inactiveLabel: false, canChangeIcon: false) {
		    state "offwhite", label:"White", action:"white", icon:"st.illuminance.illuminance.dark", backgroundColor:"#D8D8D8"
		    state "onwhite", label:"White", action:"white", icon:"st.illuminance.illuminance.bright", backgroundColor:"#FFFFFF"
		}
        standardTile("solid", "device.solid", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offsolid", label:"solid", action:"solid", backgroundColor:"#FFFFFF"
		    state "onsolid", label:"solid", action:"solid", backgroundColor:"#00FF00"
		}
        standardTile("candycane", "device.candycane", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offcandycane", label:"Candy Cane", action:"candycane", backgroundColor:"#FFFFFF"
		    state "oncandycane", label:"Candy Cane", action:"candycane", backgroundColor:"#00FF00"
		}
        standardTile("confetti", "device.confetti", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offconfetti", label:"Confetti", action:"confetti", backgroundColor:"#FFFFFF"
		    state "onconfetti", label:"Confetti", action:"confetti", backgroundColor:"#00FF00"
		}
        standardTile("cyclonrainbow", "device.cyclonrainbow", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offcyclonrainbow", label:"Cyclon Rainbow", action:"cyclonrainbow", backgroundColor:"#FFFFFF"
		    state "oncyclonrainbow", label:"Cyclon Rainbow", action:"cyclonrainbow", backgroundColor:"#00FF00"
		}
        standardTile("dots", "device.dots", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offdots", label:"Dots", action:"dots", backgroundColor:"#FFFFFF"
		    state "ondots", label:"Dots", action:"dots", backgroundColor:"#00FF00"
		}
        standardTile("fire", "device.fire", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offfire", label:"Fire!", action:"fire", backgroundColor:"#FFFFFF"
		    state "onfire", label:"Fire!", action:"fire", backgroundColor:"#00FF00"
		}
        standardTile("glitter", "device.glitter", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offglitter", label:"Glitter", action:"glitter", backgroundColor:"#FFFFFF"
		    state "onglitter", label:"Glitter", action:"glitter", backgroundColor:"#00FF00"
		}
        standardTile("juggle", "device.juggle", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offjuggle", label:"Juggle", action:"juggle", backgroundColor:"#FFFFFF"
		    state "onjuggle", label:"Juggle", action:"juggle", backgroundColor:"#00FF00"
		}
        standardTile("lightning", "device.lightning", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offlightning", label:"Lightning", action:"lightning", backgroundColor:"#FFFFFF"
		    state "onlightning", label:"Lightning", action:"lightning", backgroundColor:"#00FF00"
		}
        standardTile("policeall", "device.policeall", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offpoliceall", label:"Police - All", action:"policeall", backgroundColor:"#FFFFFF"
		    state "onpoliceall", label:"Police - All", action:"policeall", backgroundColor:"#00FF00"
		}
        standardTile("policeone", "device.policeone", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offpoliceone", label:"Police - One", action:"policeone", backgroundColor:"#FFFFFF"
		    state "onpoliceone", label:"Police - One", action:"policeone", backgroundColor:"#00FF00"
		}
        standardTile("rainbow", "device.rainbow", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offrainbow", label:"Rainbow", action:"rainbow", backgroundColor:"#FFFFFF"
		    state "onrainbow", label:"Dots", action:"rainbow", backgroundColor:"#00FF00"
		}
        standardTile("rainbowwithglitter", "device.rainbowwithglitter", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offrainbowwithglitter", label:"Rainbow Glitter", action:"rainbowwithglitter", backgroundColor:"#FFFFFF"
		    state "onrainbowwithglitter", label:"Rainbow Glitter", action:"rainbowwithglitter", backgroundColor:"#00FF00"
		}
        standardTile("sinelon", "device.sinelon", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offsinelon", label:"Sinelon", action:"sinelon", backgroundColor:"#FFFFFF"
		    state "onsinelon", label:"Sinelon", action:"sinelon", backgroundColor:"#00FF00"
		}
        standardTile("twinkle", "device.twinkle", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offtwinkle", label:"Twinkle", action:"twinkle", backgroundColor:"#FFFFFF"
		    state "ontwinkle", label:"Twinkle", action:"twinkle", backgroundColor:"#00FF00"
		}
        standardTile("randomstars", "device.randomstars", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offrandomstars", label:"Random Stars", action:"randomstars", backgroundColor:"#FFFFFF"
		    state "onrandomstars", label:"Random Stars", action:"randomstars", backgroundColor:"#00FF00"
		}
        standardTile("sinehue", "device.sinehue", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offsinehue", label:"Sinehue", action:"sinehue", backgroundColor:"#FFFFFF"
		    state "onsinehue", label:"Sinehue", action:"sinehue", backgroundColor:"#00FF00"
		}
        standardTile("ripple", "device.ripple", decoration:"flat", width: 2, height: 1, inactiveLabel: false, canChangeIcon: false) {
		    state "offripple", label:"Ripple", action:"ripple", backgroundColor:"#FFFFFF"
		    state "onripple", label:"Ripple", action:"ripple", backgroundColor:"#00FF00"
		}
        
		main(["switch"])
		//details(["switch", "level", "color", "whiteSliderControl", "softwhite","daylight","warmwhite","red","green","blue","white","cyan",
        details(["switch", "level", "color", "softwhite","daylight","warmwhite","red","green","blue","white","cyan",
			 "magenta","orange","purple","yellow",
             "solid","candycane","confetti","cyclonrainbow","dots","fire","glitter","juggle","lightning","policeall","policeone","rainbow","rainbowwithglitter","sinelon","sinehue","twinkle","randomstars","ripple",
             "lastUpdated"])
	}
}

/*** MQTT-BRIDGE COPY ***/
/*
// Store the MAC address as the device ID so that it can talk to SmartThings
def setNetworkAddress() {
    // Setting Network Device Id
    def hex = "$settings.mac".toUpperCase().replaceAll(':', '')
    hex = "ABCDEF123456" // ac:bc:32:ef:68:d7"
    if (device.deviceNetworkId != "$hex") {
        device.deviceNetworkId = "$hex"
        log.debug "Device Network Id set to ${device.deviceNetworkId}"
    }
}

// Parse events from the Bridge
def parse(String description) {
    setNetworkAddress()

    log.debug "Parsing '${description}'"
    def msg = parseLanMessage(description)

    return createEvent(name: "message", value: new JsonOutput().toJson(msg.data))
}

// Send message to the Bridge
def deviceNotification(message) {
    if (device.hub == null)
    {
        log.error "Hub is null, must set the hub in the device settings so we can get local hub IP and port"
        return
    }
    
    log.debug "Sending '${message}' to device"
    setNetworkAddress()

    def slurper = new JsonSlurper()
    def parsed = slurper.parseText(message)
    
    if (parsed.path == '/subscribe') {
        parsed.body.callback = device.hub.getDataValue("localIP") + ":" + device.hub.getDataValue("localSrvPortTCP")
    }

    def headers = [:]
    headers.put("HOST", "$ip:$port")
    headers.put("Content-Type", "application/json")

    def hubAction = new physicalgraph.device.HubAction(
        method: "POST",
        path: parsed.path,
        headers: headers,
        body: parsed.body
    )
    hubAction
}
*/
/*** END MQTT COPY ***/
def on() {
    sendEvent(name: "switch", value: "on", isStateChange: true)
//    sendEvent(name: "color", value: "{\"color\": {\"r\":255,\"g\":255,\"b\":255}}") // Added this to fix the thing i broke
    def lastColor = device.latestValue("color")
    //log.debug("On pressed.  Sending last known color value of $lastColor or if null command to white.")
    // Also since we are turning back on make sure we have at least one level turned up.
    def level = device.latestValue("level")
    def whiteLevel = device.latestValue("whiteLevel")
    if (level == 0 || level == null) {
     //log.debug("Level is 0 or null.  Checking whitelevel")
        if (whiteLevel == 0 || whiteLevel == null) {
        	//log.debug("whiteLevel is 0 or null.  Setting to 20")
			    sendEvent(name: "whiteLevel", value: 20)
        }
    }
    //parent.childOn(device.deviceNetworkId)
   	if ( lastColor == Null ) {  // For initial run
   		white() 
   	} else {
   		adjustColor(lastColor)
    }
}

def off() {
    toggleTiles("off")
    sendEvent(name: "switch", value: "off", isStateChange: true)
    //log.debug("Off pressed.  Update parent device.")
    //parent.childOff(device.deviceNetworkId)
}

def setColor(Map color) {
    log.debug "raw color map passed in: ${color}"
    // Turn off the hard color tiles
    toggleTiles("off") 
    
    // Turn on the light if color called directly but light is off
    if(device.latestValue("switch") == "off") {
    	on()
    }
    log.debug("Switch is $sw")
    log.debug("Color Input: ${color}")
    log.debug("Hue: $color.hue")
    if(color.saturation == 0) {
    	log.debug("Saturation zero")
    }
    // If the color picker was selected we will have Red, Green, Blue, HEX, Hue, Saturation, and Alpha all present.
    // Any other control will most likely only have Hue and Saturation
    if (color.hex){
        // came from the color picker.  Since the color selector takes into account lightness we have to reconvert
        // the color values and adjust the level slider to better represent where it should be at based on the color picked
        def colorHSL = rgbToHSL(color)
        //log.debug "colorHSL: $colorHSL"
        sendEvent(name: "level", value: (colorHSL.l * 100))
        adjustColor(color.hex)
    } else if (color.hue || color.saturation || (color.hue == 0 && color.saturation == 0)) {
        // came from the ST cloud which only contains hue and saturation.  So convert to hex and pass it.
        log.debug("Color and Hue passed")
		def lightness = 0.5
		if(color.hue == 0 && color.saturation == 0) {
        	log.debug("White passed - overriding lightness")
            lightness = 1
        }
        def colorRGB = hslToRGB(color.hue, color.saturation, lightness)
        def colorHEX = rgbToHex(colorRGB)
        adjustColor(colorHEX)
    }
}

def setLevel(value) {
    def level = Math.min(value as Integer, 100)
    // log.debug("Level value in percentage: $level")
    sendEvent(name: "level", value: level, isStateChange: true)
	
    // Turn on or off based on level selection
    if (level == 0) { 
	    off() 
    } else {
	    if (device.latestValue("switch") == "off") { on() }
       def color = device.latestValue("color")
       log.debug("SetLevel: Last color is: $color")
       
   	  adjustColor(color) 

    }
}

def setWhiteLevel(value) {
    //log.debug "setWhiteLevel: ${value}"
    value = Math.min(value as Integer, 100)
    sendEvent(name: "whiteLevel", value: value)
    def lastColor = device.latestValue("color")
	adjustColor(lastColor)
}

def checkOnOff() {
 // Turn on or off based on level selection of both levels
    def level = device.latestValue("level")
    def whiteLevel = device.latestValue("whiteLevel")
    if (level == 0) { 
        if (whiteLevel == 0) {
			off() 
        }
    } else {
	    if (device.latestValue("switch") == "off") {
    	    on() 
        }
    }
}

def adjustColor(inputColor) {

	def colorInHEX
    log.debug("Input color: $inputColor")
	if(inputColor.startsWith("{")) {
        def slurper = new JsonSlurper()
        def parsed = slurper.parseText(inputColor)
	
		if(parsed.effect) {
        	setEffect(parsed.effect) 
            log.debug("Bailing out")
			return
            log.debug("You should not see this")
        } else {
            def rParsed = parsed.color.r
            def gParsed = parsed.color.g
            def bParsed = parsed.color.b

            def colorRGB = [red: Math.round(rParsed), green: Math.round(gParsed), blue: Math.round(bParsed)]
            colorInHEX = rgbToHex(colorRGB)
		}
    } else {
    	colorInHEX = inputColor
	}
    
//	sendEvent(name: "color", value: colorInHEX)
    def level = device.latestValue("level")
    
    def whiteLevel = device.latestValue("whiteLevel")
    // log.debug("level value is $level")
    if(level == null) {level = 50}
    //log.debug "level from adjustColor routine: ${level}"
    //log.debug "color from adjustColor routine: ${colorInHEX}"

    def c = hexToRgb(colorInHEX)
    
    def r = hex(c.red * (level/100))
    def g = hex(c.green * (level/100))
    def b = hex(c.blue * (level/100))

	def colorString = "{\"color\": {\"r\":${c.red},\"g\":${c.green},\"b\":${c.blue}}}"
    log.debug("ColorString: $colorString")
    
    sendEvent(name: "color", value: colorString, isStateChange: true)
//    sendEvent(name: "color", value: colorInHEX)
    
    //def w = hex(whiteLevel * 255 / 100)

    def adjustedColor = "#${r}${g}${b}"
    log.debug("Adjusted color is $adjustedColor")
    	
    // First check if we should be on or off based on the levels
    //checkOnOff()
	// Then send down the color info
    //parent.childSetColorRGB(device.deviceNetworkId, adjustedColor)
}

def setEffect(effectName) {
	log.debug("Setting Effect to $effectName")
    def effectString = "{\"effect\":\"$effectName\"}"
    sendEvent(name: "color", value: effectString)
}

def setBrightness(level) {
	log.debug("Setting brightness to $level")
    def effectString = "{\"brightness\":\"$level\"}"
    sendEvent(name: "color", value: effectString)
}

def generateEvent(String name, String value) {
    //log.debug("Passed values to routine generateEvent in device named $device: Name - $name  -  Value - $value")
    // The name coming in from ST_Anything will be "dimmerSwitch", but we want to the ST standard "switch" attribute for compatibility with normal SmartApps
    sendEvent(name: "switch", value: value)
    // Update lastUpdated date and time
    def nowDay = new Date().format("MMM dd", location.timeZone)
    def nowTime = new Date().format("h:mm a", location.timeZone)
    sendEvent(name: "lastUpdated", value: nowDay + " at " + nowTime, displayed: false)
    //deviceNotification('{"path":"/push","body":{"name":"Office Lamp","value":"on","type":"switch"}}')
}

def doColorButton(colorName) {
    toggleTiles(colorName.toLowerCase().replaceAll("\\s",""))
    def colorButtonData = getColorData(colorName)
    adjustColor(colorButtonData)
}

def doEffectButton(effectName) {
	log.debug("doEffectButton")
	toggleTiles(effectName.toLowerCase().replaceAll("\\s",""))
    def effectButtonData = getEffectData(effectName)
    setEffect(effectButtonData)
}

def getColorData(colorName) {
    //log.debug "getColorData colorName: ${colorName}"
    def colorRGB = colorNameToRgb(colorName)
    //log.debug "getColorData colorRGB: $colorRGB"
    def colorHEX = rgbToHex(colorRGB)
    //log.debug "getColorData colorHEX: $colorHEX"
    colorHEX
}

def getEffectData(effectName) {
	log.debug("Getting effect string for: $effectName")
	final effects = [
	[name:"candycane",	value: "candy cane"],
    [name:"solid",		value: "solid"],
    [name:"confetti",	value: "confetti\", \"transition\":\"40"],
    [name:"cyclonrainbow",		value: "cyclon rainbow"],
    [name:"dots",		value: "dots"],
    [name:"fire",		value: "fire"],
    [name:"glitter",		value: "glitter\", \"transition\":\"40"],
    [name:"juggle",		value: "juggle"],
    [name:"lightning",		value: "lightning"],
    [name:"policeall",		value: "police all"],
    [name:"policeone",		value: "police one"],
    [name:"rainbow",		value: "rainbow"],
    [name:"rainbowwithglitter",		value: "rainbow with glitter"],
    [name:"sinelon",		value: "sinelon"],
    [name:"twinkle",		value: "twinkle\", \"transition\":\"40"],
    [name:"randomstars",		value: "random stars\", \"transition\":\"40\", \"brightness\":\"120"],
    [name:"sinehue",		value: "sine hue"],
    [name:"ripple",		value: "ripple"],
    
    ]
    log.debug("effects: ${effects}")
    def effectData = [:]
    effectData = effects.find { it.name == effectName }
    log.debug("Found effect: ${effectData}")
	effectData.value
}

private hex(value, width=2) {
    def s = new BigInteger(Math.round(value).toString()).toString(16)
    while (s.size() < width) {
	s = "0" + s
    }
    s
}

def hexToRgb(colorHex) {
    log.debug("passed in colorHex: $colorHex")
    def rrInt = Integer.parseInt(colorHex.substring(1,3),16)
    def ggInt = Integer.parseInt(colorHex.substring(3,5),16)
    def bbInt = Integer.parseInt(colorHex.substring(5,7),16)

    def colorData = [:]
    colorData = [red: rrInt, green: ggInt, blue: bbInt]
    
    colorData
}

def rgbToHex(rgb) {
    //log.debug "rgbToHex rgb value: $rgb"
    def r = hex(rgb.red)
    def g = hex(rgb.green)
    def b = hex(rgb.blue)
	
    def hexColor = "#${r}${g}${b}"

    hexColor
}

def rgbToHSL(color) {
	def r = color.red / 255
    def g = color.green / 255
    def b = color.blue / 255
    def h = 0
    def s = 0
    def l = 0

    def var_min = [r,g,b].min()
    def var_max = [r,g,b].max()
    def del_max = var_max - var_min

    l = (var_max + var_min) / 2

    if (del_max == 0) {
            h = 0
            s = 0
    } else {
    	if (l < 0.5) { s = del_max / (var_max + var_min) }
        else { s = del_max / (2 - var_max - var_min) }

        def del_r = (((var_max - r) / 6) + (del_max / 2)) / del_max
        def del_g = (((var_max - g) / 6) + (del_max / 2)) / del_max
        def del_b = (((var_max - b) / 6) + (del_max / 2)) / del_max

        if (r == var_max) { h = del_b - del_g }
        else if (g == var_max) { h = (1 / 3) + del_r - del_b }
        else if (b == var_max) { h = (2 / 3) + del_g - del_r }

		if (h < 0) { h += 1 }
        if (h > 1) { h -= 1 }
    }
    def hsl = [:]
    hsl = [h: h * 100, s: s * 100, l: l]

    hsl
}

def hslToRGB(float var_h, float var_s, float var_l) {
	float h = var_h / 100
    float s = var_s / 100
    float l = var_l

    def r = 0
    def g = 0
    def b = 0

	if (s == 0) {
    	r = l * 255
        g = l * 255
        b = l * 255
	} else {
    	float var_2 = 0
    	if (l < 0.5) {
        	var_2 = l * (1 + s)
        } else {
        	var_2 = (l + s) - (s * l)
        }

        float var_1 = 2 * l - var_2

        r = 255 * hueToRgb(var_1, var_2, h + (1 / 3))
        g = 255 * hueToRgb(var_1, var_2, h)
        b = 255 * hueToRgb(var_1, var_2, h - (1 / 3))
    }

    def rgb = [:]
    rgb = [red: Math.round(r), green: Math.round(g), blue: Math.round(b)]

    rgb
}

def hueToRgb(v1, v2, vh) {
	log.debug("v1: $v1, v2: $v2, vh: $vh");
	if (vh < 0) { vh += 1 }
	if (vh > 1) { vh -= 1 }
	if ((6 * vh) < 1) { return (v1 + (v2 - v1) * 6 * vh) }
    if ((2 * vh) < 1) { return (v2) }
    if ((3 * vh) < 2) { return (v1 + (v2 - v1) * ((2 / 3 - vh) * 6)) }
    return (v1)
}

def colorNameToRgb(color) {
    final colors = [
	[name:"Soft White",	red: 255, green: 241,   blue: 224],
	[name:"Daylight", 	red: 255, green: 255,   blue: 251],
	[name:"Warm White", 	red: 255, green: 244,   blue: 229],

	[name:"Red", 		red: 255, green: 0,	blue: 0	],
	[name:"Green", 		red: 0,   green: 255,	blue: 0	],
	[name:"Blue", 		red: 0,   green: 0,	blue: 255],

	[name:"Cyan", 		red: 0,   green: 255,	blue: 255],
	[name:"Magenta", 	red: 255, green: 0,	blue: 33],
	[name:"Orange", 	red: 255, green: 102,   blue: 0	],

	[name:"Purple", 	red: 170, green: 0,	blue: 255],
	[name:"Yellow", 	red: 255, green: 255,   blue: 0	],
	[name:"White", 		red: 255, green: 255,   blue: 255]
    ]
    log.debug("It: ${it}")
    def colorData = [:]
    colorData = colors.find { it.name == color }
    colorData
}



def toggleTiles(color) {
    state.colorTiles = []
    if ( !state.colorTiles ) {
    	state.colorTiles = ["softwhite","daylight","warmwhite","red","green","blue","cyan","magenta",
			    "orange","purple","yellow","white"]
    }

    def cmds = []

    state.colorTiles.each({
    	if ( it == color ) {
            //log.debug "Turning ${it} on"
            cmds << sendEvent(name: it, value: "on${it}", displayed: True, descriptionText: "${device.displayName} ${color} is 'ON'", isStateChange: true)
        } else {
            //log.debug "Turning ${it} off"
            cmds << sendEvent(name: it, value: "off${it}", displayed: false)
      }
    })
}

// rows of buttons
def brightness(level) { doBrightness(level) }

def softwhite() { doColorButton("Soft White") }
def daylight()  { doColorButton("Daylight") }
def warmwhite() { doColorButton("Warm White") }

def red() 	{ doColorButton("Red") }
def green() 	{ doColorButton("Green") }
def blue() 	{ doColorButton("Blue") }

def cyan() 	{ doColorButton("Cyan") }
def magenta()	{ doColorButton("Magenta") }
def orange() 	{ doColorButton("Orange") }

def purple()	{ doColorButton("Purple") }
def yellow() 	{ doColorButton("Yellow") }
def white() 	{ doColorButton("White") }

def solid()		{ doEffectButton("solid") }
def candycane() { doEffectButton("candycane") }
def confetti() { doEffectButton("confetti") }
def cyclonrainbow() { doEffectButton("cyclonrainbow") }
def dots() { doEffectButton("dots") }
def fire() { doEffectButton("fire") }
def glitter() { doEffectButton("glitter") }
def juggle() { doEffectButton("juggle") }
def lightning() { doEffectButton("lightning") }
def policeall() { doEffectButton("policeall") }
def policeone() { doEffectButton("policeone") }
def rainbow() { doEffectButton("rainbow") }
def rainbowwithglitter() { doEffectButton("rainbowwithglitter") }
def sinelon() { doEffectButton("sinelon") }
def twinkle() { doEffectButton("twinkle") }
def randomstars() { doEffectButton("randomstars") }
def sinehue() { doEffectButton("sinehue") }
def ripple() { doEffectButton("ripple") }


def installed() {

}