/**
 * Utility functions
 */


 /**
  * Makes a call to the server using XMLHttpRequest object.
  * @param {String} httpMethod is the method to use for the call.
  * @param {String} url is the URL to use for the call.
  * @param {Object} data is the optional data to send with the call.
  * @param {Node} responseTag is the element to use to display the response message.
  * @param {Function} callBack is the function to call back on an 200-OK response from the server.
  * @param {Boolean} json is a flag which is {@code true} if the parameter 'data' is in JSON format, {@code false} otherwise.
  * @param {Boolean} login is a flag which is {@code true} if the request is from the login page, {@code false} otherwise. Needed for HTTPCode UNAUTHORIZED management.
  */
 function makeCall(httpMethod, url, data, responseTag, callBack, json, login){
	 var req = new XMLHttpRequest();
	 
	 req.onreadystatechange = function(){
		 if(req.readyState == XMLHttpRequest.DONE){
			 switch(req.status){
				 case 200: // OK
					 callBack(req);
					 break;
				 case 400: // BAD_REQUEST
					 responseTag.textContent = req.responseText;
					 break;
				 case 401: // UNAUTHORIZED
				 	 if(!login){
						  sessionStorage.removeItem("user");
					 	  sessionStorage.removeItem("listaVisualizzati");
					 	  location.href = "login.html";
					 } else {
						 responseTag.textContent = req.responseText;
					 }
					 break;
				 case 403: // FORBIDDEN
					 sessionStorage.removeItem("user");
					 sessionStorage.removeItem("listaVisualizzati");
					 location.href = "login.html";
					 break;
				 case 500: // INTERNAL_SERVER_ERROR
					 responseTag.textContent = req.responseText;
					 break;
				 default: // Any other error
					 responseTag.textContent = "Error: " + req.status + " - " + req.responseText;
			 }
		 }
	 };
	 
	 req.open(httpMethod, url);
	 
	 if(json){
		 req.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
	 }
	 
	 if(data == null){
		 req.send();
	 } else {
		 req.send(data);
	 }
 }
 
 /**
  * Retrieves all the information of the user from the session.
  */
 function userInfo(){
	 return JSON.parse(window.sessionStorage.getItem("user"));
 }
 
 /**
  * Function that loads a list of elements.
  * @param {ObjectsList} objectsList is the reference to the list of objects.
  * @param {String} httpMethod is the method to use for the call.
  * @param {String} url is the URL to use for the call.
  * @param {Object} data is the optional data to send with the call.
  * @param {Node} responseTag is the element to use to display the response message.
  * @param {Boolean} json is a flag, set to {@code true} means that the data is in JSON format.
  * @param {String} emptyMessage is the message to show in case the objectsList is empty.
  */
 function loadList(objectsList, httpMethod, url, data, responseTag, json, emptyMessage){
	 makeCall(httpMethod, url, data, responseTag,
		 		  function(req){
					   var elements = JSON.parse(req.responseText);
					   if(elements.length == 0){
						   if(emptyMessage){
							   responseTag.className = "notificationMessage";
							   responseTag.textContent = emptyMessage;
						   }
						   return;
					   }
					   objectsList.update(elements);
				  }, json);
 }
 
 /**
  * Function that adds a product to the list of visualized products.
  * @param {Number} productId is the product ID of the visualized product.
  */
 function addToVisualized(productId){
	 var listaVisualizzati = sessionStorage.getItem("listaVisualizzati");
	 
	 if(listaVisualizzati === null || listaVisualizzati === undefined){ // If the list does not exist, it's created.
		 listaVisualizzati = new Array();
		 listaVisualizzati.push(productId);
	 } else {
		 listaVisualizzati = JSON.parse(listaVisualizzati);
		 if(listaVisualizzati.includes(productId)){
			 // If the last element is not the productId, it is updated.
			 if(listaVisualizzati[0] == productId){
				 return;
			 } else {
				 listaVisualizzati.splice(listaVisualizzati.indexOf(productId), 1);
			 }
		 }
		 if(listaVisualizzati.length >= 5){
			 listaVisualizzati.pop();
		 }
		 listaVisualizzati.unshift(productId);
	 }
	 sessionStorage.setItem("listaVisualizzati", JSON.stringify(listaVisualizzati));
 }
 
 /**
  * Function that loads the proviously visualized products.
  * @returns the list of visualized products.
  */
 function loadVisualized(){
	 var listaVisualizzati = sessionStorage.getItem("listaVisualizzati");
	 
	 if(listaVisualizzati === null || listaVisualizzati === undefined){
		 listaVisualizzati = new Array();
		 sessionStorage.setItem("listaVisualizzati", JSON.stringify(listaVisualizzati));
		 listaVisualizzati = JSON.stringify(listaVisualizzati);
	 }
	 return listaVisualizzati;
 }
 
 /**
  * Function that checks if it's possible to add a product to the cart.
  * @param {Number} userId is the ID of the user.
  * @param {Number} supplierId is the ID of the supplier.
  * @param {Number} productId is the ID of the product.
  * @param {Number} quantity is the quantity of the product.
  * @returns Returns {@code false} if the product quantity is over the maximum limit (999), {@code true} otherwise.
  */
 function checkAddCookieProduct(userId, supplierId, productId, quantity){
	 if(quantity > 999){
		 return false;
	 }
	 
	 var products = returnCookieProducts(userId, supplierId);
	 if(products){
		 for(let i = 0; i < products.length; i++){
			 if(products[i].id == productId){
				 var q = Number(products[i].quantity);
				 if(q + Number(quantity) > 999){
					 return false;
				 } else {
					 break;
				 }
			 }
		 }
	 }
	 return true;
 }
 
 /**
  * Function that creates and add a cookie to the session.
  * @param {Number} userId is the ID of the user.
  * @param {Number} supplierId is the ID of the supplier.
  * @param {Number} productId is the ID of the product.
  * @param {Number} quantity is the quantity of the product.
  */
 function addCookieProduct(userId, supplierId, productId, quantity){
	 var time = 60*60*1000;
	 var date = new Date();
	 date = new Date(date.getTime + time);
	 
	 var products = returnCookieProducts(userId, supplierId);
	 var added = false;
	 
	 var cookie = userId + "-" + supplierId + "=";
	 if(products){
		 for(let i = 0; i < products.length; i++){
			 if(i != 0){
				 cookie += "_";
			 }
			 cookie += products[i].id + "-";
			 var q = Number(products[i].quantity);
			 if(products[i].id == productId){ // If the product is already in the cookie, its quantity is only increasd.
				 q += Number(quantity);
				 added = true;
			 }
			 cookie += q;
		 }
	 }
	 
	 if(!added){
		 if(products){
			 cookie += "_";
		 }
		 cookie += productId + "-" + quantity;
	 }
	 
	 // Add expiration date to the cookie
	 cookie += "; Expires=" + date.toUTCString();
	 
	 document.cookie = cookie;
 }
 
 /**
  * Function that returns the products inside the cookie.
  * @param {Number} userId is the ID of the user.
  * @param {Number} supplierId is the ID of the supplier.
  * @returns the products inside the cookie, {@code null} if there are none.
  */
 function returnCookieProducts(userId, supplierId){
	 var name = userId + "-" + supplierId + "=";
	 var decodedCookie = decodeURIComponent(document.cookie);
	 var ca = decodedCookie.split(';');
	 
	 for(let i = 0; i < ca.length; i++){
		 var c = ca[i];
		 while(c.charAt(0) == ' '){
			 c = c.substring(1);
		 }
		 
		 if(c.indexOf(name) == 0){
			 var value = c.substring(name.length, c.length);
			 var va = value.split("_");
			 var products = new Array();
			 
			 for(let j = 0; j < va.length; j++){
				 var v = va[j];
				 var pa = v.split("-");
				 
				 products.push({
					 "id": pa[0],
					 "quantity": pa[1]
				 });
			 }
			 return products;
		 }
	 }
	 
	 return null;
 }
 
 /**
  * Function that returns the carts.
  * @param {Number} userId is the ID of the user.
  * @returns the cart is JSON format.
  */
 function returnCartCookies(userId){
	 var name = userId + "-";
	 var decodedCookie = decodeURIComponent(document.cookie);
	 var ca = decodedCookie.split(';');
	 var cart = new Array();
	 
	 for(let i = 0; i < ca.length; i++){
		 var c = ca[i];
		 while(c.charAt(0) == ' '){
			 c = c.substring(1);
		 }
		 
		 if(c.indexOf(name) == 0){
			 var supplierId = Number(c.split("-")[1].split("=")[0]);
			 var products = returnCookieProducts(userId, supplierId);
			 
			 if(products){
				 cart.push({
					 "supplier": {
						 "id": supplierId
					 },
					 "products": products
				 });
			 }
		 }
	 }
	 
	 return JSON.stringify(cart);
 }
 
 /**
  * Function that returns all the products inside the cart of a supplier, with a cookie.
  * @param {Number} userId is the ID of the user.
  * @param {Number} supplierId is the ID of the supplier.
  * @returns the cart is JSON format.
  */
 function returnCartCookieFromSupplier(userId, supplierId){
	 var cart = new Array();
	 var products = returnCookieProducts(userId, supplierId);
	 
	 if(products){
		 cart.push({
			 "supplier": {
				 "id": supplierId
			 },
				 "products": products
		 });
	 }
	 
	 return JSON.stringify(cart);
 }
 
 /**
  * Function that returns the number of products already in the cart of the supplier.
  * @param {Number} userId is the ID of the user.
  * @param {Number} supplierId is the ID of the supplier.
  * @returns {Number} the number of products already in the cart of the supplier.
  */
 function numberOfCookieProductsFromSupplier(userId, supplierId){
	 var products = returnCookieProducts(userId, supplierId);
	 var number = 0;
	 if(products){
		 products.forEach((product) => {
			 number += Number(product.quantity);
		 });
	 }
	 
	 return number;
 }
 
 /**
  * Function that deletes the cookies.
  * @param {Number} userId is the ID of the user.
  * @param {Number} supplierId is the ID of the supplier.
  */
 function clearCartCookies(userId, supplierId){
	 document.cookie = userId + "-"  + supplierId + "=; Expires=Thu, 01 Jan 1970 00:00:01 GMT;";
 }