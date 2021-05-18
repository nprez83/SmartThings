/**
 *  Copyright 2015 SmartThings
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
 *  Author: Numa Perez. Based off URI Switch by tguerena with the following changes. 
 * 		1) Switch is initialized in the OFF state, and returns to the OFF state after being turned on, hence working more as a momentary button than a switch. 
 * 		2) Because of the above change, only a single external URI/internal path is required.
 * 		2) Added try/catch statements around the httpget call.
 */


preferences {
	section("External Access"){
		input "external_uri", "text", title: "External URI", required: false
	}
    
	section("Internal Access"){
		input "internal_ip", "text", title: "Internal IP", required: false
		input "internal_port", "text", title: "Internal Port (if not 80)", required: false
		input "internal_path", "text", title: "Internal Path (/blah?q=this)", required: false
	}
}




metadata {
	definition (name: "URI Button", namespace: "nprez83", author: "Numa Perez") {
		capability "Actuator"
		capability "Switch"
		capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles {
		standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
			state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
			state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821", nextState: "off"
		}
		main "button"
		details "button"
	}
}

def parse(String description) {
	log.debug(description)
}

def on() {
	if (external_uri){
		def cmd = "${settings.external_uri}";
		log.debug "Sending request cmd[${cmd}]"
        try {
        	httpGet(cmd) {resp ->
                if (resp.data) {
                    log.info "${resp.data}"
                }
			}
		} catch(e) {
        	log.trace(e)
        }
		sendEvent(name: "switch", value: "off")
		log.debug "Executing ON"
	}
 
	if (internal_path){
		def port
			if (internal_port){
				port = "${internal_port}"
			} else {
				port = 80
			}

		def result = new physicalgraph.device.HubAction(
				method: "GET",
				path: "${internal_path}",
				headers: [
				HOST: "${internal_ip}:${port}"
				]
				)
			sendHubCommand(result)
			sendEvent(name: "switch", value: "off") 
			log.debug "Executing ON" 
			log.debug result
	}
}

def installed() {
	log.trace "Executing 'installed'"
	initialize()
}

def updated() {
	log.trace "Executing 'updated'"
	initialize()
}

private initialize() {
	log.trace "Executing 'initialize'"
	sendEvent(name: "switch", value: "off") 
}
