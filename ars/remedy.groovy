import groovy.json.*

//build the url
def url="http://api.fixer.io/latest"
def targetRate = "USD"
def interfaceForm = "manyos:currency"

log.info "Query Fixer for latest Exchange Rates"

//call the connector
def exchangeResponse=call.connector("Fixer")
         .set("method","get")
         .set("url",url)
         .set("timeout",15000)
         .sync()

//get the response
def myBody = exchangeResponse.body

//Create a Object out of the JSON response
def jsonSlurper = new JsonSlurper()
def myRates = jsonSlurper.parseText(myBody)

log.info "Query Remedy for existing records"

//Get the actual Values
def rate = myRates.rates.USD
def runDate = myRates.date
def query = "'Run Date' = \"${runDate}\""

def queryResponse = call.bit("remedy:base:query.groovy")         // Provide path for flintbit
                  .set("form",interfaceForm) // Set arguments
                  .set("query", query)
                  .sync()

//Check if record for today already exists - if yes: update, if not: create
if (queryResponse.data.size() > 0) {
  log.info "Entry found for query: " + query + ". Update instead."
  //update all records
  queryResponse.data.each { myRecord ->
    log.debug "Update " + myRecord.getKey()
    def recordData = "{ \"${myRecord.getKey()}\": { \"Currency\": \"EUR -> USD\", \"Exchange Rate\":" + rate + ", \"Run Date\" : \"${runDate}\" } }"
    def updateResponse = call.bit("remedy:base:update.groovy")         // Provide path for flintbit
                        .set("form",interfaceForm) // Set arguments
                        .set("data", recordData)
                        .async()
  }
} else {
  log.info "Create new Record"
  //Create a new Record
  def recordData = "{ \"MyRefId01\": { \"Currency\": \"EUR -> USD\", \"Exchange Rate\":" + rate + ", \"Run Date\" : \"${runDate}\" } }"

  def createResponse = call.bit("remedy:base:create.groovy")         // Provide path for flintbit
                      .set("form",interfaceForm) // Set arguments
                      .set("data", recordData)
                      .sync()
}
