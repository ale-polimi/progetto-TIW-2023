/**
 * Script to manage all the home elements.
 */

/**
 * Constructor for a product object.
 * @param {PageManager} manager is the page manager that contains the product.
 * @param {Node} rtProductsList is the response tag containing the products list.
 */
function Product(manager, rtProductsList){
	this.productsList = rtProductsList;
	this.offersList;
	
	this.update = function(product){
		var self = this;
		var productTable, row1, row2, row3, row4, cellImage, image, cellCategory, cellName, cellPrice, cellDescription, cellOffers, divOffers;
		
		// Function used to load the offers of a selected product.
		var loadOffers = function(e) {
			if(self.offersList.isHidden()){
				var selectedId = e.target.getAttribute("productId");
				addToVisualized(selectedId);
				self.offersList.loadingFunction();
				self.offersList.show();
			} else {
				self.offersList.hide();
			}
		}
		
		// Creating the table.
		productTable = document.createElement("table");
		productTable.className = "productTable";
		this.productsList.appendChild(productTable);
		
		row1 = document.createElement("tr");
		productTable.appendChild(row1);
		
		row2 = document.createElement("tr");
		productTable.appendChild(row2);
		
		row3 = document.createElement("tr");
		productTable.appendChild(row3);
		
		row4 = document.createElement("tr");
		row4.colSpan = "3";
		productTable.appendChild(row4);
		
		// Offers section
		cellOffers = document.createElement("td");
		cellOffers.colSpan = "3";
		row4.appendChild(cellOffers);
		
		divOffers = document.createElement("div");
		cellOffers.appendChild(divOffers);
		
		this.offersList = new ObjectsList(manager, Offer, divOffers,
			function(){
				loadList(this, "GET", "SearchProduct?productId=" + product.id, null, manager.message);
			}
		);
		this.offersList.hide();
		
		// Image section
		cellImage = document.createElement("td");
		cellImage.rowSpan = "3";
		cellImage.className = "tdImage";
		row1.appendChild(cellImage);
		
		image = document.createElement("img");
		image.src = "data:image/jpg;base64," + product.image;
		image.className = "bigImage";
		image.setAttribute("productId", product.id);
		image.addEventListener("click", loadOffers, false);
		cellImage.appendChild(image);
		
		// Category section
		cellCategory = document.createElement("td");
		cellCategory.textContent = product.category;
		row1.appendChild(cellCategory);
		
		// Name section
		cellName = document.createElement("td");
		cellName.textContent = product.name;
		cellName.className = "productText";
		cellName.setAttribute("productId", product.id);
		cellName.addEventListener("click", loadOffers, false);
		row2.appendChild(cellName);
		
		// Price section
		cellPrice = document.createElement("td");
		cellPrice.textContent = product.price.toFixed(2) + " \u20AC";
		row2.appendChild(cellPrice);
		
		// Description section
		cellDescription = document.createElement("td");
		cellDescription.textContent = product.description;
		cellDescription.colSpan = "3";
		row3.appendChild(cellDescription);
	};
}

/**
 * Constructor for an order object.
 * @param {PageManager} manager is the page manager that contains the order.
 * @param {Node} rtOrdersList is the response tag containing the orders list.
 */
function Order(manager, rtOrdersList){
	this.ordersList = rtOrdersList;
	this.update = function(order){
		var orderTable, row1, row2, row3, cellPurchasedItem, cellAddress, cellList, productsList;
		
		orderTable = document.createElement("table");
		orderTable.className = "orderTable";
		this.ordersList.appendChild(orderTable);
		
		row1 = document.createElement("tr");
		orderTable.appendChild(row1);
		
		row2 = document.createElement("tr");
		orderTable.appendChild(row2);
		
		row3 = document.createElement("tr");
		orderTable.appendChild(row3);
		
		// Purchased items information section
		cellPurchasedItem = document.createElement("td");
		cellPurchasedItem.textContent = "Purchased on " + order.date + "\n" +
										"From the supplier " + order.supplier.name + " - " + "Total: " + order.total.toFixed(2) + " \u20AC";
		row1.appendChild(cellPurchasedItem);
		
		// Address section
		cellAddress = document.createElement("td");
		cellAddress.textContent = "Shipping address:" + "\n" +
								  order.address.via + " " + order.address.numero + ", " + order.address.citta + ", " + order.address.cap;
		row2.appendChild(cellAddress);
		
		// List of the purchased products
		cellList = document.createElement("td");
		row3.appendChild(cellList);
		
		productsList = document.createElement("ul");
		cellList.appendChild(productsList);
		
		order.products.forEach((product) => {
			var productEntry = document.createElement("li");
			productEntry.textContent = product.name + "   x " + product.quantity;
			productsList.appendChild(productEntry);
		});
	}
}

/**
 * Constructor for a cart object.
 * @param {PageManager} manager is the page manager that contains the cart.
 * @param {Node} rtCartList is the response tag containing the cart.
 * @param {Boolean} display is a flag. If it's set to {@code true}, the information about the shipping address and the supplier are not shown. Needed to view the cart products in the RESULTS section.
 */
function Cart(manager, rtCartList, display){
	this.cartList = rtCartList;
	this.display = display;
	
	this.update = function(cart){
		var cartTable, tBodyCart, row1, row2, row3, cellTotal, cellShippingPrice, cellShipping, formShipping, textCitta, textVia, textNumero, textCap, buttonShipping, label, linebreak;
		
		var user = userInfo();
		var address = user.address;
		
		cartTable = document.createElement("table");
		cartTable.className = "cartTable";
		this.cartList.appendChild(cartTable);
		
		// Supplier's name
		if(!display){
			var tHeadCart, rowSupplier, cellSupplier;
			
			tHeadCart = document.createElement("thead");
			cartTable.appendChild(tHeadCart);
			
			rowSupplier = document.createElement("tr");
			tHeadCart.appendChild(rowSupplier);
			
			cellSupplier = document.createElement("th");
			cellSupplier.colSpan = "4";
			cellSupplier.textContent = cart.supplier.name;
			rowSupplier.appendChild(cellSupplier);
		}
		
		tBodyCart = document.createElement("tbody");
		cartTable.appendChild(tBodyCart);
		
		// List of products inside the cart
		cart.products.forEach((product) => {
			var row, cellImage, image, cellName, cellQuantity, cellPrice;
			
			row = document.createElement("tr");
			tBodyCart.appendChild(row);
			
			// Image of the product
			cellImage = document.createElement("td");
			cellImage.className = "cartImage";
			row.appendChild(cellImage);
			
			image = document.createElement("img");
			image.src = "data:image/jpg;base64," + product.image;
			image.className = "mediumImage";
			cellImage.appendChild(image);
			
			// Product information
			cellName = document.createElement("td");
			cellName.className = "cartProductInfo";
			cellName.textContent = product.name;
			row.appendChild(cellName);
			
			cellQuantity = document.createElement("td");
			cellQuantity.className = "cartProductInfo";
			cellQuantity.textContent = "x" + product.quantity;
			row.appendChild(cellQuantity);
			
			cellPrice = document.createElement("td");
			cellPrice.className = "cartProductInfo";
			cellPrice.textContent = product.price.toFixed(2) + " \u20AC";
			row.appendChild(cellPrice);
		});
		
		row1 = document.createElement("tr");
		tBodyCart.appendChild(row1);
		
		row2 = document.createElement("tr");
		tBodyCart.appendChild(row2);
		
		// Total of the cart
		cellTotal = document.createElement("td");
		cellTotal.textContent = "Total: " + cart.totalCost.toFixed(2) + " \u20AC";
		cellTotal.colSpan = "4";
		row1.appendChild(cellTotal);
		
		// Shipping price for the cart
		cellShippingPrice = document.createElement("td");
		cellShippingPrice.textContent = "Shipping cost: " + cart.shippingCost.toFixed(2) + " \u20AC";
		cellShippingPrice.colSpan = "4";
		row2.appendChild(cellShippingPrice);
		
		if(!display){
			row3 = document.createElement("tr");
			tBodyCart.appendChild(row3);
			
			// Form for the shipping address
			cellShipping = document.createElement("td");
			cellShipping.colSpan = "4";
			row3.appendChild(cellShipping);
			
			formShipping = document.createElement("form");
			formShipping.action = "#"; // No need for redirect for data collection after submission.
			formShipping.id = "formShipping";
			cellShipping.appendChild(formShipping);
			
			// Creating the input fields for the form
			// City
			label = document.createElement("label");
			label.innerHTML = "City: ";
			formShipping.appendChild(label);
			textCitta = document.createElement("input");
			textCitta.name = "citta";
			textCitta.type = "text";
			textCitta.value = address.citta;
			formShipping.appendChild(textCitta);
			
			linebreak = document.createElement("br");
			formShipping.appendChild(linebreak);
			
			// Road
			label = document.createElement("label");
			label.innerHTML = "Road: ";
			formShipping.appendChild(label);
			textVia = document.createElement("input");
			textVia.name = "via";
			textVia.type = "text";
			textVia.value = address.via;
			formShipping.appendChild(textVia);
			
			linebreak = document.createElement("br");
			formShipping.appendChild(linebreak);
			
			// Number
			label = document.createElement("label");
			label.innerHTML = "Number: ";
			formShipping.appendChild(label);
			textNumero = document.createElement("input");
			textNumero.name = "numero";
			textNumero.type = "text";
			textNumero.value = address.numero;
			formShipping.appendChild(textNumero);
			
			linebreak = document.createElement("br");
			formShipping.appendChild(linebreak);
			
			// Zip code
			label = document.createElement("label");
			label.innerHTML = "Zip code: ";
			formShipping.appendChild(label);
			textCap = document.createElement("input");
			textCap.name = "cap";
			textCap.type = "text";
			textCap.value = address.cap;
			formShipping.appendChild(textCap);
			
			linebreak = document.createElement("br");
			formShipping.appendChild(linebreak);
			
			var formCart = document.createElement("input");
			formCart.name = "cookieCart";
			formCart.hidden = true;
			formCart.value = "";
			formShipping.appendChild(formCart);
			
			buttonShipping = document.createElement("input");
			buttonShipping.type = "button";
			buttonShipping.value = "ORDER NOW";
			buttonShipping.addEventListener("click", (e) => {
				var form = e.target.closest("form");
				if(form.checkValidity()){
					formCart.value = returnCartCookieFromSupplier(user.id, cart.supplier.id);
					makeCall("POST", "CreateOrder", new FormData(form), manager.message,
						function(){
							formCart.value = "";
							clearCartCookies(user.id, cart.supplier.id);
							manager.viewOrders();
						}, false, false);
				} else {
					form.reportValidity();
				}
			}, false);
			
			formShipping.appendChild(buttonShipping);
		}
	};
}

/**
 * Function to manage the products and their supplier.
 * @param {PageManager} manager is the page manager that contains the product offers.
 * @param {Node} rtOffersList is the response tag containing the product offers.
 */
function Offer(manager, rtOffersList){
	this.offersList = rtOffersList;
	
	this.update = function(offer){
		var rowOffer = new Array();
		var divOffer, tableOffer, tHeadOffer, rowHead, tBodyOffer, cellSupplier, divInfo, divFreeShippingLimit, divNCart, divOverlayCart, formCart, numCart, buttonCart;
		
		divOffer = document.createElement("div");
		this.offersList.appendChild(divOffer);
		
		tableOffer = document.createElement("table");
		tableOffer.className = "tableOffer";
		divOffer.appendChild(tableOffer);
		
		tHeadOffer = document.createElement("thead");
		tableOffer.appendChild(tHeadOffer);
		
		tBodyOffer = document.createElement("tbody");
		tableOffer.appendChild(tBodyOffer);
		
		rowHead = document.createElement("tr");
		tHeadOffer.appendChild(rowHead);
		
		rowOffer[0] = document.createElement("tr");
		tBodyOffer.appendChild(rowOffer[0]);
		
		// Table header
		rowHead.appendChild(document.createElement("th"));
		var iRange, iMinNumber, iMaxNumber;
		iRange = document.createElement("th");
		iRange.textContent = "Shipping";
		rowHead.appendChild(iRange);
		iMinNumber = document.createElement("th");
		iMinNumber.textContent = "Minimum number of products";
		rowHead.appendChild(iMinNumber);
		iMaxNumber = document.createElement("th");
		iMaxNumber.textContent = "Maximum number of products";
		rowHead.appendChild(iMaxNumber);
		
		// Offer's supplier
		cellSupplier = document.createElement("td");
		cellSupplier.className = "cellSupplier";
		cellSupplier.textContent = offer.supplier.name + " - " + offer.supplier.evaluation + " \u2605 \n" +
								   offer.price.toFixed(2) + " \u20AC";
		cellSupplier.rowSpan = offer.supplier.politicaSpedizione.length;
		rowOffer[0].appendChild(cellSupplier);
		
		// Shipping fares of the offer
		for(let i = 0; i < offer.supplier.politicaSpedizione.length; i++){
			var shippingRange = offer.supplier.politicaSpedizione[i];
			var rowShippingRange, cellShipping, cellMinimum, cellMaximum;
			
			if(rowOffer[i] !== undefined){
				rowShippingRange = rowOffer[i];
			} else {
				rowShippingRange = document.createElement("tr");
				tBodyOffer.appendChild(rowShippingRange);
				rowOffer[i] = rowShippingRange;
			}
			
			cellShipping = document.createElement("td");
			cellShipping.textContent = shippingRange.price.toFixed(2) + " \u20AC";
			rowShippingRange.appendChild(cellShipping);
			
			cellMinimum = document.createElement("td");
			if(shippingRange.min > 0){
				cellMinimum.textContent = shippingRange.min;
			} else {
				cellMinimum.textContent = "N/A";
			}
			rowShippingRange.appendChild(cellMinimum);
			
			cellMaximum = document.createElement("td");
			if(shippingRange.max > 0){
				cellMaximum.textContent = shippingRange.max;
			} else {
				cellMaximum.textContent = "N/A";
			}
			rowShippingRange.appendChild(cellMaximum);
		}
		
		divInfo = document.createElement("div");
		divInfo.className = "divInfoOffer";
		divOffer.appendChild(divInfo);
		
		divFreeShippingLimit = document.createElement("div");
		if(offer.supplier.freeShippingLimit > 0){
			divFreeShippingLimit.textContent = "Free shipping available for orders above: " + offer.supplier.freeShippingLimit + " \u20AC";
		} else {
			divFreeShippingLimit.textContent = "No free shipping available for this supplier";
		}
		divInfo.appendChild(divFreeShippingLimit);
		
		// Number of products already in the cart
		divNCart = document.createElement("div");
		divNCart.className = "overlaySource";
		divNCart.textContent = "Number of products already in the cart: " + numberOfCookieProductsFromSupplier(userInfo().id, offer.supplier.id);
		
		// Cart overlay, shown on mouse hover 
		divOverlayCart = document.createElement("div");
		divOverlayCart.className = "overlay";
		divOverlayCart.hidden = true;
		
		// List of products shown inside the overlay
		var overlayList = new ObjectsList(this, Cart, divOverlayCart,
											  function(){
												  // The products inside the overlayed cart are asked to the server with a request.
												  loadList(this, "POST", "LoadCart", returnCartCookieFromSupplier(userInfo().id, offer.supplier.id), manager.message, true);  
											  },
										  true);
		
		// Listener to manage the opening/closing of the overlay
		divNCart.addEventListener("mouseenter", () => {
			divOverlayCart.hidden = false;
			overlayList.show();
			overlayList.loadingFunction();
		}, false);
		divNCart.addEventListener("mouseleave", () => {
			divOverlayCart.hidden = true;
			overlayList.hide();
		}, false);
		divOverlayCart.addEventListener("mouseleave", () => {
			divOverlayCart.hidden = true;
			overlayList.hide();
		}, false);
		
		divInfo.appendChild(divNCart);
		divNCart.appendChild(divOverlayCart);
		
		// Form to add the product to the cart
		formCart = document.createElement("form");
		formCart.action = "#";
		divInfo.appendChild(formCart);
		
		// Number of products to add
		numCart = document.createElement("input");
		numCart.name = "quantity";
		numCart.type = "number";
		numCart.min = "1";
		numCart.value = "1";
		numCart.required = "true";
		numCart.addEventListener("keypress", (e) => {
			if(e.code === "Enter"){
				e.preventDefault();
			}
		}, false);
		formCart.appendChild(numCart);
		
		// ADD TO CART button
		buttonCart = document.createElement("input");
		buttonCart.type = "button";
		buttonCart.value = "Confirm";
		buttonCart.addEventListener("click", (e) => {
			var form = e.target.closest("form");
			form.quantity.setCustomValidity("");
			if(form.checkValidity() && form.quantity.value != "" && !isNaN(Number(form.quantity.value))){
				if(checkAddCookieProduct(userInfo().id, offer.supplier.id, offer.id, form.quantity.value) && Number(form.quantity.value) > 0){
					// If the form is valid, all the products are added to the cart and the cart page is visualized.
					addCookieProduct(userInfo().id, offer.supplier.id, offer.id, form.quantity.value);
					manager.viewCart();
				} else {
					form.quantity.setCustomValidity("Quantity is out of range.");
					form.quantity.reportValidity();
				}
			} else {
				form.reportValidity();
			}
		}, false);
		formCart.appendChild(buttonCart);
	}
}

 /**
  * Constructor for a list of generic objects.
  * @param {PageManager} manager is the page manager that contains the list.
  * @param {Function} ObjectType is the reference to the constructor of objects contained in the list.
  * @param {Node} divList is the page element that will contain the list.
  * @param {Function} loadFunction is the function that loads the list.
  * @param {Object} optParameterForObject is an optional parameter for objectType.
  */
 function ObjectsList(manager, ObjectType, divList, loadFunction, optParameterForObject){
	 this.divList = divList;
	 this.loadingFunction = loadFunction;
	 
	 this.update = function(objects){
		 this.show();
		 this.divList.innerHTML = ""; // Flush the list
		 var self = this;
		 
		 /* Init every object and call its update function. */
		 objects.forEach((object) => {
			 var p = new ObjectType(manager, self.divList, optParameterForObject);
			 p.update(object);
		 });
	 };
	 
	 this.show = function() {
		 this.divList.hidden = false;
	 };
	 
	 this.hide = function() {
		 this.divList.hidden = true;
	 };
	 
	 this.isHidden = function() {
		 return this.divList.hidden;
	 };
 }