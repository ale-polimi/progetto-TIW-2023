/**
 * Login script for the login page
 */

(
	function loginForm() {
		document.getElementById("loginButton").addEventListener("click", (event) => {
			var form = event.target.closest("form");
			var message = document.getElementById("loginError");
			
			if(form.checkValidity()){
				makeCall("POST", "CheckLogin", new FormData(form), message, function(req){
					var user = JSON.parse(req.responseText);
					sessionStorage.setItem("user", JSON.stringify(user));
					sessionStorage.setItem("listaVisualizzati", JSON.stringify(new Array()));
					location.href = "home.html";
				}, null, true);
			} else {
				form.reportValidity();
			}
		});
	}
)();