/**
 * Script for the home page
 */

 (function(){ // Needed to hide the variables from the global scope
	 var pageManager;
	 
	 window.addEventListener("load", ()=> {
		 if(userInfo() != null){
			 pageManager = new PageManager();
			 pageManager.init();
			 pageManager.viewHome();
		 } else {
			 window.location.href = "login.html";
		 }
	 }, false);
	 
	 /**
	  * Constructor for the welcome message.
	  * @param {Node} responseTag is the element to use to display the response message.
	  * @param {String} username is the name of the user.
	  */
	 function WelcomeMessage(responseTag, username){
		 this.username = username;
		 this.show = function(){
			 responseTag.textContent = this.username;
		 }
	 }
	 
	 /**
	  * Constructor for the interactive menu.
	  * @param {Node} rtHome is the element containing the Home button.
	  * @param {Node} rtCart is the element containing the Cart button.
	  * @param {Node} rtOrders is the element containing the Orders button.
	  * @param {Node} rtSearchText is the element containing the search text.
	  * @param {Node} rtSearchButton is the element containing the Search button.
	  * @param {Node} rtLogout is the element containing the Logout button.
	  */
	 function Menu(rtHome, rtCart, rtOrders, rtSearchText, rtSearchButton, rtLogout){
		 this.rtHome = rtHome;
		 this.rtCart = rtCart;
		 this.rtOrders = rtOrders;
		 this.rtSearchText = rtSearchText
		 this.rtSearchButton = rtSearchButton;
		 this.rtLogout = rtLogout;
		 
		 this.addEvents = function(manager){
			 rtHome.addEventListener("click", () => {
				 manager.viewHome();
			 });
			 
			 rtCart.addEventListener("click", () => {
				 manager.viewCart();
			 });
			 
			 rtOrders.addEventListener("click", () => {
				 manager.viewOrders();
			 });
			 
			 rtSearchText.addEventListener("keypress", (e) => {
				 if(e.code === "Enter"){ // If the user presses the Enter key, the script will mask it as a click on the Search button.
					 rtSearchButton.click();
					 e.preventDefault();
				 }
			 });
			 
			 rtSearchButton.addEventListener("click", (e) => {
				 var form = e.target.closest("form");
				 if(form.checkValidity()){ // The search is done only if the form is valid.
					 manager.viewResults(form);
				 } else {
					 form.reportValidity();
				 }
			 });
			 
			 rtLogout.addEventListener("click", () => {
				makeCall("GET", "Logout", null, manager.message,
					function logoutRoutine(){
						sessionStorage.removeItem("user");
						sessionStorage.removeItem("listaVisualizzati");
						window.location.href = "login.html";
					}
				);
			 });
		 }
	 }
	 
	 /**
	  * Constructor for the page manager.
	  */
	 function PageManager(){
		 this.message = null;
		 this.welcomeMessage = null;
		 this.menu = null;
		 this.resultsList = null;
		 this.cartList = null;
		 this.ordersList = null;
		 
		 this.init = function() {
			 // Message for errors or notifications.
			 this.message = document.getElementById("message");
			 
			 // Personalized welcome message
			 this.welcomeMessage = new WelcomeMessage(document.getElementById("username"), userInfo().name);
			 this.welcomeMessage.show();
			 
			 // Interactive navigation menu
			 this.menu = new Menu(document.getElementById("homeButton"), document.getElementById("cartButton"), document.getElementById("ordersButton"), document.getElementById("searchText"), document.getElementById("searchButton"), document.getElementById("logoutButton"));
			 this.menu.addEvents(this);
			 
			 // List used to show the products
			 this.resultsList = new ObjectsList(this, Product, document.getElementById("resultsList"),
			 	function(keyword){
					 if(keyword != null){
						 loadList(this, "GET", "SearchKeyword?keyword=" + keyword, null, message, false, "No results available");
					 } else {
						 loadList(this, "POST", "LoadVisualized", loadVisualized(), message, true, "No recently visualized products.");
					 }
				 }
			 );
			 
			 // list used to show the cart
			 this.cartList = new ObjectsList(this, Cart, document.getElementById("cartList"),
			 	function(){
					 loadList(this, "POST", "LoadCart", returnCartCookies(userInfo().id), message, true, "Your cart is empty");
				 }
			 );
			 
			 // List used to show the orders
			 this.ordersList = new ObjectsList(this, Order, document.getElementById("ordersList"),
			 	function(){
					loadList(this, "GET", "ViewOrders", null, message, false, "You have no orders"); 
				 }
			 );
		 }
		 
		 this.update = function(){
			 this.message.textContent = "";
			 this.resultsList.hide();
			 this.cartList.hide();
			 this.ordersList.hide();
		 }
		 
		 this.viewHome = function(){
			 this.update();
			 this.resultsList.loadingFunction(null);
		 }
		 
		 this.viewCart = function(){
			 this.update();
			 this.cartList.loadingFunction();
		 }
		 
		 this.viewOrders = function(){
			 this.update();
			 this.ordersList.loadingFunction();
		 }
		 
		 this.viewResults = function(form){
			 this.update();
			 this.resultsList.loadingFunction(new FormData(form).get("keyword"));
		 }
	 }
 })();