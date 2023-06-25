/*
The code is enclosed within $( document ).ready(function() { ... });, which ensures that the JavaScript code executes when the DOM (Document Object Model) is fully loaded.
*/
$( document ).ready(function() {
    //The line console.log( "ready!" ); logs "ready!" to the console once the DOM is ready.
        console.log( "ready!" );
    
    //The variables button, searchBox, resultsTable, and resultsWrapper are assigned jQuery objects based on their respective HTML element IDs ($("#submit_button"), $("#search_text"), $("#results table tbody"), and $("#results")).
        var button = $("#submit_button");   
        var searchBox = $("#search_text"); 
        var resultsTable = $("#results table tbody"); 
        var resultsWrapper = $("#results"); 
    
    //The button.on("click", function(){ ... }); sets up a click event handler for the button element. When the button is clicked, the code inside the function will be executed.
        button.on("click", function(){
    
    //Within the click event handler, an AJAX request is made using $.ajax({ ... });. It sends a POST request to the "procesar_datos" URL, with the request data obtained from the createRequest() function. The contentType is set to "application/json" to specify that the request data is in JSON format. The dataType is set to "json" to expect a JSON response from the server. The success callback function onHttpResponse is specified to handle the response.
            $.ajax({
              method : "POST",
              contentType: "text/plain",
              data: createRequest(),
              url: "procesar_datos",
              dataType: "text",
              success: onHttpResponse
              });
          });
    
    //The createRequest() function retrieves the value from the searchBox input field and creates a JavaScript object called frontEndRequest with a property named searchQuery. It returns the JSON string representation of the frontEndRequest object.
        function createRequest() {
            var searchQueryTmp = searchBox.val();        
            return searchQueryTmp;
        }
    
    //The onHttpResponse function is called when the AJAX request is successful. It checks the status parameter to determine if the request was successful. If successful, it logs the data parameter to the console and calls the addResults function, passing the data object as an argument. If the request fails, it displays an alert with an error message.
        function onHttpResponse(data, status) {
            if (status === "success" ) {
                console.log(data);
                addResults(data);
            } else {
                alert("Error al conectarse al servidor: " + status);
            }
        }
    
    //The addResults function receives the data object as a parameter. It clears the contents of the resultsTable element, shows the resultsWrapper element, and appends a table row to the resultsTable with the received data (cadena) and the number of words (cantidad). The table row is constructed using string concatenation.
        function addResults(data) {
            resultsTable.empty();
    
            var cantidad = data.numero;
            var cadena = "";
            resultsWrapper.show();
            resultsTable.append("<thead><tr><th>   </th><th>   Factorial</th></tr></thead><tr><td>" + cadena + "</td><td>" + cantidad + "</td></tr>");
        }
    });
    
    