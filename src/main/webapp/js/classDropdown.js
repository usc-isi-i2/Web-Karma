
var ClassDropdownMenu = (function() {
	
	var instance = null;
	
	
    function PrivateConstructor() {
    	var menuId = "classDropdownMenu";
    	var options = [
    		   	        //Title, function to call, needs file upload     
    		   	        [ "Add Incoming Link", addIncomingLink],
    		   	        [ "Add Outgoing Link", addOutgoingLink],
    		   	        [ "divider" , null ],
    		   	        
    		   			[	"Invoke Reconciliation Service" , invokeReconciliationService ],
    		   			[ "Invoke M/L Service", invokeMLService ],
    		   			
    		   			
    		   	];
    	
    	function init() {
    		generateJS();
			 
    	}
    	
    	function hide() {
    		$("#" + menuId).hide();
    	}
    	
    	function addIncomingLink() {
    		console.log("addIncomingLink");
    	};
    	
    	function addOutgoingLink() {
    		console.log("addOutgoingLink");
    	}
    	
    	function invokeReconciliationService() {
    		console.log("invokeReconciliationService");
    	}
    	
    	function invokeMLService() {
    		console.log("invokeMLService");
    	}
    	
    	function generateJS() {
    		var ul = $("<ul>");
    		ul.attr("role", "menu")
				.addClass("dropdown-menu")
				.css("display", "block")
				.css("position", "static")
				.css("margin-bottom", "5px");
    		for(var i=0; i<options.length; i++) {
    			var option = options[i];
    			var li = $("<li>");
    			if(option[0] == "divider") {
    				li.addClass("divider");
    			} else {
	    			var a = $("<a>")
								.attr("href", "#")
								.attr("tabindex", "-1")
								.text(option[0])
								.click(option[1]);
	    			li.append(a);
    			}
    			ul.append(li);
    		}
    		
    		var div = $("<div>")
    					.attr("id", menuId)
    					.addClass("dropdown")
    					.addClass("clearfix")
    					.addClass("contextMenu")
    					.append(ul);
    		
    		var container = $("body div.container");
    		container.append(div);
    	}
    	
    	function attach(nodeScript) {
    		$("body").on("click", nodeScript, function(e) {
    			//console.log("Click for opening Menu");
				$("#" + menuId).css({
			      display: "block",
			      position: "absolute",
			      left: e.pageX,
			      top: e.pageY
			    });
			    return false;
			  });
    		
    		 $(document).click(function () {
				 hide();
			  });
        };
        
        
        return {	//Return back the public methods
        	attach : attach,
        	init : init,
        	generateJS : generateJS
        };
    };

    function getInstance() {
    	if( ! instance ) {
    		instance = new PrivateConstructor();
    		instance.init();
    	}
    	return instance;
    }
   
    function attach(nodeJS) {
    	if(!instance) {
    		getInstance();
    	}
    	instance.attach(nodeJS);
    }
    
    return {
    	getInstance : getInstance,
    	attach : attach
    };
    	
    
})();
    	
    	

ClassDropdownMenu.getInstance().attach("div svg g.InternalNode");
    